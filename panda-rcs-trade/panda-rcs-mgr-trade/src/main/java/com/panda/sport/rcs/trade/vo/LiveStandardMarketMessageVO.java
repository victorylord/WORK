package com.panda.sport.rcs.trade.vo;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.panda.sport.rcs.utils.LongToStringSerializer;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName StandardMarketMessageVO
 * @Description: TODO
 * @Author Vector
 * @Date 2019/12/14
 **/
@Getter
@Setter
public class LiveStandardMarketMessageVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 标准比赛ID   standard_match_info.id
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long standardMatchInfoId;

    /**
     * 标准盘口id
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 标准玩法id   standard_sport_market_category.id
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long marketCategoryId;

    /**
     * 盘口类型. 属于赛前盘或者滚球盘. 1: 赛前盘; 0: 滚球盘.
     */
    private Integer marketType;

    /**
     * 该盘口具体显示的值. 例如: 大小球中, 大小界限是:  3.5
     */
    private String oddsValue;

    /**
     * 盘口名称,V1.2统一命名规则.
     */
    private String oddsName;

    /**
     * 排序类型
     */
    private String orderType;

    /**
     * 盘口级别，数字越小优先级越高
     */
    private Integer oddsMetric;
    /**
     * 盘口位置，1：表示主盘，2：表示第一副盘
     */
    private Integer placeNum;
    /**
     * 附加字段1
     */
    private String addition1;

    /**
     * 附加字段2
     */
    private String addition2;

    /**
     * 取值:  SR BC分别代表: SportRadar、FeedConstruc. 详情见data_source
     */
    private String dataSourceCode;

    /**
     * 盘口状态0-5. 0:active 开盘, 1:suspended 封盘, 2:deactivated 关盘, 3:settled 已结算, 4:cancelled 已取消, 5:handedOver  盘口的中间状态，该状态的盘口后续不会有赔率过来 11:锁盘状态
     */
    private Integer status;
    /**
     * 第三方盘口状态  原始盘口 状态 不能删  0是开盘状态，1是封盘，2是关盘 3是结算  11是锁盘
     */
    private Integer thirdMarketSourceStatus;

    /**
     * 操盘后台设置的位置状态，融合侧不做修改，可为空（操盘后台没有设置位置状态为空）
     */
    private Integer placeNumStatus;

    private Long modifyTime;

    /**
     * 盘口投注项
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    private List<LiveStandardMarketOddsMessageVO> marketOddsList;

    /**
     * 标记0：非全空  1：全空
     * @return
     */
    private Integer paOddsValueIsZero;

    /**
     * 设置值
     * @return
     */
    public void setPaOddsValueIsZero(){
        if(marketOddsList== null || marketOddsList.size() == 0){
            paOddsValueIsZero = 0;
            return;
        }

        for(LiveStandardMarketOddsMessageVO vo:marketOddsList){
            if(vo.getPaOddsValue() != 0){
                paOddsValueIsZero = 0;
                return;
            }
        }
        paOddsValueIsZero = 1;
    }

	@Override
	public String toString() {
		return JSONObject.toJSONString(this);
	}


}
