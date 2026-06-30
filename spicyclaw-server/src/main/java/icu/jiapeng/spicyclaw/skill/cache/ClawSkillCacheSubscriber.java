package icu.jiapeng.spicyclaw.skill.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * 订阅 Redis 频道，在其他实例变更技能时刷新本机 skillsDir 缓存。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(RedisConnectionFactory.class)
public class ClawSkillCacheSubscriber implements MessageListener {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final ClawSkillCacheSync cacheSync;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            SkillCacheEvent event = JSON.readValue(message.getBody(), SkillCacheEvent.class);
            log.debug("Received skill cache event: {}", event);
            cacheSync.applyRemote(event);
        } catch (Exception ex) {
            log.warn("Failed to handle skill cache message: {}", new String(message.getBody()), ex);
        }
    }
}
