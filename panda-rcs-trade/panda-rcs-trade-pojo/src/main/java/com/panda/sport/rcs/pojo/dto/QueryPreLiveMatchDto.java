package com.panda.sport.rcs.pojo.dto;

import com.panda.sport.manager.api.bo.QueryPreLiveMatchBO;
import lombok.Data;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo
 * @Description :  统计ip
 * @Date: 2020-06-07 16:27
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class QueryPreLiveMatchDto extends QueryPreLiveMatchBO {
    /**
     * 联赛收藏状态
     */
    private Integer tournamentFavoriteStatus;
    /**
     * 模板等级
     */
    private String templateLevel;

}
