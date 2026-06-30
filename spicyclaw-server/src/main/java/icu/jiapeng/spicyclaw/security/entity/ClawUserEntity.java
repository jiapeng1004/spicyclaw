package icu.jiapeng.spicyclaw.security.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 应用用户实体，映射表 {@code claw_user}，供 Spring Security {@code UserDetailsService} 加载。
 */
@Data
@TableName("claw_user")
public class ClawUserEntity {

    /** 用户主键 UUID。 */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 登录用户名，全局唯一。 */
    private String username;

    /** BCrypt 编码后的密码，不可明文存储。 */
    private String passwordHash;

    /** 展示名称。 */
    private String displayName;

    /** 是否启用；禁用后无法登录。 */
    private Boolean enabled;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
