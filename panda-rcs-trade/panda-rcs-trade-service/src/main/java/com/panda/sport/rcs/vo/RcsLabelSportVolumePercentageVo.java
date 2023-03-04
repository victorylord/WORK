package com.panda.sport.rcs.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 标签百分比货量vo
 */
@Data
public class RcsLabelSportVolumePercentageVo {
    /**
     * 标签Id
     */
    private Integer tagId;

    /**
     * 体育种类
     */
    private Long sportId;

    /**
     * 货量百分比
     */
    private BigDecimal volumePercentage;
}
