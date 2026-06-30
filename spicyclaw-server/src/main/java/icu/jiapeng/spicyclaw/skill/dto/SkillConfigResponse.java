package icu.jiapeng.spicyclaw.skill.dto;

/**
 * 技能模块运行时配置，供前端展示目录与 ClawHub 连接信息。
 *
 * @param skillsCacheDir  运行时文件缓存目录（启动清空，按需刷新）
 * @param skillsStoreDir  持久化存储目录（安装落盘）
 * @param clawhubUrl      ClawHub 注册表地址
 * @param clawhubEnabled  是否启用 ClawHub 远程安装
 */
public record SkillConfigResponse(
        String skillsCacheDir,
        String skillsStoreDir,
        String clawhubUrl,
        boolean clawhubEnabled) {
}
