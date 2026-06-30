package icu.jiapeng.spicyclaw.connector.dto;

/**
 * MCP 连接器视图。
 */
public record McpConnectorResponse(
        String id,
        String name,
        String transport,
        String endpoint,
        String description,
        boolean enabled,
        String status) {
}
