package com.panda.sport.rcs.pojo.report;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @author :  enzo
 * @Project Name :  panda-rcs
 * @Package Name :  com.panda.rcs.flink.pojo
 * @Description :  日期所属 年期周
 * @Date: 2019-12-24 13:49
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RcsOrderStatisticDate extends RcsBaseEntity<RcsOrderStatisticDate> {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 日
     */
    private String orderDay;
    /**
     * 年
     */
    private String orderYear;
    /**
     * 期
     */
    private String orderPhase;
    /**
     * 周
     */
    private String orderWeek;
    /**
     * 创建时间
     */
    private Timestamp createTime;
    /**
     * 更新时间
     */
    private Timestamp updateTime;

}
