package com.cqf.ticket.model.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 工单处理记录表
 * </p>
 *
 * @author author
 * @since 2026-04-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tk_ticket_reply")
public class TkTicketReply implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 工单ID
     */
    private Long ticketId;

    /**
     * 回复内容
     */
    private String content;

    /**
     * 回复类型 1用户回复 2客服回复 3系统回复
     */
    private Integer replyType;

    /**
     * 回复人ID
     */
    private Long userId;

    /**
     * 回复人姓名
     */
    private String userName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;


}
