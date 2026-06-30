package icu.jiapeng.spicyclaw.model;

import icu.jiapeng.spicyclaw.model.entity.ClawModelEntity;
import io.agentscope.core.model.DashScopeChatModel;
import io.agentscope.core.model.Model;
import io.agentscope.core.model.OpenAIChatModel;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 根据数据库中的 {@link ClawModelEntity} 构建 AgentScope {@link Model} 实例。
 */
@Component
public class ClawModelFactory {

    public Model create(ClawModelEntity entity) {
        String provider = entity.getProvider().toLowerCase();
        return switch (provider) {
            case "dashscope" -> buildDashScope(entity);
            case "openai" -> buildOpenAi(entity);
            default -> throw new IllegalArgumentException("Unsupported model provider: " + entity.getProvider());
        };
    }

    private Model buildDashScope(ClawModelEntity entity) {
        var builder = DashScopeChatModel.builder()
                .apiKey(requireKey(entity.getApiKey()))
                .modelName(entity.getModelName())
                .stream(Boolean.TRUE.equals(entity.getStream()));
        if (entity.getEnableThinking() != null) {
            builder.enableThinking(entity.getEnableThinking());
        }
        if (StringUtils.hasText(entity.getBaseUrl())) {
            builder.baseUrl(entity.getBaseUrl());
        }
        return builder.build();
    }

    private Model buildOpenAi(ClawModelEntity entity) {
        var builder = OpenAIChatModel.builder()
                .apiKey(requireKey(entity.getApiKey()))
                .modelName(entity.getModelName())
                .stream(Boolean.TRUE.equals(entity.getStream()));
        if (StringUtils.hasText(entity.getBaseUrl())) {
            builder.baseUrl(entity.getBaseUrl());
        }
        return builder.build();
    }

    private static String requireKey(String apiKey) {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("Model apiKey is not configured");
        }
        return apiKey;
    }
}
