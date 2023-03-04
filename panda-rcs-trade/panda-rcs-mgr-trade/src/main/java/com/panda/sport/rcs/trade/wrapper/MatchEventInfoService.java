package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.MatchEventInfo;
import com.panda.sport.rcs.pojo.dto.CheckMatchEndDTO;
import com.panda.sport.rcs.vo.CustomizedEventBeanVo;
import com.panda.sport.rcs.vo.MatchMarketLiveOddsVo;

import java.util.List;

/**
 * @ClassName MatchEventInfoService
 * @Description: TODO
 * @Author Vector
 * @Date 2019/10/10
 **/
public interface MatchEventInfoService extends IService<MatchEventInfo> {

    Integer getSencondStart(Long matchId);

    void selectRecentMatchEventInfo(MatchMarketLiveOddsVo marketLiveOddsVo);

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.MatchEventInfo>
     * @Description //TODO
     * @Param [matchId, dataSource]
     * @Author kimi
     * @Date 2020/7/21
     **/
    void checkErrorMatchEnd(CheckMatchEndDTO dto);

    List<CustomizedEventBeanVo> selectMatchEventInfoByMatchId(Long matchId, String dataSource, Long eventTime, List<Integer> eventTypes, Integer sort, Integer limit,List unFilterEvents);
}
