package icu.jiapeng.spicyclaw.agent;

import icu.jiapeng.spicyclaw.skill.ClawAgentSkillRepository;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.spring.boot.properties.AgentscopeProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "agentscope.agent", name = "enabled", havingValue = "true")
public class ClawAgentFactory {

    private final AgentStateStore stateStore;
    private final ObjectProvider<Toolkit> toolkitProvider;
    private final AgentscopeProperties agentscopeProperties;
    private final ClawAgentSkillRepository skillRepository;

    /**
     * 创建绑定指定 ModelRegistry 键的会话 Agent。
     *
     * @param sessionId 会话 ID，用作 AgentStateStore 分区键
     * @param modelRef  模型注册键，如 {@code spicyclaw:default}
     */
    public ReActAgent createSessionAgent(String sessionId, String modelRef) {
        Toolkit toolkit = toolkitProvider.getObject();
        var agentConfig = agentscopeProperties.getAgent();
        return ReActAgent.builder()
                .name(agentConfig.getName())
                .sysPrompt(agentConfig.getSysPrompt())
                .model(modelRef)
                .toolkit(toolkit)
                .skillRepository(skillRepository)
                .dynamicSkillsEnabled(true)
                .maxIters(agentConfig.getMaxIters())
                .stateStore(stateStore)
                .defaultSessionId(sessionId)
                .build();
    }
}
