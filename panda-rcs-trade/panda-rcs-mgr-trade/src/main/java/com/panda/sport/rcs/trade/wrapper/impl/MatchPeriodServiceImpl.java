package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.MatchPeriodMapper;
import com.panda.sport.rcs.pojo.MatchPeriod;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.MatchPeriodService;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName MatchPeriodServiceImpl
 * @Description: TODO
 * @Author Vector
 * @Date 2019/11/19
 **/
@Service
public class MatchPeriodServiceImpl extends ServiceImpl<MatchPeriodMapper, MatchPeriod> implements MatchPeriodService {

    @Resource
    private MatchPeriodMapper matchPeriodMapper;

    @Override
    public MatchPeriod getOne(Long macthId, Integer period) {
        QueryWrapper<MatchPeriod> wrapper = new QueryWrapper();
        wrapper.lambda().eq(MatchPeriod::getStandardMatchId, macthId);
        wrapper.lambda().eq(MatchPeriod::getPeriod, period);
        return matchPeriodMapper.selectOne(wrapper);
    }

    @Override
    public String selectRecentScoreTeam(Long macthId, String score) {
        String result = "";
        try {
            QueryWrapper<MatchPeriod> wrapper = new QueryWrapper();
            wrapper.lambda().eq(MatchPeriod::getStandardMatchId, macthId);
            wrapper.lambda().orderByDesc(true, MatchPeriod::getModifyTime);
            wrapper.select("limit 1");
            MatchPeriod matchPeriod = matchPeriodMapper.selectOne(wrapper);
            if (matchPeriod != null) {
                String periodScore = matchPeriod.getScore();
                if (score.equals(periodScore)) {
                    QueryWrapper<MatchPeriod> wrapper1 = new QueryWrapper();
                    wrapper1.lambda().eq(MatchPeriod::getStandardMatchId, macthId);
                    wrapper1.lambda().lt(MatchPeriod::getModifyTime, matchPeriod.getModifyTime());
                    wrapper1.lambda().orderByDesc(true, MatchPeriod::getModifyTime);
                    wrapper1.select("limit 1");
                    MatchPeriod period = matchPeriodMapper.selectOne(wrapper);
                    if(period!=null){
                        result = getName(periodScore, period.getScore());
                    }
                } else {
                    result = getName(score, periodScore);
                }
            }
        }catch (Exception e){
            log.error("获取最近比分队伍失败",e);
        }

        return result;
    }

    String getName(String score, String periodScore) {
        String result = "";
        String[] scoreSplit = score.split(":");
        String[] periodScoreSplit = periodScore.split(":");
        Integer score1 = Integer.parseInt(scoreSplit[0]);
        Integer score2 = Integer.parseInt(scoreSplit[1]);

        Integer periodScore1 = Integer.parseInt(periodScoreSplit[0]);
        Integer periodScore2 = Integer.parseInt(periodScoreSplit[1]);
        if (score1 > periodScore1) result = "home";
        if (score2 > periodScore2) result = "away";
        return result;
    }
}
