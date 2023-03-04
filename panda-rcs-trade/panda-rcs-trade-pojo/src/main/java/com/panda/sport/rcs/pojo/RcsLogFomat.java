package com.panda.sport.rcs.pojo;

import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

@Data
public class RcsLogFomat extends RcsBaseEntity<RcsLogFomat> {
    /**
     * 主键
     */
    private Long id;

    private String logType;

    private String oldVal;

    private String uid;

    private String logDesc;

    private String dynamicBean;

    private String name;

    private String logId;

    private String newVal;

    private String createTime;

    private String ctrTime;
}