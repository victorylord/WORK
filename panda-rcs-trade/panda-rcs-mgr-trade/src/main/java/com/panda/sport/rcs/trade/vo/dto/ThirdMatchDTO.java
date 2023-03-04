package com.panda.sport.rcs.trade.vo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Mirro
 * @Project Name :  panda_data_nonrealtime
 * @Package Name :  com.panda.sport.data.nonrealtime.api.query.bo
 * @Description:
 * @date 2019/10/24 15:23
 * @ModificationHistory Who    When    What
 */
@Data
public class ThirdMatchDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 第三方赛事原始id.比如: SportRadar 发送数据时,这场比赛的ID.
     */
    private String thirdMatchSourceId;

    /**
     * 数据来源编码.取值: SR BC分别代表:SportRadar、FeedConstruc.详情见data_source
     */
    private String dataSourceCode;


    /** 是否商业数据源(0:否,1:是)*/
    private Integer commerce;

    /** 是否支持事件(0:否,1:是)*/
    private Integer eventSupport;

}
