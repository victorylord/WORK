package com.panda.sport.rcs.trade.vo;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import com.panda.sport.rcs.utils.LongToStringSerializer;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 *
 * @Description  : 标准盘口与投注项消息
 * @author       :  Vito
 * @Date:  2019年10月7日 下午5:01:27
 * @ModificationHistory   Who    When    What
 * --------  ---------  --------------------------
 */
@Getter
@Setter
public class LiveStandardMatchMarketMessageVO extends RcsBaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     *  标准联赛ID
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long standardTournamentId;

    /**
     * 标准比赛ID   standard_match_info.id
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long standardMatchInfoId;

    /**
     * 取值:  SR BC分别代表: SportRadar、FeedConstruc. 详情见data_source
     */
    private String dataSourceCode;

    private Long modifyTime;

    /**
     *  比赛开盘标识
     *  0:active 开盘, 1:suspended 封盘, 2:deactivated 关盘, 3:settled 已结算, 4:cancelled 已取消, 5:handedOver  盘口的中间状态，该状态的盘口后续不会有赔率过来 11:锁盘状态
     */
    private Integer status;

    /**
     * 盘口投注项
     */
    private List<LiveStandardMarketMessageVO> marketList;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LiveStandardMatchMarketMessageVO that = (LiveStandardMatchMarketMessageVO) o;
        return Objects.equals(standardTournamentId, that.standardTournamentId) &&
                Objects.equals(standardMatchInfoId, that.standardMatchInfoId) &&
                Objects.equals(dataSourceCode, that.dataSourceCode) &&
                Objects.equals(modifyTime, that.modifyTime) &&
                Objects.equals(status, that.status) &&
                Objects.equals(marketList, that.marketList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(standardTournamentId, standardMatchInfoId, dataSourceCode, modifyTime, status, marketList);
    }

	@Override
	public String toString() {
		return JSONObject.toJSONString(this);
	}
    
    
}
