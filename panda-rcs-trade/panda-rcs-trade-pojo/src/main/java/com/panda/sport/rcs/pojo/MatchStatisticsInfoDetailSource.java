package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.pojo
 * @Description :  TODO
 * @Date: 2020-09-25 13:58
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */

@Data
public class MatchStatisticsInfoDetailSource extends RcsBaseEntity<MatchStatisticsInfoDetailSource> {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;
    /**
     * 标准赛事id
     */
    private Long standardMatchId;
    /**
     * 编码
     */
    private String code;
    /**
     * 阶段
     */
    private Integer firstNum;
    /**
     * 小节
     */
    private Integer secondNum;
    /**
     *队1比分
     */
    private Integer t1;
    /**
     * 队2比分
     */
    private Integer t2;
    /**
     * 事件 统计 数据源
     */
    private String dataSourceCode;
}
