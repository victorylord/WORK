package com.panda.rcs.warning.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * @author :  koala
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.rcs.warning.vo
 * @Description :  监控预警配置
 * @Date: 2022-07-19 14:27
 * --------  ---------  --------------------------
 */
@Data
public class RcsMatchMonitorSetting implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String riskKey;
    private Long riskTime;
    private Long riskValue;
    //1早盘，0滚球
    private Integer matchType;
    //3.事件源未更新告警（Event feed Disconnect）
    private Integer dataType;
    //1 高危 2.中等 3.安全
    private Integer riskLevel;

}
