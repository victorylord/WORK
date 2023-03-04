package com.panda.sport.rcs.trade.strategy.logFormat;

import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.log.format.RcsOperateLog;
import com.panda.sport.rcs.mapper.RcsLanguageInternationMapper;
import com.panda.sport.rcs.pojo.LanguageInternation;
import com.panda.sport.rcs.trade.enums.MatchTypeEnum;
import com.panda.sport.rcs.trade.param.TournamentTemplatePlayMargainRefParam;
import com.panda.sport.rcs.trade.strategy.LogFormatStrategy;
import com.panda.sport.rcs.trade.util.NumberConventer;
import com.panda.sport.rcs.utils.CategoryParseUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 操盤日誌(removeMargainRef)
 * 設置-玩法分時節點刪除
 */
@Service
public class LogRemoveMargainRefFormat extends LogFormatStrategy {

	@Autowired
    private RcsLanguageInternationMapper rcsLanguageInternationMapper;

    @Override
    public RcsOperateLog formatLogBean(RcsOperateLog rcsOperateLog, Object[] args) {
        TournamentTemplatePlayMargainRefParam param = (TournamentTemplatePlayMargainRefParam) args[0];

        //根據不同操作頁面組裝不同格式
        switch (param.getOperatePageCode()) {
            case 14:
                //早盘操盘-设置
                rcsOperateLog.setOperatePageCode(110);
                rcsOperateLog.setBehavior(OperateLogEnum.OPERATE_SETTING.getName());
                return operateDeleteFormat(rcsOperateLog, param);
            case 17:
                //滚球操盘-设置
                rcsOperateLog.setOperatePageCode(111);
                rcsOperateLog.setBehavior(OperateLogEnum.OPERATE_SETTING.getName());
                return operateDeleteFormat(rcsOperateLog, param);
            case 21:
                //联赛参数设置
                rcsOperateLog.setOperatePageCode(param.getOperatePageCode());
                rcsOperateLog.setBehavior(OperateLogEnum.TEMPLATE_UPDATE.getName());
                return templateDeleteFormat(rcsOperateLog, param);
        }

        return null;
    }

    /**
     * 操盤設置中刪除
     *
     * @param rcsOperateLog
     * @param param
     */
    private RcsOperateLog operateDeleteFormat(RcsOperateLog rcsOperateLog, TournamentTemplatePlayMargainRefParam param) {
        String playName = getPlayName(param.getPlayId().longValue(), param.getSportId());
        rcsOperateLog.setMatchId(param.getMatchId());
        rcsOperateLog.setPlayId(param.getPlayId().longValue());
        rcsOperateLog.setObjectIdByObj(param.getPlayId());
        rcsOperateLog.setObjectNameByObj(playName);
        rcsOperateLog.setExtObjectIdByObj(param.getMatchManageId());
        rcsOperateLog.setExtObjectNameByObj(getMatchName(param.getTeamList()));
        rcsOperateLog.setParameterName(playName + "-" + transTimeValName(param));
        rcsOperateLog.setBeforeValByObj(OperateLogEnum.NONE.getName());
        rcsOperateLog.setAfterValByObj("刪除");

        return rcsOperateLog;
    }

    /**
     * 聯賽模板中刪除
     *
     * @param rcsOperateLog
     * @param param
     * @return
     */
    private RcsOperateLog templateDeleteFormat(RcsOperateLog rcsOperateLog, TournamentTemplatePlayMargainRefParam param) {
        String playName = getPlayName(param.getPlayId().longValue(), param.getSportId());
        rcsOperateLog.setObjectIdByObj(param.getTemplateId());
        String tournamentLevelTemplateName = getTournamentLevelTemplateName(param);
        rcsOperateLog.setObjectNameByObj(StringUtils.isNoneBlank(param.getTemplateName()) ? param.getTemplateName() : tournamentLevelTemplateName);
        rcsOperateLog.setExtObjectIdByObj(OperateLogEnum.NONE.getName());
        rcsOperateLog.setExtObjectNameByObj(OperateLogEnum.NONE.getName());
        rcsOperateLog.setParameterName(playName + "-" + transTimeValName(param));
        rcsOperateLog.setBeforeValByObj(OperateLogEnum.NONE.getName());
        rcsOperateLog.setAfterValByObj("刪除");

        return rcsOperateLog;
    }

    /**
     * 查詢玩法名稱
     *
     * @param playId
     * @return
     */
    private String getPlayName(Long playId, Integer sportId) {
        LanguageInternation playName = rcsLanguageInternationMapper.getPlayNameByCategoryIdAndSportId(playId, sportId);
        return CategoryParseUtils.parseName(Objects.nonNull(playName) ? playName.getText() : "");
    }

    /**
     * 獲取預設模板名稱
     *
     * @param param
     * @return
     */
    private String getTournamentLevelTemplateName(TournamentTemplatePlayMargainRefParam param) {
        if (Objects.nonNull(param.getTypeVal())) {
            String levelName = NumberConventer.GetCH(param.getTypeVal().intValue());
            return StringUtils.isNoneBlank(levelName) ? levelName + "级联赛" + MatchTypeEnum.getNameById(param.getMatchType()) + "模板" : "";
        } else {
            return "";
        }
    }


    /**
     * TimeVal轉換
     *
     * @param param
     * @return
     */
    private String transTimeValName(TournamentTemplatePlayMargainRefParam param) {
        if (Objects.nonNull(param.getMatchType())) {
            switch (param.getMatchType()) {
                case 0:
                    //滾球
                    return inRunningMarketTimeVal(param);
                case 1:
                    //早盤
                    return earlyMarketTimeVal(param);
                default:
                    return String.valueOf(param.getTimeVal());
            }
        }
        return String.valueOf(param.getTimeVal());
    }

    /**
     * 早盤文字轉換
     *
     * @param param
     * @return
     */
    private String earlyMarketTimeVal(TournamentTemplatePlayMargainRefParam param) {
        Long hour = param.getTimeVal() / 3600;
        if (24 >= hour) {
            return hour + "H";
        } else {
            if (hour / 24 == 30) {
                return "开售";
            } else {
                return hour / 24 + "D";
            }
        }
    }

    /**
     * 滾球文字轉換
     *
     * @param param
     * @return
     */
    private String inRunningMarketTimeVal(TournamentTemplatePlayMargainRefParam param) {
        Long minute = param.getTimeVal() / 60;
        if (minute == 0) {
            return "开售";
        } else {
            return minute + "分钟";
        }
    }
}
