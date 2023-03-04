package com.panda.sport.rcs.vo;

import lombok.Data;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 * @author :  Sean
 * @Project Name :  panda-rcs
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2020-01-16 17:15
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class BaseMatchInfoVo implements Serializable {
    /**
     * 球队
     */
    private List<MatchMarketLiveOddsVo.MatchMarketTeamVo> teamList;
    private Integer matchId;
    /**
     * 标准赛事编码. 用于管理的赛事id
     */
    private String matchManageId;
    private Integer standardTournamentId;
    private String standardTournamentName;
    private String matchInfo;
    private String tradeType;
    final static String VS = " VS ";
    private String matchPosition;
    /**
     * 中场休息 0 关闭 1 开启
     */
    private Integer halfTime;
    private Integer tournamentLevel;
    private Long nameCode;
    private List<I18nItemVo> tournamentNames;
//    public String getMatchInfo(){
//        if(!StringUtils.isEmpty(matchInfo)){
//            return matchInfo;
//        }
//        StringBuffer matchInfoStr = new StringBuffer();
//        String home = "";
//        String away = "";
//        if(CollectionUtils.isEmpty(teamList)){
//            return matchInfoStr.toString();
//        }
//        for (MatchMarketLiveOddsVo.MatchMarketTeamVo m :teamList){
//            if(m.getMatchPosition().equalsIgnoreCase("home")){
//                home = StringUtils.isEmpty(m.getNames().get("zs")) ? m.getNames().get("en") : m.getNames().get("zs");
//                continue;
//            }
//            if(m.getMatchPosition().equalsIgnoreCase("away")){
//                away = StringUtils.isEmpty(m.getNames().get("zs")) ? m.getNames().get("en") : m.getNames().get("zs");
//            }
//        }
//        return matchInfoStr.append(home).append(VS).append(away).toString();
//    }
}
