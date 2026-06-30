package icu.jiapeng.spicyclaw.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * 创建 LLM 模型配置请求体。
 *
 * @param slug            唯一标识，生成注册键 {@code spicyclaw:{slug}}
 * @param displayName     展示名称
 * @param provider        提供商，如 {@code dashscope}、{@code openai}
 * @param modelName       提供商模型名
 * @param apiKey          API Key
 * @param baseUrl         自定义 API 基址，可选
 * @param stream          是否流式，默认 {@code true}
 * @param enableThinking  是否启用思考模式，可选
 * @param extraOptions    扩展参数，可选
 * @param isDefault       是否设为默认模型
 * @param enabled         是否启用，默认 {@code true}
 */
public record CreateModelRequest(
        @NotBlank(message = "slug 不能为空")
        @Size(max = 128, message = "slug 不能超过128个字符")
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "slug 只能包含小写字母、数字和连字符")
        String slug,
        @NotBlank(message = "displayName 不能为空")
        @Size(max = 256, message = "displayName 不能超过256个字符")
        String displayName,
        @NotBlank(message = "provider 不能为空")
        @Size(max = 64, message = "provider 不能超过64个字符")
        String provider,
        @NotBlank(message = "modelName 不能为空")
        @Size(max = 128, message = "modelName 不能超过128个字符")
        String modelName,
        @NotBlank(message = "apiKey 不能为空")
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
