package com.panda.sport.rcs.pojo.dto;

import com.panda.sport.rcs.pojo.MatchStatisticsInfoDetail;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * @author :  vector 2.3
 * @Project Name :  data-realtime
 * @Package Name :  com.panda.sport.data.realtime.api.message
 * @Description :  TODO
 * @Date: 2019-10-07 17:10
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class MatchStatisticsInfoDTO implements Serializable{

    private static final long serialVersionUID = 1L;
    private List<MatchStatisticsInfoDetail> matchStatisticsInfoDetailList;
    /**
     * 数据来源编码. 取值见: data_source.code
     */
    private String dataSourceCode;
    /**
     * id
     */
    private Long id;
    /**
     * 运动类型
     */
    private Long sportId;
    /**
     * 第三方赛事原始id
     */
    private Long thirdSourceMatchId;

    /**
     * 第三方赛事id
     */
    private Long thirdMatchId;

    /**
     * 标准赛事id
     */
    private Long standardMatchId;

    /**
     * 预计比赛时长.  单位:秒
     */
    private Integer matchLength;

    /**
     * Game short info
     */
    private String info;

    /**
     * 比赛阶段
     */
    private Integer period;

    /**
     * Total set count
     */
    private Integer setCount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 更新时间. UTC时间,精确到毫秒
     */
    private Long modifyTime;

    /**
     * 当前比分信息
     */
    private String score;
    
    /**
     * 比赛进行时间
     */
    private Integer secondsFromStart;

}
