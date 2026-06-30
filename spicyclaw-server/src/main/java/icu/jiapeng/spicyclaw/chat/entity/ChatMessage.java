package icu.jiapeng.spicyclaw.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 聊天消息实体，映射数据库表 {@code claw_message}。
 * <p>
 * 消息与会话为一对多关系；删除会话时级联删除消息。
 */
@Data
@TableName("claw_message")
public class ChatMessage {

    /**
     * 消息主键，MyBatis-Plus 自动生成 UUID。
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 所属会话 ID，外键关联 {@code claw_session.id}。
     */
    private String sessionId;

    /**
     * 消息角色，如 {@code user}、{@code assistant}、{@code system}。
     */
    private String role;

    /**
     * 消息正文；用户输入或 Agent 回复的完整文本。
     */
    private String content;

    /**
     * 消息写入时间。
     */
    private OffsetDateTime createdAt;
}
