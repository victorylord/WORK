package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 操盘配置和赛程配置
 * </p>
 *
 * @author Black
 * @since 2019-11-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RcsTradeMarketConfig extends Model<RcsTradeMarketConfig> {

	@TableId(type = IdType.INPUT)
    private String configId;

    private Integer level;

    @TableField("targetId")
    private Long targetId;

    private Integer active;

    private Integer marketStatus;

    private Long modifyTime;

    private Integer sourceSystem;

    private String addition1;

    private String addition2;

    private String addition3;


    @Override
    public Serializable pkVal() {
        return this.configId;
    }

}
