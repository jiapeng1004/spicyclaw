package icu.jiapeng.spicyclaw.chat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import icu.jiapeng.spicyclaw.chat.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}
