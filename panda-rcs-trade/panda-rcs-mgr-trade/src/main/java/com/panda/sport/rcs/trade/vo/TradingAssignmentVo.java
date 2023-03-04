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
public class TradingAssignmentVo {
    /**
     * 玩法集列表
     */
    List<RcsMarketCategorySetVo> rcsMarketCategorySetVoHashMap;
    /**
     * 指派数据
     */
    Collection<TradingAssignmentDataVo> stringTradingAssignmentDataVoHashMap;
    /**
     * 玩家数据
     */
    HashMap<String, SysTraderVO> stringSysTraderVOHashMap;


    private Long setNo;

    private Long typeId;

    /**
     * 国际化
     */
    private Map setNames;

    /**
     * 赛事id
     */
    private Long matchId;

    /**
     * 运动种类id。 对应表 sport.id
     */
    private Long sportId;

    /**
     * 0:滚球 1:早盘
     */
    private Integer marketType;

    List<TradingAssignmentSubPlayVo> sysTraderWeightList;

}
