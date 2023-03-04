package com.panda.rcs.warning.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * @author :  myname
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.rcs.warning.vo
 * @Description :  TODO
 * @Date: 2022-07-22 20:29
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RcsMatchMonitorMqLicense implements Serializable {
    @TableId()
    private Long  id;
    private Long matchId;
    private Integer playId;
    private Long eventTime;
    private Long updateTime;
    private Integer matchType;
}
