package icu.jiapeng.spicyclaw.chat.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 创建聊天会话请求体。
 * <p>
 * 标题与模型均可选；未指定模型时使用数据库中的默认模型。
 *
 * @param title     会话标题，最长 256 字符
 * @param modelSlug 模型 slug，对应 {@code spicyclaw:{modelSlug}} 注册键
 */
public record CreateSessionRequest(
        @Size(max = 256, message = "会话标题不能超过256个字符")
        String title,
        @Size(max = 128, message = "modelSlug 不能超过128个字符")
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "modelSlug 只能包含小写字母、数字和连字符")
        String modelSlug) {

    /** 空请求体（全部使用默认值）时使用。 */
    public static CreateSessionRequest empty() {
        return new CreateSessionRequest(null, null);
    }
}
