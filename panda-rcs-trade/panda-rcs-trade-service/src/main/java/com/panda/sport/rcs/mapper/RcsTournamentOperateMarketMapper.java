package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.data.rcs.dto.ExtendBean;
import com.panda.sport.data.rcs.dto.ThreewayOverLoadTriggerItem;
import com.panda.sport.rcs.pojo.RcsBusinessSingleBetConfig;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsTournamentMarketConfig;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author :  Sean
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mapper
 * @Description :  联赛操盘赔付数据库操作接口类
 * @Date: 2019-10-23 16:46
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public interface RcsTournamentOperateMarketMapper extends BaseMapper<RcsTournamentMarketConfig> {
    /**
     * @Description   查询赛事和联赛最大投注额
     * @Param [order]
     * @Author  Sean
     * @Date  16:47 2019/12/6
     * @return com.panda.sport.rcs.pojo.RcsTournamentMarketConfig
     **/
    RcsTournamentMarketConfig queryMatchAndTournamentMaxBetAmount(@Param("order")ExtendBean order);

    void saveAndUpdateMarketWaterHeadConfig(@Param("list") List<ThreewayOverLoadTriggerItem> list);
//    /**
//     * @Description   根据玩法阶段和赛事查询水差设置
//     * @Param [item]
//     * @Author  Sean
//     * @Date  12:02 2020/1/18
//     * @return java.util.List<com.panda.sport.data.rcs.dto.ThreewayOverLoadTriggerItem>
//     **/
//    List<ThreewayOverLoadTriggerItem> queryMarketWaterHeadConfig(@Param("item") ThreewayOverLoadTriggerItem item);
    /**
     * @Description   根据赛事id查询所有盘口和赔率信息
     * @Param [item]
     * @Author  Sean
     * @Date  12:03 2020/1/18
     * @return java.util.List<com.panda.sport.data.rcs.dto.ThreewayOverLoadTriggerItem>
     **/
    List<ThreewayOverLoadTriggerItem> queryMarketOddsList(@Param("item") ThreewayOverLoadTriggerItem item);
    /**
     * @Description   根据赛事查询显示滚球盘口数
     * @Param [item]
     * @Author  Sean
     * @Date  18:29 2020/1/21
     * @return java.lang.Long
     **/
    Long queryMarketCount(@Param("item") ThreewayOverLoadTriggerItem item);
    /**
     * @Description   //新增和修改联赛配置
     * @Param [config]
     * @Author  Sean
     * @Date  11:42 2020/8/5
     * @return int
     **/
    int insertOrUpdateTournamentConfig(@Param("config") RcsTournamentMarketConfig config);
    /**
     * @Description   //查出商户配置的限额
     * @Param [config]
     * @Author  Sean
     * @Date  15:15 2020/8/28
     * @return com.panda.sport.rcs.pojo.RcsBusinessSingleBetConfig
     **/
    RcsBusinessSingleBetConfig queryBusinessBetMaxAmount(@Param("config") RcsMatchMarketConfig config);
}
