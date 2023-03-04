package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 玩法集表
 */
@Data
public class RcsMarketCategorySet extends RcsBaseEntity<RcsMarketCategorySet> {

    private static final long serialVersionUID = 1L;
    /**
     * 数据库id，自增
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 运动种类id。 对应表 sport.id
     */
    private Long sportId;

    /**
     * 玩法集类型。0展示型；1风控型。 默认展示型
     */
    private Integer type;

    /**
     * 玩法集名称
     */
    private String name;

    /**
     * 玩法集编码
     */
    @TableField("play_set_code")
    private String playSetCode;

    /**
     * 业务需要名称
     */
    @TableField(exist = false)
    private String marketName;

    /**
     * 关联联赛等级。1: 一级联赛；2:二级联赛；3：三级联赛；以此类推；
     */
    private Integer tournamentLevel;

    /**
     * 排序值。
     */
    private Integer orderNo;

    /**
     * 返回率。*10000
     */
    private Integer returnRate;

    /**
     * 玩法状态。0已创建；1待二次校验；2已开启；3已关闭。 默认已创建
     */
    private Integer status;

    /**
     * 备注.长度不超过130个字符。
     */
    private String remark;

    /**
     * 创建时间. UTC时间，精确到毫秒
     */
    private Long createTime;

    /**
     * 更新时间. UTC时间，精确到毫秒
     */
    private Long modifyTime;

    /**
     * 操盘玩法集展示顺序
     */
    @TableField("display_sort")
    private Integer displaySort;

    /**
     * 多语言编码
     */
    private String nameCode;

    /**
     * 阶段
     */
    private Integer period;

    /**
     * 多语言
     */
    @TableField(exist = false)
    private Map language;

    /**
     * 玩法集下的玩法数量
     */
    @TableField(exist = false)
    private Integer marketCategoryCount;

    /**
     * 玩法集管理====》点击具体的玩法前的+ 显示玩法列表
     */
    @TableField(exist = false)
    List<StandardSportMarketCategory> categoryList;

    /**
     * 抽水
     */
    @TableField(exist = false)
    LinkedHashMap<String, RcsMarketCategorySetMargin> margin;

}
