package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsMatchMarketMarginConfig;
import com.panda.sport.rcs.pojo.RcsNewSportWaterConfig;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.config.MarketBuildConfig;
import com.panda.sport.rcs.pojo.config.MarketBuildPlayConfig;
import com.panda.sport.rcs.pojo.dto.ClearSubDTO;
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
public interface RcsNewSportWaterConfigMapper extends BaseMapper<RcsNewSportWaterConfig> {

//    void clearNewTableWaterDiff(List<ClearSubDTO> list);
//
//    void clearNewTableWaterDiffV2(StandardSportMarket bean);
}
