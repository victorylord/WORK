package com.panda.sport.rcs.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author :  riben
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.pojo.dto
 * @Description :  TODO
 * @Date: 2020-10-24 17:49
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class MatchTradeMemoRemindDTO implements Serializable{
    /**
     * 操盘手id
     */
    private String traderId;
    /**
     * 提醒发送的的赛事id
     */
    private Long matchId;
    /**
     * 提醒发送的的备忘录id
     */
    private String memoId;

    /**
     * 是否已读
     */
    private Boolean readFlag;

}
