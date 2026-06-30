package icu.jiapeng.spicyclaw.agent.dto;

/**
 * Agent 智能体（专家）配置视图。
 */
public record AgentResponse(
        String id,
        String name,
        String description,
        String sysPrompt,
        int maxIters,
        String defaultModelSlug,
        boolean enabled) {
}
