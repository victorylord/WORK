package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.enums.ScoreTypeEnum;
import com.panda.sport.rcs.mapper.statistics.MatchStatisticsInfoDetailMapper;
import com.panda.sport.rcs.pojo.MatchStatisticsInfoDetail;
import com.panda.sport.rcs.trade.wrapper.MatchStatisticsInfoDetailService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MatchStatisticsInfoDetailServiceImpl extends ServiceImpl<MatchStatisticsInfoDetailMapper, MatchStatisticsInfoDetail> implements MatchStatisticsInfoDetailService {

    @Override
    public MatchStatisticsInfoDetail slectRedScore(Long matchId) {
        return this.baseMapper.selectRedScore(matchId);
    }

    @Override
    public List<MatchStatisticsInfoDetail> slectListByMatchId(Long matchId) {
        return getAllTypeScore(matchId);
    }

    @Override
    public MatchStatisticsInfoDetail getByScoreType(Long matchId, ScoreTypeEnum scoreTypeEnum) {
        LambdaQueryWrapper<MatchStatisticsInfoDetail> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(MatchStatisticsInfoDetail::getStandardMatchId, matchId)
                .eq(MatchStatisticsInfoDetail::getCode, scoreTypeEnum.getCode())
                .eq(MatchStatisticsInfoDetail::getFirstNum, scoreTypeEnum.getFirstNum())
                .eq(MatchStatisticsInfoDetail::getSecondNum, scoreTypeEnum.getSecondNum())
                .last("LIMIT 1");
        MatchStatisticsInfoDetail result = this.getOne(wrapper);
        if (result == null) {
            result = new MatchStatisticsInfoDetail();
            result.setStandardMatchId(matchId);
            result.setCode(scoreTypeEnum.getCode());
            result.setFirstNum(scoreTypeEnum.getFirstNum());
            result.setSecondNum(scoreTypeEnum.getSecondNum());
            result.setT1(0);
            result.setT2(0);
        }
        return result;
    }

    @Override
    public List<MatchStatisticsInfoDetail> getAllTypeScore(Long matchId) {
        LambdaQueryWrapper<MatchStatisticsInfoDetail> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(MatchStatisticsInfoDetail::getStandardMatchId, matchId);
        return this.list(wrapper);
    }

    @Override
    public MatchStatisticsInfoDetail getByAllTypeScore(List<MatchStatisticsInfoDetail> list, ScoreTypeEnum scoreTypeEnum) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (MatchStatisticsInfoDetail detail : list) {
                if (scoreTypeEnum.getCode().equalsIgnoreCase(detail.getCode()) &&
                        scoreTypeEnum.getFirstNum().equals(detail.getFirstNum()) &&
                        scoreTypeEnum.getSecondNum().equals(detail.getSecondNum())) {
                    return detail;
                }
            }
        }
        MatchStatisticsInfoDetail result = new MatchStatisticsInfoDetail();
        result.setCode(scoreTypeEnum.getCode());
        result.setFirstNum(scoreTypeEnum.getFirstNum());
        result.setSecondNum(scoreTypeEnum.getSecondNum());
        result.setT1(0);
        result.setT2(0);
        return result;
    }

    @Override
    public Map<String, String> fifteenSoreMap(Long matchId, Long categoryId) {
        Map<String, String> scoreMap = new HashMap<>();
        if(categoryId!=null){
            String code = "minutesGoalScore";
            if(categoryId.equals(33L)){
                code = "minutesGoalScore";
            }else if(categoryId.equals(232L)){
                code = "minutesCornerScore";
            }
            List<MatchStatisticsInfoDetail> details = baseMapper.selectScoreByCode(matchId, code);
            if(CollectionUtils.isNotEmpty(details)){
                scoreMap = details.stream().filter(fi -> null != fi.getT1() && null != fi.getT2() && StringUtils.isNotBlank(fi.getCode())).collect(Collectors.toMap(vo -> vo.getCode(), vo -> vo.getT1() + ":" + vo.getT2()));
            }
        }
        return scoreMap;
    }
}

