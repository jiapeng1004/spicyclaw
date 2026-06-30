package icu.jiapeng.spicyclaw.agent;

import icu.jiapeng.spicyclaw.agent.dto.AgentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/agents")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "agentscope.agent", name = "enabled", havingValue = "true")
public class AgentController {

    private final AgentCatalogService agentCatalogService;

    @GetMapping
    public List<AgentResponse> list() {
        return agentCatalogService.listAgents();
    }
}
