package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 赔率转换映射表
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RcsOddsConvertMapping extends RcsBaseEntity<RcsOddsConvertMapping> {


    /**
     * 表ID，自增
     */
      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 欧洲赔率
     */
    private String europe;

    /**
     * 香港赔率
     */
    private String hongkong;

    /**
     * 马来赔率
     */
    private String malaysia;

    /**
     * 英赔率
     */
    private String unitedKingdom;

    /**
     * 美赔率
     */
    private String unitedStates;

    /**
     * 印尼率
     */
    private String indonesia;
}
