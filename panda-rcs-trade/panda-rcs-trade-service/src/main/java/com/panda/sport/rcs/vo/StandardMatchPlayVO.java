package com.panda.sport.rcs.vo;

import com.panda.merge.dto.StandardMatchMarketDTO;
import lombok.Data;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.vo
 * @Description : 标准玩法信息
 * @Author : Paca
 * @Date : 2020-10-04 20:29
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class StandardMatchPlayVO extends StandardMatchMarketDTO {

    /**
     * 玩法ID
     */
    private Long playId;

    /**
     * 盘口位置
     */
    private Integer marketPlaceNum;

    /**
     * 盘口状态
     */
    private Integer marketStatus;

}
