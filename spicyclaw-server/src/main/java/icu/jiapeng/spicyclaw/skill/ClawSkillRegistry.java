package icu.jiapeng.spicyclaw.skill;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import icu.jiapeng.spicyclaw.skill.cache.ClawSkillCacheSync;
import icu.jiapeng.spicyclaw.skill.cache.SkillCacheEvent;
import icu.jiapeng.spicyclaw.skill.dto.SkillResponse;
import icu.jiapeng.spicyclaw.skill.entity.ClawSkillEntity;
import icu.jiapeng.spicyclaw.skill.mapper.ClawSkillMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 技能注册表：DB 为清单与启停权威来源；持久化目录存内容；skillsDir 为运行时缓存。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClawSkillRegistry {

    private final ClawSkillLoader loader;
    private final ClawSkillMapper skillMapper;
    private final ClawSkillCacheSync cacheSync;

    public List<SkillResponse> listSkills() {
        return skillMapper.selectList(
                        new LambdaQueryWrapper<ClawSkillEntity>().orderByDesc(ClawSkillEntity::getUpdatedAt))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public boolean isSlugEnabled(String slug) {
        if (slug == null || slug.isBlank()) {
            return false;
        }
        ClawSkillEntity entity = skillMapper.selectOne(
                new LambdaQueryWrapper<ClawSkillEntity>().eq(ClawSkillEntity::getSlug, slug));
        return entity != null && Boolean.TRUE.equals(entity.getEnabled());
    }

    @Transactional
    public SkillResponse install(String path) throws IOException {
        ClawSkillLoader.LoadedSkill loaded = loader.installFromPath(path);
        upsertEntity(loaded);
        cacheSync.broadcast(SkillCacheEvent.refresh(loaded.slug()));
        ClawSkillEntity entity = skillMapper.selectOne(
                new LambdaQueryWrapper<ClawSkillEntity>().eq(ClawSkillEntity::getSlug, loaded.slug()));
        return toResponse(entity);
    }

    @Transactional
    public SkillResponse setEnabled(String slug, boolean enabled) {
        ClawSkillEntity entity = skillMapper.selectOne(
                new LambdaQueryWrapper<ClawSkillEntity>().eq(ClawSkillEntity::getSlug, slug));
        if (entity == null) {
            throw new IllegalArgumentException("Skill not found: " + slug);
        }
        entity.setEnabled(enabled);
        entity.setUpdatedAt(OffsetDateTime.now());
        skillMapper.updateById(entity);
        cacheSync.broadcast(enabled ? SkillCacheEvent.refresh(slug) : SkillCacheEvent.evict(slug));
        return toResponse(entity);
    }

    /**
     * 从持久化目录重新解析 SKILL.md 元数据，并刷新各实例 skillsDir 缓存。
     */
    public void reload() throws IOException {
        for (ClawSkillEntity entity : skillMapper.selectList(null)) {
            if (entity.getPath() == null || entity.getPath().isBlank()) {
                log.warn("Skip reload for skill {}: store path is empty", entity.getSlug());
                continue;
            }
            ClawSkillLoader.LoadedSkill loaded = loader.loadFromDirectory(Path.of(entity.getPath()));
            if (loaded == null) {
                log.warn("Skip reload for skill {}: SKILL.md missing at {}", entity.getSlug(), entity.getPath());
                continue;
            }
            upsertEntity(loaded);
            log.info("Reloaded skill metadata: {} ({})", loaded.slug(), loaded.path());
            if (Boolean.TRUE.equals(entity.getEnabled())) {
                cacheSync.broadcast(SkillCacheEvent.refresh(entity.getSlug()));
            }
        }
    }

    private void upsertEntity(ClawSkillLoader.LoadedSkill loaded) {
        Map<String, Object> metadata = new LinkedHashMap<>(loaded.metadata());
        String name = loaded.name();
        String description = loaded.description();

        ClawSkillEntity existing = skillMapper.selectOne(
                new LambdaQueryWrapper<ClawSkillEntity>().eq(ClawSkillEntity::getSlug, loaded.slug()));
        OffsetDateTime now = OffsetDateTime.now();
        if (existing == null) {
            ClawSkillEntity entity = new ClawSkillEntity();
            entity.setSlug(loaded.slug());
            entity.setName(name);
            entity.setDescription(description);
            entity.setSource("local");
            entity.setPath(loaded.path());
            entity.setEnabled(true);
            entity.setMetadata(metadata);
            entity.setCreatedAt(now);
            entity.setUpdatedAt(now);
            skillMapper.insert(entity);
        } else {
            existing.setName(name);
            existing.setDescription(description);
            existing.setPath(loaded.path());
            existing.setMetadata(metadata);
            existing.setUpdatedAt(now);
            skillMapper.updateById(existing);
        }
    }

    private SkillResponse toResponse(ClawSkillEntity entity) {
        return new SkillResponse(
                entity.getId(),
                entity.getSlug(),
                entity.getName(),
                entity.getDescription(),
                entity.getSource(),
                entity.getPath(),
                Boolean.TRUE.equals(entity.getEnabled()));
    }
}
