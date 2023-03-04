package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.log.annotion.format.LogFormatAnnotion;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@LogFormatAnnotion
public class RcsNewSportWaterConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 赛事id
     */
    private Long matchId;
    /**
     * 玩法id
     */
    private Long playId;

    /**
     *  子玩法id
     */
    private String subPlayId;

    /**
     * 盘口配置位置
     */
    private Integer marketIndex;

    private  String awayAutoChangeRate;

}
