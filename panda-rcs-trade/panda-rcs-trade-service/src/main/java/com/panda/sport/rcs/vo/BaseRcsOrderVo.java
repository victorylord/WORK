package com.panda.sport.rcs.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author :  Administrator
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2019-12-26 15:47
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class BaseRcsOrderVo implements Serializable {
    /**
     * 赛种ID
     */
    private List<Integer> sportIds;
    /**
     * 联赛ID
     */
    private List<Integer> tournamentIds;
    /**
     * 查询全部联赛 是1 否0
     */
    private Integer allTournament;
    /**
     * 查询全部玩法 是1 否0
     */
    private Integer allPlayId;
    /**
     * [
     * 类型：1 ：早盘 ，2： 滚球盘， 3： 冠军盘
     */
    private List<Integer> matchTypes;
    /**
     * 玩法ID
     */
    private List<Integer> playIds;
    /**
     * 注单状态
     */
    private List<Integer> orderStatuses;

    /**
     * 时间类型设置 1.开始时间 2.投注时间 3.结算时间
     */
    private Integer settleTimeType;
    /**
     * 时间格式
     */
    private JSONObject jsonTimes;

    private List<TimeBeanVo> timeBeanVoList;

    /**
     * 日期类型 枚举DateTypeEnum
     */
    private Integer dateType;

    private Integer pageNo;

    private Integer pageSize;
    /**
     * 排序字段ID(1.时间粒度 2.维度 3.受注量 4.投注笔数 5.投注人数 6.单笔平均投注量 7.人均投注笔数  8.人均投注量 9.单笔>=1万的笔数
     * 10.单笔>=5千的笔数 11.单笔>=2千的笔数 12.单笔>=1千的笔数 13.单笔<1千的笔数 14.平台盈利 15.平台盈利百分比 )
     */
    private Integer sortId;
    /**
     * 是否升序
     */
    private Integer isAsc;
}
