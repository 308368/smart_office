package com.cqf.office.model.po;

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
 * 通知公告表
 * </p>
 *
 * @author author
 * @since 2026-04-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("of_notice")
public class OfNotice implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 公告类型 1通知 2公告
     */
    private Integer noticeType;

    /**
     * 优先级 1高 2普通
     */
    private Integer priority;

    /**
     * 发布状态 0草稿 1已发布
     */
    private Integer publishStatus;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 发布人ID
     */
    private Long publisherId;

    /**
     * 发布人姓名
     */
    private String publisherName;

    /**
     * 阅读次数
     */
    private Integer viewCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除 0否 1是
     */
    private Integer deleted;


}
