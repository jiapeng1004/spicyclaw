package icu.jiapeng.spicyclaw.skill.cache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@ConditionalOnBean(RedisConnectionFactory.class)
public class ClawSkillRedisConfig {

    @Bean(name = "skillCacheRedisListenerContainer")
    RedisMessageListenerContainer skillCacheRedisListenerContainer(
            RedisConnectionFactory connectionFactory,
            ClawSkillCacheSubscriber subscriber) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(subscriber, new ChannelTopic(ClawSkillCacheSync.REDIS_CHANNEL));
        return container;
    }
}
