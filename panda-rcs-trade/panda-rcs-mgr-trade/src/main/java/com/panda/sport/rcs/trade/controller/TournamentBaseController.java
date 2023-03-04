package com.panda.sport.rcs.trade.controller;

import com.panda.sport.rcs.mapper.StandardSportTournamentMapper;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.TournamentVagueVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @program: 联赛查询接口
 * @description:
 * @author: kimi
 * @create: 2021-01-10 16:58
 **/
@RestController
@RequestMapping(value = "/tournamentBase")
@Slf4j
@Component
public class TournamentBaseController {
    @Autowired
    private StandardSportTournamentMapper standardSportTournamentMapper;
    /**
     * 联赛模糊查询
     * @param name
     * @return
     */
    @RequestMapping(value = "/getList",method = RequestMethod.GET)
    public HttpResponse<List<TournamentVagueVo>> getList(String name,Integer sportId) {
        try {
            if (sportId == null) {
                sportId = 1;
            }
            List<TournamentVagueVo> tournamentVagueVoList = standardSportTournamentMapper.selectTournamentByVagueName(sportId, name);
            return HttpResponse.success(tournamentVagueVoList);
        }catch (Exception e){
            log.error("::{}::tournamentBase/getList:{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("系统故障");
        }
    }
}
