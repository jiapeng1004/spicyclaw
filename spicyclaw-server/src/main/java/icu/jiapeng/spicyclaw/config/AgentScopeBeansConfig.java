package icu.jiapeng.spicyclaw.config;

import io.agentscope.core.state.AgentStateStore;
import io.agentscope.core.state.InMemoryAgentStateStore;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.spring.boot.properties.AgentscopeProperties;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * AgentScope 2.0 beans.
 *
 * <p>Conversation state is managed via {@link AgentStateStore}, not the deprecated {@code Memory}
 * interface. Configure {@code stateStore} + {@code defaultSessionId} on the agent builder.
 */
@Configuration
@EnableConfigurationProperties(AgentscopeProperties.class)
public class AgentScopeBeansConfig {

    @Bean
    @ConditionalOnProperty(prefix = "agentscope.agent", name = "enabled", havingValue = "true")
    public AgentStateStore agentStateStore() {
        return new InMemoryAgentStateStore();
    }

    @Bean
    @ConditionalOnProperty(prefix = "agentscope.agent", name = "enabled", havingValue = "true")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Toolkit agentscopeToolkit() {
        return new Toolkit();
    }
}
