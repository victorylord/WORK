package com.panda.sport.rcs.vo;

import com.panda.sport.rcs.pojo.RcsLabelSportVolumePercentage;
import lombok.Data;

import java.util.Collection;
import java.util.List;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-02-09 18:23
 **/
@Data
public class RcsLabelLimitConfigDataVo {
    private Collection<RcsLabelLimitConfigVo> rcsLabelLimitConfigVoList;
    private Long total;
    private Long getCurrent;
}
