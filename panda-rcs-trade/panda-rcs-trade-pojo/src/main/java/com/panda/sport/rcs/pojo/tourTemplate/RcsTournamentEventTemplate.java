package com.panda.sport.rcs.pojo.tourTemplate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.io.Serializable;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo.tourTemplate
 * @Description :  联赛事件模板
 * @Date: 2020-07-17 16:46
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RcsTournamentEventTemplate implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 赛种
     */
    private Integer sportId;
    /**
     * 序号
     */
    private Integer orderNo;
    /**
     * 事件编码
     */
    private String eventCode;
    /**
     * 事件文本模板
     */
    private String templateText;
    /**
     * 事件审核倒计时时间默认值
     */
    private Integer auditTime;
    /**
     * 事件结算倒计时时间默认值
     */
    private Integer billTime;
}
