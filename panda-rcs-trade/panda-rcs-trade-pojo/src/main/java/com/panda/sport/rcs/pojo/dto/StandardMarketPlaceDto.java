package com.panda.sport.rcs.pojo.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import lombok.Data;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.pojo.dto
 * @Description : 新版盘口位置信息
 * @Author : Paca
 * @Date : 2020-10-03 20:58
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class StandardMarketPlaceDto extends StandardSportMarket {

    private static final long serialVersionUID = -233523553236222948L;

    /**
     * 位置ID
     */
    @TableField("place_id")
    private String placeId;

    /**
     * 盘口位置，1：表示主盘，2：表示第一副盘
     */
    @TableField("place_num")
    private Integer placeNum;

    /**
     * 盘口ID
     */
    @TableField("market_id")
    private Long marketId;

    /**
     * 版本号，查询的时候使用位置1的版本号做关联
     */
    @TableField("version_id")
    private String versionId;

}
