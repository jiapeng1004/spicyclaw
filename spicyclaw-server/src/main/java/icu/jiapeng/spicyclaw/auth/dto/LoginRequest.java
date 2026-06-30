package icu.jiapeng.spicyclaw.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 用户登录请求体。
 *
 * @param username 用户名
 * @param password 明文密码
 */
public record LoginRequest(
        @NotBlank(message = "username 不能为空")
        @Size(max = 64, message = "username 不能超过64个字符")
        String username,
        @NotBlank(message = "password 不能为空")
        @Size(max = 128, message = "password 不能超过128个字符")
        String password) {
}
