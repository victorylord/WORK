package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.MatchStatisticsInfoMapper;
import com.panda.sport.rcs.mapper.statistics.MatchStatisticsInfoDetailMapper;
import com.panda.sport.rcs.pojo.MatchStatisticsInfo;
import com.panda.sport.rcs.pojo.MatchStatisticsInfoDetail;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.dto.utils.SubPlayUtil;
import com.panda.sport.rcs.pojo.enums.FootBallPlayEnum;
import com.panda.sport.rcs.trade.wrapper.MatchStatisticsInfoService;
import com.panda.sport.rcs.vo.RedCardVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * @ClassName MatchStatisticsInfoServiceImpl
 * @Description: TODO
 * @Author Vector
 * @Date 2019/10/11
 **/
@Service
@Slf4j
public class MatchStatisticsInfoServiceImpl extends ServiceImpl<MatchStatisticsInfoMapper, MatchStatisticsInfo> implements MatchStatisticsInfoService {

    @Autowired
    MatchStatisticsInfoMapper matchStatisticsInfoMapper;
    @Autowired
    MatchStatisticsInfoDetailMapper matchStatisticsInfoDetailMapper;
    static String MINUTES_GOAL_SCORE = "minutesGoalScore_%s";
    static String MINUTES_CORNER_SCORE_ = "minutesCornerScore_%s";

    @Override
    public MatchStatisticsInfo getMatchInfoByMatchId(Long matchId) {
        QueryWrapper<MatchStatisticsInfo> matchStatisticsInfoQueryWrapper = new QueryWrapper<>();
        matchStatisticsInfoQueryWrapper.eq(matchId != null, "standard_match_id", matchId);
        matchStatisticsInfoQueryWrapper.last("limit 1");
        MatchStatisticsInfo matchStatisticsInfo = matchStatisticsInfoMapper.selectOne(matchStatisticsInfoQueryWrapper);
        if (matchStatisticsInfo == null) {
            matchStatisticsInfo = new MatchStatisticsInfo();
        }
        return matchStatisticsInfo;
    }

    @Override
    public RedCardVo selectRedCardNum(Long standardMatchId) {
        MatchStatisticsInfo matchStatisticsInfo = getMatchInfoByMatchId(standardMatchId);
        RedCardVo cardVo = new RedCardVo();
        if (StringUtils.isNotBlank(matchStatisticsInfo.getRedCardScore())) {
            String redCardScore = matchStatisticsInfo.getRedCardScore();
            String[] split = redCardScore.split(":");
            cardVo.setHomeNum(Integer.parseInt(split[0]));
            cardVo.setAwayNum(Integer.parseInt(split[0]));
            cardVo.setSportId(Long.parseLong(matchStatisticsInfo.getSportId()));
            cardVo.setStandardMatchId(standardMatchId);
        }
        return cardVo;
    }
    @Override
    public String queryCurrentScoreByPlayId(RcsMatchMarketConfig config) {
        String score = "0:0";
        if (TradeConstant.FOOTBALL_X_A3_PLAYS.contains(config.getPlayId().intValue())){
            score = queryMatchScoreBySubPlay(config);
        }else {
            score = queryMatchScoreByPlay(config);
        }
        return score;
    }

    private String queryMatchScoreByPlay(RcsMatchMarketConfig config) {
        String score = "0:0";
        String code = FootBallPlayEnum.getScoreType(config.getPlayId());
        QueryWrapper<MatchStatisticsInfoDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MatchStatisticsInfoDetail ::getStandardMatchId,config.getMatchId())
                //.eq(MatchStatisticsInfoDetail ::getCode, FootBallPlayEnum.getScoreType(playId))
                .eq(MatchStatisticsInfoDetail ::getFirstNum, FootBallPlayEnum.getStage(config.getPlayId()));
        if ("card".equalsIgnoreCase(code)){
            queryWrapper.lambda().in(MatchStatisticsInfoDetail ::getCode, "yellow_card_score","red_card_score");
        }else {
            queryWrapper.lambda().eq(MatchStatisticsInfoDetail ::getCode, code);
        }
        List<MatchStatisticsInfoDetail> infos = matchStatisticsInfoDetailMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(infos)){
            if ("card".equalsIgnoreCase(code)){
                Integer t1 = NumberUtils.INTEGER_ZERO;
                Integer t2 = NumberUtils.INTEGER_ZERO;
                for (MatchStatisticsInfoDetail detail : infos){
                    t1 += detail.getT1();
                    t2 += detail.getT2();
                    if ("red_card_score".equalsIgnoreCase(detail.getCode())){
                        t1 += t1;
                        t2 += t2;
                    }
                }
                score = t1+":"+t2;
            }else {
                score = infos.get(NumberUtils.INTEGER_ZERO).getT1().toString() + ":" + infos.get(NumberUtils.INTEGER_ZERO).getT2().toString();
            }
        }
        log.info("::{}::获取玩法比分score = {}",config.getMatchId(),score);
        return score;
    }
    private String queryMatchScoreBySubPlay(RcsMatchMarketConfig config) {
        String score = "0:0";
        Integer subPlayId = Integer.parseInt(config.getSubPlayId()) % 100 * 15;

        QueryWrapper<MatchStatisticsInfoDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MatchStatisticsInfoDetail ::getStandardMatchId,config.getMatchId());
        if (TradeConstant.FOOTBALL_X_15M_GOAL_SCORE_PLAYS.contains(config.getPlayId().intValue())){
            queryWrapper.lambda().eq(MatchStatisticsInfoDetail ::getCode, String.format(MINUTES_GOAL_SCORE,subPlayId));
        }else if (TradeConstant.FOOTBALL_X_15M_CORNER_SCORE_PLAYS.contains(config.getPlayId().intValue())){
            queryWrapper.lambda().eq(MatchStatisticsInfoDetail ::getCode, String.format(MINUTES_CORNER_SCORE_,subPlayId));
        }
        MatchStatisticsInfoDetail info = matchStatisticsInfoDetailMapper.selectOne(queryWrapper);
        if (!ObjectUtils.isEmpty(info)){
            score = info.getT1().toString() + ":" + info.getT2().toString();
        }
        log.info("::{}::获取子玩法比分score = {}",config.getMatchId(),score);
        return score;
    }
}
