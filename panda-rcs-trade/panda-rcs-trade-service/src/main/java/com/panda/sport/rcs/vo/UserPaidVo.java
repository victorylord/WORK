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
 * @Description :  用户单日模型
 * @Date: 2019-10-07 17:44
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class UserPaidVo implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 商户id
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long businessId;
    /**
     * 项目名称
     */
    private String dayPay;
    /**
     * 额度
     */
    private BigDecimal dayPayValue;
}
