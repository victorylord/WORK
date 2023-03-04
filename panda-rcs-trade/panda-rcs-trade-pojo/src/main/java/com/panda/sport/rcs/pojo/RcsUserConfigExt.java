package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 用户特殊配置扩展
 *
 * @description:
 * @author: magic
 * @create: 2022-05-14 18:15
 **/
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class RcsUserConfigExt extends RcsBaseEntity<RcsUserConfigExt> {
    /**
     * 表ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 用户Id
     */
    private Long userId;

    /**
     * 赔率分组动态风控开关 0关 1开
     */
    private Integer tagMarketLevelStatus;

    /**
     * 商户编码
     */
    private String merchantCode;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建时间
     */
    private Date createTime;
}
