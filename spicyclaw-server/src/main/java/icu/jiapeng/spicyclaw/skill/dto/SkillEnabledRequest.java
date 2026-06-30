package icu.jiapeng.spicyclaw.skill.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 启用或禁用技能请求体。
 *
 * @param enabled {@code true} 启用，{@code false} 禁用
 */
public record SkillEnabledRequest(
        @NotNull(message = "enabled 不能为空")
        Boolean enabled) {
}
