package com.panda.sport.rcs.trade.mq.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mapper.StandardSportTeamMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplateMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.MatchTeamInfo;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import com.panda.sport.rcs.trade.init.AoDataSourceInit;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.vo.ao.AoParametersModifyVo;
import com.panda.sport.rcs.trade.vo.tourTemplate.AoBasketBallTemplateConfigEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author: jstyChandler
 * @Package: com.panda.sport.rcs.trade.mq.impl
 * @ClassName: AoCompetitionSystemConsumer
 * @Description: 融合下发AO参数
 * @Date: 2023/2/10 16:47
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "RCS_TRADE_AO_CS_MODIFY",
        consumerGroup = "RCS_TRADE_AO_CS_MODIFY",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class AoParameterModifyConsumer extends RcsConsumer<AoParametersModifyVo> {

    @Resource
    private RcsTournamentTemplateMapper templateMapper;
    @Resource
    private StandardMatchInfoMapper standardMatchInfoMapper;
    @Resource
    private RcsTournamentTemplateMapper rcsTournamentTemplateMapper;
    @Resource
    private StandardSportTeamMapper standardSportTeamMapper;
    @Resource
    private AoDataSourceInit aoDataSourceInit;
    @Resource
    private ProducerSendMessageUtils sendMessage;

    @Override
    protected String getTopic() {
        return "RCS_TRADE_AO_CS_MODIFY";
    }

    @Override
    public Boolean handleMs(AoParametersModifyVo modifyVo) {
        try {
            if (null == modifyVo) {
                log.info("AO参数下发错误,无数据");
                return true;
            }
            log.info("AO参数下发参数:{}", JsonFormatUtils.toJson(modifyVo));
            List<AoBasketBallTemplateConfigEntity> basketBallTemplateConfigList = modifyVo.getBasketBallTemplateConfigList();
            RcsTournamentTemplate updateTemplate = new RcsTournamentTemplate();
            QueryWrapper<RcsTournamentTemplate> queryWrapper = new QueryWrapper<>();
            if (!CollectionUtils.isEmpty(basketBallTemplateConfigList)) {
                for (AoBasketBallTemplateConfigEntity basketBallTemplateConfig : basketBallTemplateConfigList) {
                    String matchId = basketBallTemplateConfig.getStandardMatchId();
                    RcsOperateLog rcsOperateLog = getLogFormatBean(basketBallTemplateConfig, modifyVo.getSportId());
                    queryWrapper.lambda().eq(RcsTournamentTemplate::getType, 3L);
                    queryWrapper.lambda().eq(RcsTournamentTemplate::getTypeVal, Long.valueOf(matchId));
                    updateTemplate.setAoConfigValue(JsonFormatUtils.toJson(basketBallTemplateConfig));
                    if (rcsOperateLog != null) {
                        templateMapper.update(updateTemplate, queryWrapper);
                        sendMessage.sendMessage("AO_DATA_REALTIME_CONFIG_TOPIC", "", matchId, modifyVo);
                        sendMessage.sendMessage("rcs_log_operate", "", rcsOperateLog.getMatchId() + "", rcsOperateLog);
                    }
                }
            }
        } catch (Exception e) {
            log.error("AO参数下发消费异常:", e);
        }
        return true;
    }

    private RcsOperateLog getLogFormatBean(AoBasketBallTemplateConfigEntity basketBallTemplateConfig, Integer sportId) {
        RcsOperateLog rcsOperateLog = new RcsOperateLog();
        Long matchId = Long.parseLong(basketBallTemplateConfig.getStandardMatchId());
        StandardMatchInfo standardMatchInfo = standardMatchInfoMapper.selectById(matchId);
        Integer matchType = RcsConstant.getMatchType(standardMatchInfo);
        QueryWrapper<RcsTournamentTemplate> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RcsTournamentTemplate::getMatchType, matchType);
        queryWrapper.lambda().eq(RcsTournamentTemplate::getType, 3L);
        queryWrapper.lambda().eq(RcsTournamentTemplate::getTypeVal, matchId);
        RcsTournamentTemplate tournamentTemplate = rcsTournamentTemplateMapper.selectOne(queryWrapper);
        if (null == tournamentTemplate) {
            log.info("AO参数修改失败,联赛模板不存在:matchType={},matchId={}", matchType, matchId);
            return null;
        }
        List<MatchTeamInfo> teamList = standardSportTeamMapper.queryTeamListByMatchId(matchId);
        String matchName = getMatchName(teamList);
        rcsOperateLog.setOperatePageCode(15);
        rcsOperateLog.setMatchId(matchId);
        rcsOperateLog.setObjectIdByObj(matchId);
        rcsOperateLog.setObjectNameByObj(matchName);
        rcsOperateLog.setExtObjectIdByObj(tournamentTemplate.getId());
        rcsOperateLog.setExtObjectNameByObj(convertMatchType(matchType, sportId));
        rcsOperateLog.setBeforeValByObj(tournamentTemplate.getAoConfigValue());
        rcsOperateLog.setAfterValByObj(JsonFormatUtils.toJson(basketBallTemplateConfig));
        rcsOperateLog.setParameterName("AO参数修改");
        rcsOperateLog.setUserId("-1");
        rcsOperateLog.setOperateTime(new Date());
        return rcsOperateLog;
    }

    private String convertMatchType(Integer matchType, Integer sportId) {
        String objectName = null;
        if (0 == matchType) {
            objectName = "早盘";
        } else if (1 == matchType) {
            objectName = "滚球盘";
        }
        return SportIdEnum.getBySportId(Long.valueOf(sportId)).getName() + "-" + objectName;
    }

    private String getMatchName(List<MatchTeamInfo> teamList) {
        //取隊伍名稱
        String home = "", away = "";
        for (MatchTeamInfo teamVo : teamList) {
            String name = Optional.ofNullable(teamVo.getText()).orElse("");
            if ("home".equals(teamVo.getMatchPosition())) {
                home = name;
            } else if ("away".equals(teamVo.getMatchPosition())) {
                away = name;
            }
        }
        return home + " VS " + away;
    }
}
