package com.panda.sport.rcs.vo.statistics;

import com.panda.sport.rcs.pojo.statistics.RcsProfitRectangle;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.vo.statistics
 * @Description :  矩阵
 * @Date: 2020-06-14 21:13
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class ProfitRectangleMQVo {
    private String id;

    /**
     * 主键
     * @return
     */
    public String getId(){
        return String.format("%s_%s_%s",matchId.toString() , matchType.toString() , playId.toString() );
    }
    /**
     * 赛事id
     */
    private Long matchId;
    /**
     * 玩法id
     */
    private Integer playId;
    /**
     * 赛事类型
     */
    private Integer matchType;
    /**
     * 数据列表
     */
    private Map<Double, RcsProfitRectangle> profitRectangleList;

    /**
     * 初始化
     * @param matchId
     * @param playId
     * @param matchType
     * @param profitRectangleList
     */
    public ProfitRectangleMQVo(Long matchId, Integer playId, Integer matchType, Map<Double, RcsProfitRectangle> profitRectangleList) {
        this.matchId = matchId;
        this.playId = playId;
        this.matchType = matchType;
        this.profitRectangleList = profitRectangleList;
    }
}
