package com.panda.sport.rcs.trade.init;

import com.alibaba.fastjson.JSON;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mapper.StandardSportTournamentMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.StandardSportTournament;
import com.panda.sport.rcs.pojo.tourTemplate.AoParameterTemplateVo;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.ao.AoParametersModifyVo;
import com.panda.sport.rcs.trade.vo.tourTemplate.AoBasketBallTemplateConfigEntity;
import com.panda.sport.rcs.trade.vo.tourTemplate.AoFootBallTemplateConfigEntity;
import com.panda.sport.rcs.vo.StandardMatchInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class AoDataSourceInit {
    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    private StandardSportTournamentMapper standardSportTournamentMapper;
    @Autowired
    private ProducerSendMessageUtils sendMessage;

    public void sendAoDataSourceMessage(RcsTournamentTemplate tournamentTemplate, long matchId) {
        //查询数据库AOID
        StandardMatchInfoVo standardMatchInfo = standardMatchInfoMapper.selectStandardMatchInfoBaseByMatchId(matchId);
        log.info("::{}::AO发送数据源给融合查询AOID:{},{}", matchId, matchId, JSON.toJSONString(standardMatchInfo));
        StandardSportTournament standardSportTournament = standardSportTournamentMapper.selectById(standardMatchInfo.getStandardTournamentId());
        log.info("::{}::AO发送数据源给融合查询联赛信息:{},{}", matchId, standardMatchInfo.getStandardTournamentId(), JSON.toJSONString(standardSportTournament));
        String thirdMatchStr = CommonUtil.getId(standardMatchInfo.getThirdMatchListStr(), "AO");
        Integer tournamentLevel = Objects.isNull(standardSportTournament) || Objects.isNull(standardSportTournament.getTournamentLevel()) ? 0 : standardSportTournament.getTournamentLevel();
        if (StringUtils.isNotBlank(thirdMatchStr)) {
            AoParametersModifyVo modifyVo = new AoParametersModifyVo(standardMatchInfo.getSportId().intValue());
            if(SportIdEnum.isFootball(standardMatchInfo.getSportId())){
                //足球
                List<AoFootBallTemplateConfigEntity> aoFootBallList = convertToAoFootBall(tournamentTemplate.getAoConfigValue(), matchId, thirdMatchStr, tournamentLevel);
                modifyVo.setFootBallTemplateConfigList(aoFootBallList);
            }else if(SportIdEnum.isBasketball(standardMatchInfo.getSportId())){
                //篮球
                List<AoBasketBallTemplateConfigEntity> aoBasketBallList = convertToAoBasketBall(tournamentTemplate.getAoConfigValue(), matchId, thirdMatchStr, tournamentLevel);
                modifyVo.setBasketBallTemplateConfigList(aoBasketBallList);
            }
            sendMessage.sendMessage("AO_DATA_REALTIME_CONFIG_TOPIC", "", String.valueOf(matchId), modifyVo);
        }
    }

    public boolean checkIfAoSport(Integer sportId) {
        return SportIdEnum.isFootball(Long.valueOf(sportId))
                || SportIdEnum.isBasketball(Long.valueOf(sportId));
    }

    private List<AoFootBallTemplateConfigEntity> convertToAoFootBall(String aoConfigValue, Long matchId, String thirdMatchStr, Integer tournamentLevel){
        List<AoParameterTemplateVo> aoParameterTemplateVoList = JSON.parseArray(aoConfigValue, AoParameterTemplateVo.class);
        log.info("::{}::足球AO发送数据源给融合查询模板AO配置:{}", matchId, JSON.toJSONString(aoParameterTemplateVoList));
        List<AoFootBallTemplateConfigEntity> aoFootBallTemplateConfigEntityList = new ArrayList<>();
        aoParameterTemplateVoList.forEach(aoParameterTemplateVo -> {
            AoFootBallTemplateConfigEntity aoFootBallTemplateConfigEntity = new AoFootBallTemplateConfigEntity();
            aoFootBallTemplateConfigEntity.setAoMatchId(thirdMatchStr);
            aoFootBallTemplateConfigEntity.setStandardMatchId(String.valueOf(matchId));
            aoFootBallTemplateConfigEntity.setHalf1stPeriod(aoParameterTemplateVo.getPerId());
            aoFootBallTemplateConfigEntity.setInjTime1st(aoParameterTemplateVo.getOneInjTime());
            aoFootBallTemplateConfigEntity.setInjTime2nd(aoParameterTemplateVo.getTwoInjTime());
            aoFootBallTemplateConfigEntity.setHtDrawadj(Double.valueOf(new BigDecimal(aoParameterTemplateVo.getHtDrawAdj()).divide(BigDecimal.valueOf(100)).toString()));
            aoFootBallTemplateConfigEntity.setFtDrawadj(Double.valueOf(new BigDecimal(aoParameterTemplateVo.getFtDrawAdj()).divide(BigDecimal.valueOf(100)).toString()));
            aoFootBallTemplateConfigEntity.setRefresh(aoParameterTemplateVo.getRefresh());
            aoFootBallTemplateConfigEntity.setDis0to151H(Double.valueOf(aoParameterTemplateVo.getZeroOneFive().toString()));
            aoFootBallTemplateConfigEntity.setDis15to301H(Double.valueOf(aoParameterTemplateVo.getOneFiveThree().toString()));
            aoFootBallTemplateConfigEntity.setDis30toHT(Double.valueOf(aoParameterTemplateVo.getThreeHt().toString()));
            aoFootBallTemplateConfigEntity.setDis45to602H(Double.valueOf(aoParameterTemplateVo.getHtSix().toString()));
            aoFootBallTemplateConfigEntity.setDis60to752H(Double.valueOf(aoParameterTemplateVo.getSixSevenFive().toString()));
            aoFootBallTemplateConfigEntity.setDis75toFT(Double.valueOf(aoParameterTemplateVo.getSevenFiveFt().toString()));
            aoFootBallTemplateConfigEntity.setTempType(aoParameterTemplateVo.getTempType());
            aoFootBallTemplateConfigEntity.setTournamentLevel(tournamentLevel);
            aoFootBallTemplateConfigEntityList.add(aoFootBallTemplateConfigEntity);
        });
        log.info("::{}::足球AO发送数据源给融合查询模板matchTemplateConfigEntity配置:{}", matchId, aoFootBallTemplateConfigEntityList);
        return aoFootBallTemplateConfigEntityList;
    }

    private List<AoBasketBallTemplateConfigEntity> convertToAoBasketBall(String aoConfigValue, Long matchId, String thirdMatchStr, Integer tournamentLevel) {
        AoBasketBallTemplateConfigEntity aoBasketBallTemplateConfigEntity = JSON.parseObject(aoConfigValue, AoBasketBallTemplateConfigEntity.class);
        log.info("::{}::蓝球AO发送数据源给融合查询模板AO配置:{}", matchId, JSON.toJSONString(aoBasketBallTemplateConfigEntity));
        aoBasketBallTemplateConfigEntity.setAoMatchId(thirdMatchStr);
        aoBasketBallTemplateConfigEntity.setStandardMatchId(String.valueOf(matchId));
        aoBasketBallTemplateConfigEntity.setTournamentLevel(tournamentLevel);
        log.info("::{}::篮球AO发送数据源给融合查询模板matchTemplateConfigEntity配置:{}", matchId, aoBasketBallTemplateConfigEntity);
        List<AoBasketBallTemplateConfigEntity> aoBasketBallTemplateConfigEntityList = new ArrayList<>();
        aoBasketBallTemplateConfigEntityList.add(aoBasketBallTemplateConfigEntity);
        return aoBasketBallTemplateConfigEntityList;
    }
}