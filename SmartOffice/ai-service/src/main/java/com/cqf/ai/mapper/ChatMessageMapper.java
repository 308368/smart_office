package com.cqf.ai.mapper;

import com.cqf.ai.model.po.ChatMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cqf.ai.model.vo.ChatCount;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * AI对话消息表 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2026-04-17
 */
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS date, COUNT(*) AS count FROM chat_message WHERE role = 'user'  GROUP BY date ORDER BY date DESC")
    List<ChatCount> selectDailyChatCount();
}
