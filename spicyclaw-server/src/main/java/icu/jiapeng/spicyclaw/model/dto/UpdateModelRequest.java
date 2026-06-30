package icu.jiapeng.spicyclaw.model.dto;

import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * 更新 LLM 模型配置请求体；字段均为可选，仅更新传入的非空值。
 *
 * @param displayName     展示名称
 * @param provider        提供商
 * @param modelName       提供商模型名
 * @param apiKey          API Key
 * @param baseUrl         自定义 API 基址
 * @param stream          是否流式
 * @param enableThinking  是否启用思考模式
 * @param extraOptions    扩展参数
 * @param isDefault       是否设为默认模型
 * @param enabled         是否启用
 */
public record UpdateModelRequest(
        @Size(max = 256, message = "displayName 不能超过256个字符")
        String displayName,
        @Size(max = 64, message = "provider 不能超过64个字符")
        String provider,
        @Size(max = 128, message = "modelName 不能超过128个字符")
        String modelName,
        @Size(max = 512, message = "apiKey 不能超过512个字符")
        String apiKey,
        @Size(max = 512, message = "baseUrl 不能超过512个字符")
        String baseUrl,
        Boolean stream,
        Boolean enableThinking,
        Map<String, Object> extraOptions,
        Boolean isDefault,
        Boolean enabled) {
}
