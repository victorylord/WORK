package com.panda.sport.rcs.vo;

import lombok.Data;

/**
 * @author :  myname
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2019-12-12 16:37
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RequestMarketOrderVo {
    /**
     * @Description 当前页码
     **/
    Integer pageNum;
    /**
     * @Description 一页展示多少条
     **/
    Integer pageSize;
    /**
     * 投注项
     */
    Integer orderOdds;

    /**
     * 投注项（over under even 1 x 2 。。。。）
     */
    String oddsType;
    /**
     * @Description 玩法id
     **/
    private Integer playId;
    /**
     * @Description 盘口id
     **/
    private Long marketId;
    /**
     * @Description 赛事id
     **/
    private Long matchId;
    /**
     * @Description 类型  0滚球界面  1其他页面
     **/
    private Integer type;

    /**
     * @Description 盘口位置
     **/
    private Integer placeNum;

    /**
     * 1早盘   2滚球
     **/
    private Integer matchType;

    /**
     * 1足球   2篮球  ......
     **/
    private Integer sportId;
}
