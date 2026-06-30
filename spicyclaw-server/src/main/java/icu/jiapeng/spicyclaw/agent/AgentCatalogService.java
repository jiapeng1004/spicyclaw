package icu.jiapeng.spicyclaw.agent;

import icu.jiapeng.spicyclaw.agent.dto.AgentResponse;
import icu.jiapeng.spicyclaw.model.ClawModelRegistryService;
import icu.jiapeng.spicyclaw.model.dto.ModelResponse;
import io.agentscope.spring.boot.properties.AgentscopeProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "agentscope.agent", name = "enabled", havingValue = "true")
public class AgentCatalogService {

    private final AgentscopeProperties agentscopeProperties;
    private final ClawModelRegistryService modelRegistryService;

    public List<AgentResponse> listAgents() {
        var agent = agentscopeProperties.getAgent();
        String defaultSlug = modelRegistryService.listModels().stream()
                .filter(ModelResponse::enabled)
                .filter(ModelResponse::isDefault)
                .map(ModelResponse::slug)
                .findFirst()
                .orElse(null);
        return List.of(new AgentResponse(
                "spicyclaw-default",
                agent.getName(),
                "默认 ReAct Agent 智能体，负责对话任务规划与执行。",
                agent.getSysPrompt(),
                agent.getMaxIters(),
                defaultSlug,
                true));
    }
}
