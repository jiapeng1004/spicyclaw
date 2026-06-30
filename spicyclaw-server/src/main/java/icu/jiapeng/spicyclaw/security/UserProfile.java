package icu.jiapeng.spicyclaw.security;

/**
 * 对外暴露的用户概要（Spring Modulith API 类型，供 auth 等模块使用）。
 */
public record UserProfile(String id, String username, String displayName) {
}
