package icu.jiapeng.spicyclaw.model.dto;

import java.util.Map;

/**
 * LLM 模型配置 API 视图。
 *
 * @param id              配置 UUID
 * @param slug            唯一标识
 * @param registryKey     AgentScope 注册键，如 {@code spicyclaw:default}
 * @param displayName     展示名称
 * @param provider        提供商
 * @param modelName       提供商模型名
 * @param apiKeyMasked    脱敏后的 API Key
 * @param baseUrl         自定义 API 基址
 * @param stream          是否流式
 * @param enableThinking  是否启用思考模式
 * @param extraOptions    扩展参数
 * @param isDefault       是否为默认模型
 * @param enabled         是否启用
 * @param createdAt       创建时间（ISO-8601）
 * @param updatedAt       更新时间（ISO-8601）
 */
public record ModelResponse(
        String id,
        String slug,
        String registryKey,
        String displayName,
        String provider,
        String modelName,
        String apiKeyMasked,
        String baseUrl,
        boolean stream,
        Boolean enableThinking,
        Map<String, Object> extraOptions,
        boolean isDefault,
        boolean enabled,
        String createdAt,
        String updatedAt) {
}
