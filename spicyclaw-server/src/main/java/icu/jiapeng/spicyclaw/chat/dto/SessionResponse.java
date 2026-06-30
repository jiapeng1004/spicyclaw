package icu.jiapeng.spicyclaw.chat.dto;

/**
 * 聊天会话摘要，用于列表与创建会话响应。
 *
 * @param id        会话 UUID
 * @param title     会话标题
 * @param agentName 绑定的 Agent 名称
 * @param modelRef  AgentScope 模型注册键，如 {@code spicyclaw:default}
 * @param createdAt 创建时间（ISO-8601 字符串）
 * @param updatedAt 最后更新时间（ISO-8601 字符串）
 */
public record SessionResponse(
        String id,
        String title,
        String agentName,
        String modelRef,
        String createdAt,
        String updatedAt) {
}
