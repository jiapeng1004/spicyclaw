package icu.jiapeng.spicyclaw.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 聊天会话实体，映射数据库表 {@code claw_session}。
 * <p>
 * 每个会话对应一个独立的 Agent 运行时实例（见 {@code ClawAgentSessionManager}）。
 */
@Data
@TableName("claw_session")
public class ChatSession {

    /**
     * 会话主键，MyBatis-Plus 自动生成 UUID。
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 会话标题，展示在侧边栏；默认 {@code New Chat}。
     */
    private String title;

    /**
     * 绑定的 Agent 名称，与 {@code agentscope.agent.name} 配置一致。
     */
    private String agentName;

    /**
     * AgentScope 模型注册键，如 {@code spicyclaw:default}；创建会话时写入，运行期不变。
     */
    private String modelRef;

    /**
     * 会话创建时间。
     */
    private OffsetDateTime createdAt;

    /**
     * 会话最后活跃时间（发消息或创建时更新）。
     */
    private OffsetDateTime updatedAt;
}
