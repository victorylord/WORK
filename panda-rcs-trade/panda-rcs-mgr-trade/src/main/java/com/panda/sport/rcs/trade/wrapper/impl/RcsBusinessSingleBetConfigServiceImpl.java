package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.common.NumberUtils;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.RcsBusinessSingleBetConfigMapper;
import com.panda.sport.rcs.pojo.RcsBusinessSingleBetConfig;
import com.panda.sport.rcs.pojo.StandardSportMarketCategory;
import com.panda.sport.rcs.pojo.StandardSportTournament;
import com.panda.sport.rcs.trade.wrapper.*;
import com.panda.sport.rcs.vo.TournamentVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.panda.sport.rcs.constants.RedisKeys.TIMEPERIOD_VALUE_CACHE;

/**
 * @author :  Administrator
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.impl
 * @Description :  TODO
 * @Date: 2019-11-22 15:04
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */

@Service
public class RcsBusinessSingleBetConfigServiceImpl extends ServiceImpl<RcsBusinessSingleBetConfigMapper, RcsBusinessSingleBetConfig> implements RcsBusinessSingleBetConfigService {

    @Autowired
    StandardSportTournamentService standardSportTournamentService;
    @Autowired
    private RcsCodeService rcsCodeService;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private RcsBusinessMatchPaidConfigService rcsBusinessMatchPaidConfigService;
    @Autowired
    private StandardSportMarketCategoryService standardSportMarketCategoryService;

    @Override
    public List<RcsBusinessSingleBetConfig> selectBusinessSingleBetConfigList(RcsBusinessSingleBetConfig businessSingleBetConfig) {
        QueryWrapper<RcsBusinessSingleBetConfig> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(RcsBusinessSingleBetConfig::getBusinessId, businessSingleBetConfig.getBusinessId());
        wrapper.lambda().eq(RcsBusinessSingleBetConfig::getSportId, businessSingleBetConfig.getSportId());
        wrapper.lambda().eq(RcsBusinessSingleBetConfig::getMatchType, businessSingleBetConfig.getMatchType());
        wrapper.lambda().eq(RcsBusinessSingleBetConfig::getTimePeriod, businessSingleBetConfig.getTimePeriod());
        //增加排序支持
        wrapper.orderByDesc("order_number");
        if (businessSingleBetConfig.getTournamentLevel() != null) {
            wrapper.lambda().eq(RcsBusinessSingleBetConfig::getTournamentLevel, businessSingleBetConfig.getTournamentLevel());
        }
        List<RcsBusinessSingleBetConfig> list = this.list(wrapper);
        if (list.size() <= 0) {
            initBusinessSingleBetConfig(businessSingleBetConfig);
            list = this.list(wrapper);
        }
        return list;
    }

    @Override
    public List<RcsBusinessSingleBetConfig> getCustomizedConfigList(int playId, int tournamentId) throws RcsServiceException {
        StandardSportTournament tournament = standardSportTournamentService.getById(tournamentId);
        if (tournament == null) {
            throw new RcsServiceException("没有找到该联赛的数据，数据同步存在问题");
        }
        QueryWrapper<RcsBusinessSingleBetConfig> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(RcsBusinessSingleBetConfig::getTournamentLevel, tournament.getTournamentLevel());
        wrapper.lambda().eq(RcsBusinessSingleBetConfig::getSportId, tournament.getSportId());
        wrapper.lambda().eq(RcsBusinessSingleBetConfig::getMatchType, Integer.valueOf(0));
        wrapper.lambda().eq(RcsBusinessSingleBetConfig::getBusinessId, Integer.valueOf(1));
        StandardSportMarketCategory cachedMarketCategoryById = standardSportMarketCategoryService.getCachedMarketCategoryById((long) playId);
        wrapper.lambda().eq(RcsBusinessSingleBetConfig::getTimePeriod, cachedMarketCategoryById.getTheirTime());
        wrapper.and(wp -> wp.eq("play_id", playId).or().eq("play_id", -1)).last("order by play_id desc");
        return this.list(wrapper);
    }

    @Override
    public void initBusinessSingleBetConfig(RcsBusinessSingleBetConfig businessSingleBetConfig) {
        List<RcsBusinessSingleBetConfig> list = new ArrayList<>();
        String s = redisClient.get(String.format(TIMEPERIOD_VALUE_CACHE, businessSingleBetConfig.getSportId(),businessSingleBetConfig.getTimePeriod()));
        if (StringUtils.isBlank(s)) {
            s = initTimePeriod(businessSingleBetConfig.getTimePeriod(),businessSingleBetConfig.getSportId());
        }
        String[] categories = s.split(",");
        //int[] categories = PlayTypeConstants.get(businessSingleBetConfig.getTimePeriod());
        try {
            long base = rcsCodeService.getRcsCodeList("rcsBusinesssSingleBet", "amount");
            if (categories != null && categories.length > 0) {
                for (String categoryId : categories) {
                    if (StringUtils.isNotBlank(categoryId)) {
                        TournamentVo tournamentVo = new TournamentVo();
                        tournamentVo.setSportId(businessSingleBetConfig.getSportId());
                        tournamentVo.setTournamentLevel("tournamentLevel");
                        List<TournamentVo> tournamentVos = rcsBusinessMatchPaidConfigService.selectTournaments(tournamentVo);
                        if (tournamentVos.size() > 0) {
                            tournamentVos.forEach(model -> {
                                RcsBusinessSingleBetConfig config = new RcsBusinessSingleBetConfig();
                                config.setBusinessId(businessSingleBetConfig.getBusinessId());
                                config.setMatchType(businessSingleBetConfig.getMatchType());
                                config.setTimePeriod(businessSingleBetConfig.getTimePeriod());
                                config.setSportId(businessSingleBetConfig.getSportId());
                                config.setPlayId(Integer.parseInt(categoryId));
                                config.setTournamentLevel(Integer.parseInt(model.getValue()));
                                Integer rate = getPercent(Integer.parseInt(model.getValue()));
                                config.setOrderMaxRate(NumberUtils.getBigDecimal(rate));
                                config.setOrderMaxValue(NumberUtils.getBigDecimal(base).multiply(NumberUtils.getBigDecimal(rate)).divide(new BigDecimal(100), 2, BigDecimal.ROUND_HALF_UP));
                                config.setStatus(1);
                                list.add(config);
                            });
                        }
                    }
                }
            }
            this.saveBatch(list);
        } catch (Exception e) {
            log.error("初始化失败", e);
        }
    }

    public Integer getPercent(Integer level) {
        if (level == 1 || level.equals(1)) {
            return 100;
        } else if (level == 2 || level.equals(2)) {
            return 80;
        } else if (level == 3 || level.equals(3)) {
            return 50;
        } else if (level == 4 || level.equals(4)) {
            return 40;
        } else if (level == 5 || level.equals(5)) {
            return 30;
        } else if (level == 6 || level.equals(6)) {
            return 20;
        } else if (level >= 7) {
            return 10;
        } else {
            return 10;
        }
    }

    public String initTimePeriod(Integer timePeriod,Long sportId) {
        //足球
        String values = "";
        if(sportId==1){
            switch (timePeriod) {
                case 1:
                    values = "17,18,19,21,22,23,24,29,-1";
                    break;
                case 2:
                    values = "25,26,-1";
                    break;
                case 3:
                    values = "1,2,3,4,6,7,15,-1";
                    break;
                case 5:
                    values = "32,33,34,-1";
                    break;
                default:
                    values = "";
                    break;
            }
        }else if (sportId==2){
            switch (timePeriod) {
                case 1:
                    values = "-1";
                    break;
                case 2:
                    values = "-1";
                    break;
                case 4:
                    values = "-1";
                    break;
                default:
                    values = "";
                    break;
            }
        }

        redisClient.set(String.format(TIMEPERIOD_VALUE_CACHE,sportId, timePeriod), values);
        return values;
    }
}


