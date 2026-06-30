package icu.jiapeng.spicyclaw.skill.dto;

/**
 * 已安装技能的 API 视图。
 *
 * @param id          技能记录 UUID
 * @param slug        技能唯一标识（目录名 / kebab-case）
 * @param name        展示名称（来自 SKILL.md front matter）
 * @param description 技能描述
 * @param source      来源，如 {@code local}、{@code clawhub}
 * @param path        技能在磁盘上的路径
 * @param enabled     是否对 Agent 可见
 */
public record SkillResponse(
        String id,
        String slug,
        String name,
        String description,
        String source,
        String path,
        boolean enabled) {
}
