package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 标准玩法表
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-07
 */
@Data
/*@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)*/
public class StandardSportMarketCategory extends RcsBaseEntity<StandardSportMarketCategory> {

    private static final long serialVersionUID = 1L;

    public static List firstHalfMatchCategorys = Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 29);

    public static List unCalcMatchCategorys = Arrays.asList(16, 25, 26, 27, 32, 33, 34);
    /**
     * 表ID, 自增
     */
    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    /**
     * 运动种类id.  对应表 sport.id
     */
    private Long sportId;

    /**
     * 例如: total
     */
    private String type;

    /**
     * 玩法标识.
     */
    private String typeIdentify;

    /**
     * 激活. 激活为1, 否则为 0. 默认 1
     */
    private Integer active;

    /**
     * 玩法名称编码. 用于多语言.
     */
    private Long nameCode;

    /**
     * 玩法状态. 0已关闭; 1已创建; 2待二次校验; 3已开启; .  默认已创建
     */
    private Integer status;

    /**
     * 是否属于多盘口玩法. 0no; 1yes.  默认no
     */
    private Integer multiMarket;

    /**
     * 排序值.
     */
    private Integer orderNo;

    /**
     * 投注项数量
     */
    private Integer fieldsNum;

    /**
     * 附件字段1
     */
    private String addition1;

    /**
     * 附件字段2
     */
    private String addition2;

    /**
     * 附件字段3
     */
    private String addition3;

    /**
     * 附件字段4
     */
    private String addition4;

    /**
     * 附件字段5
     */
    private String addition5;

    /**
     * 赔率切换system_item_dict.id列表  保存多个用逗号隔开
     */
    private String oddsSwitch;

    /**
     * 选项展示 Yes 展示 No 关闭
     */
    private String optionToShow;

    /**
     * 模板展示
     */
    private Long templateShowing;

    /**
     * 所属时段 system_item_dict.id
     */
    private Long theirTime;

    /**
     * 玩法构成盘口的数据格式. 例如:  Total [total] in 15 minutes interval [from]-[to] 玩法下:  当前字段的值是 from/to/total
     */
    private String dataFormate;

    /**
     * 玩法详细说明
     */
    private String description;

    /**
     * 备注.长度不超过130个字符.
     */
    private String remark;

    /**
     * 创建时间. UTC时间, 精确到毫秒
     */
    private Long createTime;

    /**
     * 更新时间. UTC时间, 精确到毫秒
     */
    private Long modifyTime;

    private String categoryNameCode;

    /**
     * PC模板展示
     */
    private Integer templatePc;

    /**
     * h5模板展示
     */
    private Integer templateH5;



    @TableField(exist = false)
    private Map<String, Object> language;

    @TableField(exist = false)
    private Long relId;

    @TableField(exist = false)
    private String displayStyle;




    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StandardSportMarketCategory that = (StandardSportMarketCategory) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }
}
