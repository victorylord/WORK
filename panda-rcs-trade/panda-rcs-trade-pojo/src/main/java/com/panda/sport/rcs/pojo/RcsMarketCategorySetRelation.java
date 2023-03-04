package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author :  myname
 * @Project Name :  panda-rcs
 * @Package Name :  com.panda.sport.rcs.pojo
 * @Description :  TODO
 * @Date: 2019-09-10 10:51
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
/**
 * 玩法集玩法关联表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RcsMarketCategorySetRelation extends RcsBaseEntity<RcsMarketCategorySetRelation> {
    /**
     * 表ID，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 玩法集id
     */
    private Long marketCategorySetId;
    /**
     * 玩法id
     */
    private Long marketCategoryId;
    /**
     * 排序值。
     */
    private Integer orderNo;
    /**
     * 创建时间. UTC时间，精确到毫秒
     */
    private Long createTime;

}
