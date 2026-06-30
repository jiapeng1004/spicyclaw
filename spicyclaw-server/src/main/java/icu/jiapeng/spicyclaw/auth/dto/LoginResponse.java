package icu.jiapeng.spicyclaw.auth.dto;

/**
 * 登录成功响应，包含用户信息与 JWT。
 *
 * @param id           用户 UUID
 * @param username     用户名
 * @param displayName  展示名称
 * @param accessToken  JWT 访问令牌
 * @param tokenType    令牌类型，固定为 Bearer
 * @param expiresIn    有效时长（秒）
 */
public record LoginResponse(
        String id,
        String username,
        String displayName,
        String accessToken,
        String tokenType,
        long expiresIn) {
}
