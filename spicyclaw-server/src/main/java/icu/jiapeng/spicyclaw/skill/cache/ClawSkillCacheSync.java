package icu.jiapeng.spicyclaw.skill.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import icu.jiapeng.spicyclaw.skill.ClawAgentSkillRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 协调本地文件缓存与 Redis Pub/Sub，使多实例 skillsDir 保持一致。
 */
@Slf4j
@Service
public class ClawSkillCacheSync {

    public static final String REDIS_CHANNEL = "spicyclaw:skill-cache";

    private static final ObjectMapper JSON = new ObjectMapper();

    private final ClawSkillFileCache fileCache;
    private final ClawAgentSkillRepository agentSkillRepository;
    private final ObjectProvider<StringRedisTemplate> redisTemplate;

    public ClawSkillCacheSync(
            ClawSkillFileCache fileCache,
            ClawAgentSkillRepository agentSkillRepository,
            ObjectProvider<StringRedisTemplate> redisTemplate) {
        this.fileCache = fileCache;
        this.agentSkillRepository = agentSkillRepository;
        this.redisTemplate = redisTemplate;
    }

    /** 本地应用并在有 Redis 时广播给其他实例。 */
    public void broadcast(SkillCacheEvent event) {
        try {
            applyLocal(event);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to apply skill cache event locally: " + event, ex);
        }
        redisTemplate.ifAvailable(template -> {
            try {
                template.convertAndSend(REDIS_CHANNEL, JSON.writeValueAsString(event));
            } catch (JsonProcessingException ex) {
                log.warn("Failed to publish skill cache event: {}", event, ex);
            }
        });
    }

    /** 仅本地应用（Redis 订阅端使用，避免回环广播）。 */
    public void applyRemote(SkillCacheEvent event) {
        try {
            applyLocal(event);
        } catch (IOException ex) {
            log.warn("Failed to apply remote skill cache event: {}", event, ex);
        }
    }

    private void applyLocal(SkillCacheEvent event) throws IOException {
        switch (event.type()) {
            case INVALIDATE_ALL -> fileCache.clearAll();
            case REFRESH -> fileCache.refreshFromStore(event.slug());
            case EVICT -> fileCache.evict(event.slug());
        }
        agentSkillRepository.clearCache();
    }
}
