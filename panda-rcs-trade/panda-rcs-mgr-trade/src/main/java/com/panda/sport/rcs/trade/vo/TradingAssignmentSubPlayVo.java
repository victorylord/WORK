package com.panda.sport.rcs.trade.vo;

import com.panda.sport.rcs.pojo.RcsCategorySetTraderWeight;
import com.panda.sport.rcs.pojo.vo.TradingAssignmentDataVo;
import com.panda.sport.rcs.vo.RcsMarketCategorySetVo;
import com.panda.sports.api.vo.SysTraderVO;
import lombok.Data;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: xindaima
 * @description:指派数据
 * @author: kimi
 * @create: 2020-11-06 17:21
 **/
@Data
public class TradingAssignmentSubPlayVo {

    /*
    verison 0 玩法集id 1玩法id
     */
    private Long typeId;
    /**
     * 国际化
     */
    private Object setNames;


    List<RcsCategorySetTraderWeight> sysTraderWeightList;

}
