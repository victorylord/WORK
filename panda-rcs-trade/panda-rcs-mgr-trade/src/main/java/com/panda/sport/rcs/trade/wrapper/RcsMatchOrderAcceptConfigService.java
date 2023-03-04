package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsMatchOrderAcceptConfig;
import com.panda.sport.rcs.pojo.RcsTournamentOrderAcceptConfig;

import java.util.List;


public interface RcsMatchOrderAcceptConfigService extends IService<RcsMatchOrderAcceptConfig> {


    int updateBatch(List<RcsMatchOrderAcceptConfig> list);

    int batchInsert(List<RcsMatchOrderAcceptConfig> list);

    int insertOrUpdate(RcsMatchOrderAcceptConfig record);

    int insertOrUpdateSelective(RcsMatchOrderAcceptConfig record);

    /**
     * @return com.panda.sport.rcs.pojo.RcsMatchOrderAcceptConfig
     * @Description //TODO
     * @Param [matchId]
     * @Author kimi
     * @Date 2020/2/1
     **/
    RcsMatchOrderAcceptConfig selectRcsMatchOrderAcceptConfigById(Long matchId);

    /**
     * @return com.panda.sport.rcs.pojo.RcsMatchOrderAcceptConfig
     * @Description //初始化赛事数据
     * @Param [matchId]
     * @Author kimi
     * @Date 2020/2/1
     **/
    RcsMatchOrderAcceptConfig init(Long matchId);

    /**
     * @return com.panda.sport.rcs.pojo.RcsMatchOrderAcceptConfig
     * @Description //根据联赛数据初始化赛事配置数据
     * @Param [rcsTournamentOrderAcceptConfig]
     * @Author kimi
     * @Date 2020/2/1
     **/
    RcsMatchOrderAcceptConfig init(RcsTournamentOrderAcceptConfig rcsTournamentOrderAcceptConfig, Long matchId);

    /**
     * @return void
     * @Description //更新数据
     * @Param [rcsMatchOrderAcceptConfig]
     * @Author kimi
     * @Date 2020/2/1
     **/
    void updateRcsMatchOrderAcceptConfig(RcsMatchOrderAcceptConfig rcsMatchOrderAcceptConfig);
}
