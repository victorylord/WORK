package com.panda.sport.rcs.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import com.panda.sport.rcs.pojo.statistics.RcsProfitRectangle;
import lombok.Data;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 *  赛事
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-07
 */
@Data
/*@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)*/
public class StandardMatchInfoVo extends RcsBaseEntity<StandardMatchInfoVo> {

    private static final long serialVersionUID = 1L;

    /**
     * id. id
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * 体育种类id. 运动种类id 对应sport.id
     */
    private Long sportId;

    /**
     * 标准联赛 id. 对应联赛 id  对应  standard_sport_tournament.id
     */
    private Long standardTournamentId;

    /**
     * 第三方比赛id. 第三方比赛在 表 third_match_info 中的id
     */
    private Long thirdMatchId;

    /**
     * 开赛后的时间. 单位:秒.例如:3分钟11秒,则该值是 191
     */
    private Integer secondsMatchStart;

    /**
     * 事件发生时间. UTC时间
     */
    private Long eventTime;

    /**
     * 赛事是否开放赛前盘. 取值为 1  或  0.  1=开放; 0=不开放
     */
    private Integer preMatchBusiness;

    /**
     * 赛事是否开放滚球. 取值为 1  或  0.  1=开放; 0=不开放
     */
    private Integer liveOddBusiness;

    /**
     * 比赛开盘标识. 0: 未开盘; 1: 开盘; 2: 关盘; 3: 封盘; 开盘后用户可下注
     */
    private Integer operateMatchStatus;

    /**
     * 比赛开始时间. 比赛开始时间 UTC时间
     */
    private Long beginTime;

    /**
     * 比赛是否被激活. 1: 激活; 0: 未激活.  激活的比赛可以进行下注. 
     */
    private Integer active;

    /**
     * 赛前盘下注状态. 赛前盘: 1 可下注; 0不可下注; 用于数据源控制下注状态
     */
    private Integer preMatchBetStatus;

    /**
     * 滚球下注状态. 滚球中使用: 1 可下注; 0不可下注; 用于数据源控制下注状态
     */
    private Integer liveOddsBetStatus;

    /**
     * 赛事状态.  比如:未开赛, 滚球, 取消, 延迟等. 
     */
    private Integer matchStatus;

    /**
     * 赛前滚球状态
     */
    private Integer oddsLive;

    /**
     * 是否为中立场. 取值为 0  和1  .   1:是中立场, 0:非中立场. 操盘人员可手动处理
     */
    private Integer neutralGround;

    /**
     * 标准赛事编码. 用于管理的赛事id
     */
    private String matchManageId;

    /**
     * 比赛场地名称,仅限中文. 用于查看mysql 时 使用. 
     */
    private String matchPositionName;

    /**
     * 比赛场地的国际化编码
     */
    private Long matchPositionNameCode;

    /**
     * 风控服务商编码. sr bc pa 等. 详见 数据源表 data_source中的code字段
     */
    private String riskManagerCode;

    /**
     * 数据来源编码. 取值见: data_source.code
     */
    private String dataSourceCode;

    /**
     * 关联数据源编码列表.  数据样例: SR,BC,188; SR,188; BC,188
     */
    private String relatedDataSourceCoderList;

    /**
     * 关联数据源数量
     */
    private Integer relatedDataSourceCoderNum;

    /**
     * 数据来源编码. 取值见: data_source.code
     */
    private String matchDataProviderCode;

    /**
     * 第三方赛事原始id. 该厂比赛在第三方数据供应商中的id. 比如:  SportRadar 发送数据时, 这场比赛的ID. 
     */
    private String thirdMatchSourceId;

    /**
     * 赛事双方的对阵信息.格式:主场队名称 VS 客场队名称
     */
    private String homeAwayInfo;

    /**
     * 父赛事id
     */
    private Long parentId;

    /**
     * 赛事可下注状态. 0: betstart; 1: betstop
     */
    private Integer betStatus;

    /**
     * 赛事包含的所有球队多语言信息,json串,冗余字段,用于赛程页面查询
     */
    private String teamName;

    /**
     * 赛事包含的所有球队id信息,json串,冗余字段,用于赛程页面查询
     */
    private String teamManageId;

    /**
     * 比赛阶段id. 取自基础表 : match_status.id
     */
    private Long matchPeriodId;

    /**
     * 备注. 
     */
    private String remark;

    /**
     * 创建时间. 
     */
    private Long createTime;

    /**
     * 修改时间. 
     */
    private Long modifyTime;

    /**
     * 联赛 级别。 对应标准联赛表的联赛级别
     */
    @TableField(exist = false)
    private Integer tournamentLevel;

    /**
     * 联赛名称编码. 联赛名称编码. 用于多语言
     */
    @TableField(exist = false)
    private Long nameCode;

    /**
     * 操盘类型 0:自动操盘 1:手动操盘 2:智能操盘
     */
    @TableField(exist = false)
    private List<Integer> tradeType;
    
    /**
     * 赛前操盘平台 MTS,PA
     */
    private String preRiskManagerCode;
    /**
     * 滚球操盘平台 MTS,PA
     */
    private String liveRiskManagerCode;
    /**
     * 第三方赛事信息
     */
    private String thirdMatchListStr;

    /**
     * 删除  0:否 1:是
     */
    @TableField(exist = false)
    private Integer isDelete;

    @TableField(exist = false)
    private List<RcsProfitRectangle> rcsProfitRectangleList;

    @TableField(exist = false)
    private String businessEvent;

    /**
     * 局数
     */
    @TableField(exist = false)
    private Integer setNum;

    @TableField(exist = false)
    private Integer roundType;

    /**
     * 比赛时长
     */
    private Integer matchLength;

    private String eventCode;
    /**
     * ao数据源id
     */
    private String aoId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StandardMatchInfoVo that = (StandardMatchInfoVo) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }

    public enum matchType{
        PREMATCH(1),SCROLLMATCH(2),TURENANTMATCH(3);
        private Integer value;
        matchType(Integer value) {
            this.value = value;
        }
        public Integer getValue() {
            return value;
        }

    }

    public enum settleStatusType{
        UNSETTLE(0),SETTLED(1);
        private Integer value;
        settleStatusType(Integer value) {
            this.value = value;
        }
        public Integer getValue() {
            return value;
        }
    }
}
