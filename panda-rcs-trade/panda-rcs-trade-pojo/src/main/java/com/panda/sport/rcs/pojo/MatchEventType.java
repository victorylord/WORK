package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 赛事盘中事件类型表
 * </p>
 *
 * @author CodeGenerator
 * @since 2020-01-11
 */
@Data
public class MatchEventType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 第三方数据源提供的该事件id
     */
    private Integer sportId;

    /**
     * 事件编码
     */
    private String eventCode;

    /**
     * 事件描述
     */
    private String eventDescribe;

    /**
     * 事件中文名称
     */
    private String eventName;

    /**
     * 额外信息
     */
    private String extraInfo;

    private String remark;

    private Long createTime;

    private Long modifyTime;


}
