package icu.jiapeng.spicyclaw.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 发送聊天消息请求体（SSE 流式回复）。
 *
 * @param content 用户输入的消息正文
 */
public record SendMessageRequest(
        @NotBlank(message = "消息内容不能为空")
        @Size(max = 65535, message = "消息内容不能超过65535个字符")
        String content) {
}
