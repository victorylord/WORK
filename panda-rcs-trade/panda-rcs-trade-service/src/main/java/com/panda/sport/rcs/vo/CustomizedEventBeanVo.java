package com.panda.sport.rcs.vo;

import lombok.Data;

/**
 * @Description //TODO
 * @Param
 * @Author kimi
 * @Date 2020/7/21
 * @return
 **/
@Data
public class CustomizedEventBeanVo {
    //事件code
    private String eventCode;

    //事件名
    private String eventName;

    //事件名英文
    private String eventEnName;

    /**事件类型*/
    private Integer eventType;
    /**
     * 是否被取消.0:uof 1:liveData
     */
    private Integer sourceType;

    //体育种类
    private Long sportId;

    //home 主 away 客
    private String homeAway;

    //当前时间
    private Integer currentTime;

    //当前比分
    private String score;

    //当前局比分
    private String setScore;

    //当前小分
    private String currentScore;

    /**
     * 对应data_source.code
     */
    private String dataSourceCode;
    /**
     * 事件时间
     */
    private Long eventTime;
    private Integer t1;
    private Integer t2;
    private Integer canceled;
    private String extraInfo;

    /**
     * 备注
     */
    private String remark;

    /**
     * 比赛阶段id.  system_item_dict.value
     */
    private Long matchPeriodId;
}
