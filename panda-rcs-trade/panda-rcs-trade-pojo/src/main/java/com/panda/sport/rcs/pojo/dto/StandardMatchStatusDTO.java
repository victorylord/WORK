package com.panda.sport.rcs.pojo.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.panda.sport.rcs.utils.LongToStringSerializer;
import lombok.Data;

import java.io.Serializable;

/**
 * @author :  charls
 * @Project Name :  panda-rcs-service
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2019-10-07 16:48
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class StandardMatchStatusDTO implements Serializable{


private static final long serialVersionUID = 1L;

    /**
     * 标准赛事ID
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long standardMatchId;

    /**
     * 运动种类id。 对应sport.id
     * 如果玩法不区分体育类型，传0，否则传对应体育类型标识
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long sportId;

    /**
     * 比赛开始时间. 比赛开始时间 UTC时间
     */
    private Long beginTime;

    /**
     * 赛事状态.
     * 字典数据，对应 parent_type_id = 5
     */
    private Integer matchStatus;

    /**
     * 是否为中立场. 取值为 0  和1  .   1:是中立场, 0:非中立场. 操盘人员可手动处理
     */
    private Integer neutralGround;

    /**
     * 数据来源编码. 取值见: data_source.code
     */
    private String dataSourceCode;

    /**
     * 第三方赛事原始id. 该厂比赛在第三方数据供应商中的id. 比如:  SportRadar 发送数据时, 这场比赛的ID.
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    private String thirdMatchSourceId;

    /**
     * 父赛事id
     */
    private Long parentId;

    /**
     * 赛事可下注状态. 0: betstart; 1: betstop
     */
    private Integer betStatus;

    /**
     * 比赛阶段
     * 字典数据，
     * 对应 parent_type_id = 8
     */
    @JSONField(serializeUsing = LongToStringSerializer.class)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long matchPeriodId;


    private MatchStatisticsInfoDTO matchStatisticsInfo;
}
