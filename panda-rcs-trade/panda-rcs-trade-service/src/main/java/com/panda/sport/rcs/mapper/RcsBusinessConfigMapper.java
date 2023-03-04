package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.log.annotion.NotWriteLog;
import com.panda.sport.rcs.pojo.LanguageInternation;
import com.panda.sport.rcs.pojo.RcsBusinessConPlayConfig;
import com.panda.sport.rcs.pojo.RcsBusinessDayPaidConfig;
import com.panda.sport.rcs.pojo.RcsBusinessMatchPaidConfig;
import com.panda.sport.rcs.pojo.RcsBusinessPlayPaidConfig;
import com.panda.sport.rcs.pojo.RcsBusinessSingleBetConfig;
import com.panda.sport.rcs.pojo.RcsBusinessUserPaidConfig;
import com.panda.sport.rcs.pojo.StandardSportMarketCategory;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 多语言 Mapper 接口
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
@Repository
public interface RcsBusinessConfigMapper extends BaseMapper<LanguageInternation> {

	List<RcsBusinessDayPaidConfig> queryBusDayConifgList();

	List<RcsBusinessMatchPaidConfig> queryBusMatchConifgList();

	List<RcsBusinessPlayPaidConfig> queryBusPlayConifgList();

	List<RcsBusinessUserPaidConfig> queryBusUserConifgList();

	List<StandardSportMarketCategory> queryAllPlayList();

	List<RcsBusinessSingleBetConfig> queryBusSingleBetConfigList();

	List<RcsBusinessConPlayConfig> queryBusConPlayConifgList();

}
