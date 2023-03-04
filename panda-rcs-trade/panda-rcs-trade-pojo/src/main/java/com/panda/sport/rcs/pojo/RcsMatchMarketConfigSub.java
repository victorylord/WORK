package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.panda.sport.rcs.log.annotion.format.LogFormatAnnotion;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description //盘口子玩法设置表
 * @Param
 * @Author sean
 * @Date 2021/7/6
 * @return
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@LogFormatAnnotion
public class RcsMatchMarketConfigSub extends RcsMatchMarketConfig {

    private static final long serialVersionUID = 1L;

    /**
     * 子玩法ID
     */
    @TableField("sub_play_id")
    private String subPlayId;
}
