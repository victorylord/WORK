package com.panda.sport.rcs.trade.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.StandardSportTournament;
import com.panda.sport.rcs.pojo.statistics.RcsProfitRectangle;
import com.panda.sport.rcs.trade.vo.profit.ProfiRectangleVo;
import com.panda.sport.rcs.trade.vo.profit.ProfitDetailParam;
import com.panda.sport.rcs.trade.vo.profit.ProfitDetailVo;
import com.panda.sport.rcs.trade.vo.profit.ProfitMatchVo;
import com.panda.sport.rcs.trade.vo.profit.ProfitPlayRectangleVo;
import com.panda.sport.rcs.trade.wrapper.RcsLanguageInternationService;
import com.panda.sport.rcs.trade.wrapper.StandardSportTeamService;
import com.panda.sport.rcs.trade.wrapper.StandardSportTournamentService;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.MatchMarketLiveOddsVo;

import lombok.extern.slf4j.Slf4j;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.controller
 * @Description :  期望详情
 * @Date: 2020-03-05 11:04
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@RestController
@Slf4j
@RequestMapping(value = "/profitDetail")
public class ProfitDetailController {
	
    @Autowired
    private StandardSportTournamentService standardSportTournamentService;

    @Autowired
    private StandardSportTeamService standardSportTeamService;
    
    @Autowired
    private RcsLanguageInternationService rcsLanguageInternationService;

    private static List<Integer> playIds=new ArrayList<>();

    static {
        playIds.add(2);
        playIds.add(4);
        playIds.add(18);
        playIds.add(19);
    }


    @RequestMapping(value = "/get", method = RequestMethod.POST)
    public HttpResponse< List<RcsProfitRectangle>> getList(@RequestBody  ProfitDetailParam param)
    {
        //换算成账务时间
        long beginTime = DateUtils.getBeginTime(param.getDate());
        long endTime = DateUtils.getEndTime(param.getDate());
        if(param.getStandardTournamentIds().size() == 0){
            return HttpResponse.success("没有找到相关赛事信息");
        }

        List<StandardSportTournament> standardSportTournaments= standardSportTournamentService.queryByIdsAndBeginDateAndEndDateAndMatchType(param.getStandardTournamentIds(),beginTime,endTime,param.getMatchType(),param.getOtherMorningMarket());
        if(standardSportTournaments.size() == 0){
            return HttpResponse.success("没有找到相关赛事信息");
        }

        List<ProfitDetailVo> profitDetailVoList = new ArrayList<ProfitDetailVo>();
        //联赛
        for(StandardSportTournament standardSportTournament:standardSportTournaments) {
            ProfitDetailVo profitDetailVo = new ProfitDetailVo();
            profitDetailVo.setStandardTournamentId(standardSportTournament.getId());
            profitDetailVo.setTurnamentName(rcsLanguageInternationService.getCachedNamesByCode(standardSportTournament.getNameCode()));

            List<ProfitMatchVo> matchVoList = new ArrayList<>();
            profitDetailVo.setMatchVoList(matchVoList);

            //赛事
            for (StandardMatchInfo standardMatchInfo : standardSportTournament.getStandardMatchInfoList()) {
                 ProfitMatchVo profitMatchVo = new ProfitMatchVo();

                List<MatchMarketLiveOddsVo.MatchMarketTeamVo> teamList = standardSportTeamService.queryTeamList(standardMatchInfo.getId());
                if (!CollectionUtils.isEmpty(teamList)) {
                    profitMatchVo.setTeamList(teamList);
                }


                profitMatchVo.setBeginTime(standardMatchInfo.getBeginTime());
                profitMatchVo.setMatchId(standardMatchInfo.getId());

                List<ProfitPlayRectangleVo> profitPlayRectangleVoList= new ArrayList<>();

                //玩法
                for(Integer playId:playIds) {
                    ProfitPlayRectangleVo profitPlayRectangleVo = new ProfitPlayRectangleVo();
                    profitPlayRectangleVo.setPlayId(playId);
                    switch (playId) {
                        case 2:
                            profitPlayRectangleVo.setPlayName("大小盘");
                            break;
                        case 4:
                            profitPlayRectangleVo.setPlayName("让球盘");
                            break;
                        case 18:
                            profitPlayRectangleVo.setPlayName("上半场大小");
                            break;
                        case 19:
                            profitPlayRectangleVo.setPlayName("上半场让球");
                            break;
                        default:
                            break;
                    }

                    List<ProfiRectangleVo> profiRectangleVos = new ArrayList<>();

                    //矩阵
                    for (RcsProfitRectangle rcsProfitRectangle : standardMatchInfo.getRcsProfitRectangleList()) {
                        if (param.getMatchType().compareTo(rcsProfitRectangle.getMatchType()) == 0 &&
                                rcsProfitRectangle.getPlayId().equals(playId) && rcsProfitRectangle.getMatchId() .equals(standardMatchInfo.getId())) {
                            if(((rcsProfitRectangle.getPlayId() == 2 || rcsProfitRectangle.getPlayId()==18)&&
                                    (rcsProfitRectangle.getScore()>=0 && rcsProfitRectangle.getScore()<=6))||
                                    ((rcsProfitRectangle.getPlayId() == 4 || rcsProfitRectangle.getPlayId()==19)&&
                                            (rcsProfitRectangle.getScore()>=-3 && rcsProfitRectangle.getScore()<=3))) {
                                ProfiRectangleVo profiRectangleVo = new ProfiRectangleVo();
                                profiRectangleVo.setProfitValue(rcsProfitRectangle.getProfitValue());
                                profiRectangleVo.setScore(rcsProfitRectangle.getScore());
                                profiRectangleVos.add(profiRectangleVo);
                            }
                        }
                    }

                    if(profiRectangleVos.size() == 0) {
                        if (playId == 2 || playId == 18) {
                            for (int i = 0; i <= 6; i++) {
                                ProfiRectangleVo profiRectangleVo = new ProfiRectangleVo();
                                profiRectangleVo.setProfitValue(BigDecimal.ZERO);
                                profiRectangleVo.setScore(i);
                                profiRectangleVos.add(profiRectangleVo);
                            }
                        } else if (playId == 4 ||playId == 19) {
                            for (int i = -3; i <= 3; i++) {
                                ProfiRectangleVo profiRectangleVo = new ProfiRectangleVo();
                                profiRectangleVo.setProfitValue(BigDecimal.ZERO);
                                profiRectangleVo.setScore(i);
                                profiRectangleVos.add(profiRectangleVo);
                            }
                        }
                    }

                    Collections.sort(profiRectangleVos);
                    profitPlayRectangleVo.setProfiRectangleVos(profiRectangleVos);
                    profitPlayRectangleVoList.add(profitPlayRectangleVo);
                }

                profitMatchVo.setProfitPlayRectangleVos(profitPlayRectangleVoList);
                matchVoList.add(profitMatchVo);
            }
            profitDetailVoList.add(profitDetailVo);
        }

        return HttpResponse.success(profitDetailVoList);
    }
}
