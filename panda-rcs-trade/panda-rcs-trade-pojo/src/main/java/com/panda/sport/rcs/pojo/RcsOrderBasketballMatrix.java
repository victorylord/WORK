package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-01-13 17:01
 **/
@Data
public class RcsOrderBasketballMatrix extends RcsBaseEntity<RcsOrderBasketballMatrix> {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 商户id
     */
    private Long tenantId;
    /**
     * 订单号
     */
    private Long orderNo;
    /**
     * 运动种类
     */
    private Long sportId;
    /**
     * 联赛id
     */
    private Long tournamentId;
    /**
     * 标准赛事id
     */
    private Long matchId;
    /**
     * 赛事类型:1赛前,2滚球
     */
    private Long matchType;
    /**
     * 玩法id
     */
    private Long playId;
    /**
     * 分差中值/总分始值
     */
    private Long initMarket;
    /**
     * 矩阵数据
     */
    private String recVal;
}
