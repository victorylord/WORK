package com.panda.sport.rcs.pojo.dto;

import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import com.panda.sport.rcs.pojo.RcsBusinessPlayPaidConfig;
import lombok.Data;

import java.io.Serializable;

/**
 * @author :  vector 2.3
 * @Project Name :  data-realtime
 * @Package Name :  com.panda.sport.data.realtime.api.message
 * @Description :  TODO
 * @Date: 2019-10-07 17:10
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class StatisticsNotifyDTO extends RcsBaseEntity<StatisticsNotifyDTO> {
    /**
     * 赛事id
     */
    private Long standardMatchId;

    /**
     * 从哪个通首来
     * -1 表示只推送时间
     */
    private Integer channel;
    
    private Integer secondsFromStart;

}
