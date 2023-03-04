package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.MatchStatisticsInfo;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.vo.RedCardVo;

/**
 * @ClassName MatchStatisticsInfoService
 * @Description: TODO
 * @Author Vector
 * @Date 2019/10/11
 **/
public interface MatchStatisticsInfoService extends IService<MatchStatisticsInfo> {

    /**
     * @MethodName: getMatchInfoByMatchId
     * @Description: 获取赛事基本数据
     * @Param:
     * @Return:
     **/
    MatchStatisticsInfo getMatchInfoByMatchId(Long id);


    RedCardVo selectRedCardNum(Long standardMatchId);
    /**
     * @Description   //根据玩法获取当前比分
     * @Param [RcsMatchMarketConfig]
     * @Author  sean
     * @Date   2021/2/7
     * @return java.lang.Integer
     **/
    String queryCurrentScoreByPlayId(RcsMatchMarketConfig config);
}
