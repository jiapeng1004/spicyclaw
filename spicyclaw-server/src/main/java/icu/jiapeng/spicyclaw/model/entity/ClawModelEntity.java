package icu.jiapeng.spicyclaw.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * LLM 模型配置实体，映射数据库表 {@code claw_llm_model}。
 * <p>
 * 运行时通过 {@code spicyclaw:{slug}} 注册到 AgentScope {@link io.agentscope.core.model.ModelRegistry}，
 * 供 {@code ReActAgent.builder().model(...)} 动态解析。
 */
@Data
@TableName(value = "claw_llm_model", autoResultMap = true)
public class ClawModelEntity {

    /**
     * 配置主键，MyBatis-Plus 自动生成 UUID。
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 唯一标识，用于生成注册键 {@code spicyclaw:{slug}}。
     */
    private String slug;

    /**
     * 前端展示名称。
     */
    private String displayName;

    /**
     * 模型提供商，如 {@code dashscope}、{@code openai}。
     */
    private String provider;

    /**
     * 提供商侧模型名，如 {@code qwen-plus}。
     */
    private String modelName;

    /**
     * API Key（生产环境建议加密存储）。
     */
    private String apiKey;

    /**
     * 自定义 API 基址，OpenAI 兼容端点等场景使用。
     */
    private String baseUrl;

    /**
     * 是否启用流式输出。
     */
    private Boolean stream;

    /**
     * 是否启用思考模式（DashScope 等提供商）。
     */
    private Boolean enableThinking;

    /**
     * 扩展生成参数（temperature、maxTokens 等），JSON 存储。
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extraOptions;

    /**
     * 是否为系统默认模型；全局至多一个为 {@code true}。
     */
    private Boolean isDefault;

    /**
     * 是否启用；禁用后无法被新会话选用。
     */
    private Boolean enabled;

    /**
     * 创建时间。
     */
    private OffsetDateTime createdAt;

    /**
     * 最后更新时间。
     */
    private OffsetDateTime updatedAt;

    /**
     * 返回 AgentScope ModelRegistry 注册键。
     */
    public String registryKey() {
        return "spicyclaw:" + slug;
    }
}
