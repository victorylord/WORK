package com.panda.sport.rcs.mapper.sub;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfigSub;
import com.panda.sport.rcs.pojo.RcsMatchMarketMarginConfig;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.config.MarketBuildConfig;
import com.panda.sport.rcs.pojo.config.MarketBuildPlayConfig;
import com.panda.sport.rcs.pojo.dto.odds.MatchMarketPlaceConfig;
import com.panda.sport.rcs.pojo.dto.odds.RcsStandardMarketDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>
 * 赛事设置表 Mapper 接口
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-24
 */
@Component
public interface RcsMatchMarketConfigSubMapper extends BaseMapper<RcsMatchMarketConfigSub> {

    /**
     * @Description   //根据玩法id查询子玩法对应的配置
     * @Param [config]
     * @Author  sean
     * @Date   2021/7/6
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketConfig
     **/
    RcsMatchMarketConfig queryMatchMarketConfigSub(@Param("config")RcsMatchMarketConfig config);
    /**
     * @Description   //新增或者修改配置
     * @Param [conf]
     * @Author  sean
     * @Date   2021/7/6
     * @return void
     **/
    int insertOrUpdateMarketConfig(@Param("config")RcsMatchMarketConfig conf);
    /**
     * @Description   //根据玩法id查询子玩法对应的配置
     * @Param [config]
     * @Author  sean
     * @Date   2021/8/1
     * @return java.util.List<com.panda.sport.rcs.pojo.RcsMatchMarketConfigSub>
     **/
    List<RcsMatchMarketConfigSub> queryMatchMarketConfigSubList(@Param("config")RcsMatchMarketConfig config);


    /**
     * @Date 2021/8/14
     **/
    Integer updateMatchMarketConfigSubByMatch(@Param("match") StandardSportMarket market);

    /**
     * @Date 2021/8/14
     **/
    Integer updatePlaceSubConfig(@Param("config") RcsMatchMarketConfig config);
}
