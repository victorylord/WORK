package com.panda.sport.rcs.vo.trade;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Lists;
import com.panda.merge.dto.MarketPlaceDtlDTO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 位置状态信息
 * @Author : Paca
 * @Date : 2021-07-28 18:44
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class PlaceStatusInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 占位符玩法总开关封锁
     */
    private List<MarketPlaceDtlDTO> mainPlayStatusList;

    /**
     * 玩法位置状态
     */
    private List<MarketPlaceDtlDTO> placeList;

    public List<MarketPlaceDtlDTO> getAllPlaceDtoList() {
        List<MarketPlaceDtlDTO> placeDtoList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(placeList)) {
            placeDtoList.addAll(placeList);
        }
        if (CollectionUtils.isNotEmpty(mainPlayStatusList)) {
            placeDtoList.addAll(mainPlayStatusList);
        }
        return placeDtoList;
    }
}
