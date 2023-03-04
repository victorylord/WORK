package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author :  Enzo
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo
 * @Description :  赛事预警消息
 * @Date: 2020-09-16 16:25
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class RcsTraderMessage extends RcsBaseEntity<RcsTraderMessage> {

    /**
     * 表ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 消息ID
     */
    private Long messageId;
    /**
     * 操盘手ID
     */
    private String traderId;
    /**
     * 是否已读
     */
    private Integer isRead;
    /**
     * 备注
     */
    private String note;
    /**
     * 赛种ID
     */
    @TableField(exist = false)
    private Long sportId;
    /**
     * 0早盘 1滚球
     */
    private Integer matchStatus;

}
