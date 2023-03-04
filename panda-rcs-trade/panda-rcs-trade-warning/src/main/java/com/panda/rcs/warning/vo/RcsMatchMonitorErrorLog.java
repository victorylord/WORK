package com.panda.rcs.warning.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author :  myname
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.rcs.warning.vo
 * @Description :  TODO
 * @Date: 2022-07-30 16:09
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RcsMatchMonitorErrorLog implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    // 标准赛事Id
    @TableField("match_id")
    private Long matchId;
    // 赛事Id
    @TableField("match_manage_id")
    private String matchManageId;
    //玩法id
    private Integer playId;
    //玩法编码
    private Long playCode;
    //玩法名称
    @TableField(exist = false)
    private String playName;
    //联赛编码
    private Long tourCode;
    //联赛名称
    @TableField(exist = false)
    private String tourName;
    //主队编码
    private Long homeTeamCode;
    @TableField(exist = false)
    private String homeTeamName;
    //客队编码
    private Long awayTeamCode;
    @TableField(exist = false)
    private String awayTeamName;
    //断链时间
    private Long eventTime;
    //恢复断链时间
    private Long recoverTime;
    //创建时间
    private Date createTime;
}
