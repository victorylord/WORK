package com.panda.sport.rcs.pojo.tourTemplate;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author :  Sean
 * @Project Name :  panda-rcs-trade
 * @Package Name :  com.panda.sport.rcs.pojo.tourTemplate
 * @Description :  TODO
 * @Date: 2020-09-05 13:27
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class BaseTournament implements Serializable {

    private static final long serialVersionUID = 1L;
    /**赛事种类*/
    @TableField(exist = false)
    private Integer sportId;

    /**赛事id*/
    @TableField(exist = false)
    private Long matchId;
}
