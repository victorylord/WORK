package com.panda.sport.rcs.trade.wrapper;

import com.panda.sport.data.rcs.dto.ThreewayOverLoadTriggerItem;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsTournamentMarketConfig;
import com.panda.sport.rcs.vo.HttpResponse;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author :  Sean
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper
 * @Description :  联赛操盘服务类
 * @Date: 2019-10-23 16:28
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface RcsTournamentOperateMarketService {
    /**
     * @return com.panda.sport.rcs.pojo.RcsTournamentMarketConfig
     * @Description //查找数据
     * @Param [columnMap]
     * @Author kimi
     * @Date 2019/11/13
     **/
//    List<RcsTournamentMarketConfig> getRcsTournamentMarketConfig(Map<String, Object> columnMap);

    /**
     * @return void
     * @Description //修改联赛对应配置信息
     * @Param [config]
     * @Author kimi
     * @Date 2019/11/15
     **/
//    HttpResponse<RcsTournamentMarketConfig> updateGetTournamentConfig(RcsTournamentMarketConfig config);

    /**
     * 发送mq数据
     **/
    void sendRcsDataMq(Long tournamentId,String playId,String marketId,String match,String subPlayId,Long amount);

//    /**
//     * @Description   新增或者更新盘口水位差设置
//     * @Param [list]
//     * @Author  Sean
//     * @Date  16:25 2020/1/11
//     * @return void
//     **/
//    void saveAndUpdateMarketWaterHeadConfig(@Param("list") List<ThreewayOverLoadTriggerItem> list)  throws RcsServiceException;

    /**
     * @return void
     * @Description //TODO
     * @Param [list]
     * @Author kimi
     * @Date 2020/2/24
     **/
//    void saveAndUpdateMarketWaterHeadConfigs(@Param("list") List<ThreewayOverLoadTriggerItem> list) throws RcsServiceException;

//    /**
//     * @Description   查询盘口水位差设置
//     * @Param [RcsMatchMarketConfig]
//     * @Author  Sean
//     * @Date  12:00 2020/1/16
//     * @return java.util.List<com.panda.sport.data.rcs.dto.ThreewayOverLoadTriggerItem>
//     **/
//    Map<Integer,List<Map<String,Object>>> queryMarketWaterHeadConfig (ThreewayOverLoadTriggerItem item);


    /**
     * 查询联赛最大投注额
     * @param config
     * @return
     */
//    BigDecimal getMaxBetAmount(RcsMatchMarketConfig config);

}
