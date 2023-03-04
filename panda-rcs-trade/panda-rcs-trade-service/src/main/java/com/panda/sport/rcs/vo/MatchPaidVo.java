package com.panda.sport.rcs.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.panda.sport.rcs.utils.LongToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author :  Administrator
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  单场赛事最大赔付 模型
 * @Date: 2019-10-07 16:16
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class MatchPaidVo implements Serializable {
    /**
     * 商户ID
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long businessId;

    /**
     * 商户名称
     */
    private String businessName;
    /**
     * 足球赛事最大赔付
     */
    private BigDecimal football;
    /**
     * 足球赛事最大赔付
     */
    private BigDecimal basketball;
    /**
     * 网球赛事最大赔付
     */
    private BigDecimal tennis;
    /**
     * 电子竞技赛事最大赔付
     */
    private BigDecimal esports;
    /**
     * 新颖玩法赛事最大赔付
     */
    private BigDecimal newplay;
}
