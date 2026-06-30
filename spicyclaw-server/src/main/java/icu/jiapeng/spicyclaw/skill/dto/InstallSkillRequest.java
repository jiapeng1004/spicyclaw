package icu.jiapeng.spicyclaw.skill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 从本地路径安装技能请求体。
 * <p>
 * {@code path} 可为技能目录或 zip 压缩包绝对/相对路径。
 *
 * @param path 技能目录或 zip 文件路径
 */
public record InstallSkillRequest(
        @NotBlank(message = "技能路径不能为空")
        @Size(max = 2048, message = "技能路径不能超过2048个字符")
        String path) {
}
