package icu.jiapeng.spicyclaw.chat.dto;

/**
 * 单条聊天消息，用于历史消息列表。
 *
 * @param id        消息 UUID
 * @param sessionId 所属会话 UUID
 * @param role      消息角色，如 {@code user}、{@code assistant}
 * @param content   消息正文
 * @param createdAt 创建时间（ISO-8601 字符串）
 */
public record MessageResponse(
        String id,
        String sessionId,
        String role,
        String content,
        String createdAt) {
}
