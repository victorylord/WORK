package com.panda.sport.rcs.vo;

import com.panda.sport.rcs.pojo.RcsLabelLimitConfig;
import lombok.Data;

import java.util.List;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-02-04 13:58
 **/
@Data
public class RcsLabelLimitConfigVo extends RcsLabelLimitConfig {
    private String name;
    private List<Integer> sportIdList;
    //标签货量百分比配置
    private List<RcsLabelSportVolumePercentageVo> sportVolumePercentageList;
}
