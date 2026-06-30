package icu.jiapeng.spicyclaw.skill;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import icu.jiapeng.spicyclaw.skill.cache.ClawSkillFileCache;
import icu.jiapeng.spicyclaw.skill.entity.ClawSkillEntity;
import icu.jiapeng.spicyclaw.skill.mapper.ClawSkillMapper;
import io.agentscope.core.skill.repository.AgentSkillRepository;
import io.agentscope.core.skill.repository.AgentSkillRepositoryInfo;
import io.agentscope.core.skill.util.SkillFileSystemHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Agent 技能仓库：从 DB 取 enabled 清单，获取时物化到 skillsDir 缓存再加载。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClawAgentSkillRepository implements AgentSkillRepository {

    private static final String SOURCE = "spicyclaw";

    private final ClawSkillMapper skillMapper;
    private final ClawSkillFileCache fileCache;
    private final ConcurrentMap<String, io.agentscope.core.skill.AgentSkill> memoryCache = new ConcurrentHashMap<>();
    private volatile boolean writeable = false;

    public void clearCache() {
        memoryCache.clear();
    }

    @Override
    @SuppressWarnings("deprecation")
    public io.agentscope.core.skill.AgentSkill getSkill(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String key = name.strip();
        io.agentscope.core.skill.AgentSkill cached = memoryCache.get(key);
        if (cached != null) {
            return cached;
        }
        for (ClawSkillEntity entity : enabledEntities()) {
            io.agentscope.core.skill.AgentSkill skill = loadSkill(entity);
            if (skill == null) {
                continue;
            }
            cacheSkill(skill);
            if (key.equals(entity.getSlug()) || key.equals(skill.getName())) {
                return skill;
            }
        }
        return memoryCache.get(key);
    }

    @Override
    public List<String> getAllSkillNames() {
        return getAllSkills().stream()
                .map(io.agentscope.core.skill.AgentSkill::getName)
                .toList();
    }

    @Override
    @SuppressWarnings("deprecation")
    public List<io.agentscope.core.skill.AgentSkill> getAllSkills() {
        try {
            fileCache.refreshAllEnabled();
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to refresh skills file cache", ex);
        }
        List<io.agentscope.core.skill.AgentSkill> skills = new ArrayList<>();
        for (ClawSkillEntity entity : enabledEntities()) {
            io.agentscope.core.skill.AgentSkill skill = loadSkill(entity);
            if (skill != null) {
                cacheSkill(skill);
                skills.add(skill);
            }
        }
        return skills;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean save(List<io.agentscope.core.skill.AgentSkill> skills, boolean overwrite) {
        throw new UnsupportedOperationException(
                "Use POST /api/skills/install to add skills; repository is read-only");
    }

    @Override
    public boolean delete(String name) {
        throw new UnsupportedOperationException(
                "Use skill management API; repository is read-only");
    }

    @Override
    public boolean skillExists(String name) {
        return getSkill(name) != null;
    }

    @Override
    public AgentSkillRepositoryInfo getRepositoryInfo() {
        return new AgentSkillRepositoryInfo(SOURCE, "database-backed cached filesystem skills", writeable);
    }

    @Override
    public String getSource() {
        return SOURCE;
    }

    @Override
    public void setWriteable(boolean writeable) {
        this.writeable = writeable;
    }

    @Override
    public boolean isWriteable() {
        return writeable;
    }

    private List<ClawSkillEntity> enabledEntities() {
        return skillMapper.selectList(new LambdaQueryWrapper<ClawSkillEntity>()
                .eq(ClawSkillEntity::getEnabled, true)
                .orderByDesc(ClawSkillEntity::getUpdatedAt));
    }

    @SuppressWarnings("deprecation")
    private io.agentscope.core.skill.AgentSkill loadSkill(ClawSkillEntity entity) {
        try {
            Path dir = fileCache.ensureCached(entity);
            if (dir == null || !dir.toFile().isDirectory()) {
                return null;
            }
            return SkillFileSystemHelper.loadSkillFromDirectory(dir, SOURCE, false);
        } catch (IOException ex) {
            log.warn("Failed to load skill {}: {}", entity.getSlug(), ex.getMessage());
            return null;
        } catch (RuntimeException ex) {
            log.warn("Failed to load skill {} from cache: {}", entity.getSlug(), ex.getMessage());
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    private void cacheSkill(io.agentscope.core.skill.AgentSkill skill) {
        memoryCache.putIfAbsent(skill.getName(), skill);
        skill.getOriginDir()
                .ifPresent(path -> memoryCache.putIfAbsent(path.getFileName().toString(), skill));
    }
}
