package com.panda.sport.rcs.vo;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author :  myname
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2019-12-12 10:32
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class OrderDetailVo {

    /**
     * 体育id
     **/
    private Integer sportId;

    /**
     * 用户id
     **/
    private String uid;
    /**
     * 用户id
     **/
    private String userName;
    /**
     * 用户标签
     **/
    private List<Integer> userLevels;
    /**
     * 用户标签
     **/
    private Integer userLevel;
    /**
     * 注单赔率
     **/
    private BigDecimal oddsValue;
    /**
     * 注单金额
     **/
    private BigDecimal betAmount;
    /**
     * 注单金额币种
     **/
    private String currencyCode;
    /**
     * 下注时间
     **/
    private String betTime;
    /**
     * 最早下注时间
     **/
    private String minBetTime;
    /**
     * 最后下注时间
     **/
    private String maxBetTime;
    /**
     * 盘口值
     **/
    private String marketValue;
    /**
     * 投注项名称(新加)
     */
    private String playOptionsName;
    /**
     * 比分阶段
     */
    private String playStage;
    /**
     * 比分
     */
    private String betScore;
    /**
     * 注单状态
     */
    private String betStatus;
    /**
     * ip
     */
    private String ip;
    /**
     * ip对应的地址
     */
    private String ipAddress;
    /**
     * 赛事阶段：1-早盘；2-滚球
     */
    private Integer matchType;
    /**
     * 玩法id
     */
    private List<Integer> playIds;
    /**
     * 赛事id
     */
    private Integer matchId;
    /**
     * 盘口id
     */
    private Long marketId;

    /**
     * 玩法id
     */
    private Long playId;

    /**
     * 投注类型
     */
    private String playOptions;
    /**
     * 投注项id
     */
    private Long playOptionsId;
    /**
     * 玩法名称
     */
    private String playName;
    /**
     * 每页展示数量
     */
    private Integer pageSize;
    /**
     * 当前页
     */
    private Integer currentPage;
    /**
     * 记录起始位置
     */
    private Long start;

    public String getBetScore(){
        String score = "0:0";
        if (StringUtils.isNotBlank(this.betScore)){
            score = this.betScore;
        }
        return score;
    }

    /**
     * 一级标签等级
     */
    private Integer levelId;
    /**
     * 一级标签名字
     */
    private String levelName;
    private String bgColor;
    private String color;

    private String sportList;
    private String orderTypeList;
    private String tournamentList;
    private String orderStageList;
    private String playList;
    private Integer orderStatus;
    private String reason;

    private Integer matchPeriodId;
    private String score;
}
