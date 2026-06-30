package icu.jiapeng.spicyclaw.skill.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * 已安装技能实体，映射数据库表 {@code claw_skill}。
 * <p>
 * DB 为清单与启停权威来源；{@code path} 为持久化存储目录（非运行时 skillsDir 缓存）。
 */
@Data
@TableName(value = "claw_skill", autoResultMap = true)
public class ClawSkillEntity {

    /**
     * 技能记录主键，MyBatis-Plus 自动生成 UUID。
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 技能唯一标识，通常为技能目录名（kebab-case）。
     */
    private String slug;

    /**
     * 展示名称，来自 SKILL.md YAML front matter 的 {@code name} 字段。
     */
    private String name;

    /**
     * 技能描述，来自 front matter 的 {@code description}。
     */
    private String description;

    /**
     * 安装来源，如 {@code local}（本地目录）、{@code zip}、{@code clawhub}。
     */
    private String source;

    /**
     * 技能在文件系统中的绝对或相对路径。
     */
    private String path;

    /**
     * 是否启用；禁用后 Agent 不会加载该技能。
     */
    private Boolean enabled;

    /**
     * SKILL.md front matter 中的扩展元数据（JSON 序列化存储）。
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> metadata;

    /**
     * 首次安装时间。
     */
    private OffsetDateTime createdAt;

    /**
     * 最近一次同步或更新配置的时间。
     */
    private OffsetDateTime updatedAt;
}
