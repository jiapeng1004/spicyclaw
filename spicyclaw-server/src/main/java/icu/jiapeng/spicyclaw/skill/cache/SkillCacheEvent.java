package icu.jiapeng.spicyclaw.skill.cache;

/**
 * 技能文件缓存同步事件，经 Redis Pub/Sub 在实例间传播。
 *
 * @param type  缓存操作类型
 * @param slug  技能 slug；{@link Type#INVALIDATE_ALL} 时可空
 */
public record SkillCacheEvent(Type type, String slug) {

    public enum Type {
        /** 清空本地 skillsDir 缓存 */
        INVALIDATE_ALL,
        /** 从持久化目录刷新单个技能到 skillsDir */
        REFRESH,
        /** 从 skillsDir 移除单个技能 */
        EVICT
    }

    public static SkillCacheEvent invalidateAll() {
        return new SkillCacheEvent(Type.INVALIDATE_ALL, null);
    }

    public static SkillCacheEvent refresh(String slug) {
        return new SkillCacheEvent(Type.REFRESH, slug);
    }

    public static SkillCacheEvent evict(String slug) {
        return new SkillCacheEvent(Type.EVICT, slug);
    }
}
