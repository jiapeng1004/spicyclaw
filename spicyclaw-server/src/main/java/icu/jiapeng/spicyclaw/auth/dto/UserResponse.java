package icu.jiapeng.spicyclaw.auth.dto;

/**
 * 当前登录用户视图。
 *
 * @param id          用户 UUID
 * @param username    用户名
 * @param displayName 展示名称
 */
public record UserResponse(String id, String username, String displayName) {
}
