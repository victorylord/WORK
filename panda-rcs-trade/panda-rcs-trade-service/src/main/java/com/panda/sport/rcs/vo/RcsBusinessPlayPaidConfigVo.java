package com.panda.sport.rcs.vo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;


import java.io.Serializable;


/**
 * <p>
 * 玩法维度配置
 * </p>
 *
 * @author holly
 * @since 2019-10-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class RcsBusinessPlayPaidConfigVo  implements  Serializable{

    /**
     * 最高单注赔付
     */
    private Long playId;
    /**
     * 最高单注赔付
     */
    private Long orderMaxPay;

    /**
     * 用户最高玩法赔付
     */
    @JsonIgnore
    private Long playMaxPay;

    /**
     * 玩法类型 全场 3，上半场 1，下半场 2，0-15分钟 4
     */
    @JsonIgnore
    private Long playType;

    /**
     * 用户最低玩法赔付
     */
    private Long minBet;
    
    /**
     * 复式串关，拆分类型
     */
    private String type;
    
    /**
     * 串关   验证是否通过
     */
    private Boolean isPass;
    
    /**
     * 串关   不通过原因
     */
    private String errorMsg;
}
