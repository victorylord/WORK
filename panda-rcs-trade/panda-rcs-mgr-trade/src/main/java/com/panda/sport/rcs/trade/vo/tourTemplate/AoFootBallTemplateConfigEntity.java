package com.panda.sport.rcs.trade.vo.tourTemplate;


import lombok.Data;

/**
 * @Author carson
 * @DATE 2022/3/7 11:26
 **/
@Data
public class AoFootBallTemplateConfigEntity implements java.io.Serializable {
    /**
     * 上半场常规时间 例:half1stPeriod = 45(分钟)
     **/
    Integer half1stPeriod;
    /**
     * 上半场伤停补时时间 例:injTime1st = 2(分钟)
     **/
    Integer injTime1st;
    /**
     * 下半场伤停补时时间 例:injTime2nd = 2(分钟)
     **/
    Integer injTime2nd;
    /***全场 drawadj  例:ftDrawadj=0.05***/
    Double ftDrawadj;
    /***半场 drawadj  例:ftDrawadj=0.05***/
    Double htDrawadj;
    /**
     * 间隔刷新时长 例:refresh=10 (秒)
     **/
    Integer refresh;
    /**
     * 00-15 例:dis0to151H =0.295
     **/
    Double dis0to151H;
    /**
     * 同上
     **/
    Double dis15to301H;
    /**
     * 同上
     **/
    Double dis30toHT;
    /**
     * 同上
     **/
    Double dis45to602H;
    /**
     * 同上
     **/
    Double dis60to752H;
    /**
     * 同上
     **/
    Double dis75toFT;
    /**
     * AO赛事id
     **/
    String aoMatchId;
    /**
     * 标准赛事ID
     **/
    String standardMatchId;
    /**
     * 联赛等级
     **/
    Integer tournamentLevel;
    /**
     * g_goal、g_corner、g_booking、g_yc、g_rc
     * ex_goal、ex_corner
     **/
    String tempType;
}
