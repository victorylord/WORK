package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

/**
 * rcs_market_category_set_margin表
 * @author :  Felix
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo
 * @Description :  TODO
 * @Date: 2019-10-04 16:48
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RcsMarketCategorySetMargin  extends RcsBaseEntity<RcsMarketCategorySetMargin> {
    /**
     * 数据库id，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 时间节点(换算成按小时存)
     */
    private Integer timeFrame;

    /**
     * 抽水值
     */
    private Integer margin;

    /**
     * 玩法集ID
     */
    private Long marketCategorySetId;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 修改时间
     */
    private Long modifyTime;
}
