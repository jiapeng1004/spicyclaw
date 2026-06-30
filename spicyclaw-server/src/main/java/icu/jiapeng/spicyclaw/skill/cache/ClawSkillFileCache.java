package icu.jiapeng.spicyclaw.skill.cache;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import icu.jiapeng.spicyclaw.config.SpicyclawProperties;
import icu.jiapeng.spicyclaw.skill.ClawSkillLoader;
import icu.jiapeng.spicyclaw.skill.entity.ClawSkillEntity;
import icu.jiapeng.spicyclaw.skill.mapper.ClawSkillMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 本地 skillsDir 文件缓存：启动时清空；按需从 DB 记录的持久化路径物化到缓存目录。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClawSkillFileCache {

    private final SpicyclawProperties properties;
    private final ClawSkillLoader loader;
    private final ClawSkillMapper skillMapper;

    @EventListener(ApplicationReadyEvent.class)
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void clearOnStartup() throws IOException {
        Path cacheDir = cacheRoot();
        clearDirectory(cacheDir);
        Files.createDirectories(cacheDir);
        Files.createDirectories(storeRoot());
        log.info("Skills cache cleared on startup: {}", cacheDir.toAbsolutePath());
    }

    public Path cacheRoot() {
        return Path.of(properties.getSkillsDir());
    }

    public Path storeRoot() {
        return Path.of(properties.getSkillsStoreDir());
    }

    public Path cachePathFor(String slug) {
        return cacheRoot().resolve(slug);
    }

    public void clearAll() throws IOException {
        clearDirectory(cacheRoot());
        Files.createDirectories(cacheRoot());
    }

    public void evict(String slug) throws IOException {
        if (slug == null || slug.isBlank()) {
            return;
        }
        clearDirectory(cachePathFor(slug));
    }

    /**
     * 将 DB 中 enabled 的技能全部物化到 skillsDir。
     */
    public void refreshAllEnabled() throws IOException {
        List<ClawSkillEntity> enabled = skillMapper.selectList(new LambdaQueryWrapper<ClawSkillEntity>()
                .eq(ClawSkillEntity::getEnabled, true)
                .orderByDesc(ClawSkillEntity::getUpdatedAt));
        for (ClawSkillEntity entity : enabled) {
            refreshFromStore(entity.getSlug());
        }
    }

    /**
     * 从持久化目录复制到 skillsDir；获取 Agent 技能前调用。
     */
    public Path ensureCached(ClawSkillEntity entity) throws IOException {
        if (entity == null || !Boolean.TRUE.equals(entity.getEnabled())) {
            return null;
        }
        refreshFromStore(entity.getSlug());
        return cachePathFor(entity.getSlug());
    }

    public void refreshFromStore(String slug) throws IOException {
        if (slug == null || slug.isBlank()) {
            return;
        }
        ClawSkillEntity entity = skillMapper.selectOne(
                new LambdaQueryWrapper<ClawSkillEntity>().eq(ClawSkillEntity::getSlug, slug));
        if (entity == null) {
            evict(slug);
            return;
        }
        if (!Boolean.TRUE.equals(entity.getEnabled())) {
            evict(slug);
            return;
        }
        Path storePath = Path.of(entity.getPath());
        if (!Files.isDirectory(storePath)) {
            log.warn("Skill store path missing for {}: {}", slug, storePath);
            evict(slug);
            return;
        }
        Path cachePath = cachePathFor(slug);
        loader.materializeDirectory(storePath, cachePath);
        log.debug("Refreshed skill cache: {} -> {}", storePath, cachePath);
    }

    private void clearDirectory(Path root) throws IOException {
        if (!Files.exists(root)) {
            return;
        }
        loader.clearDirectory(root);
    }
}
