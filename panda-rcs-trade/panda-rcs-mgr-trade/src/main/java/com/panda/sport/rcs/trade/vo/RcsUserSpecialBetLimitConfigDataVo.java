package com.panda.sport.rcs.trade.vo;

import com.panda.sport.rcs.pojo.RcsUserSpecialBetLimitConfig;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-01-29 16:49
 **/
@Data
public class RcsUserSpecialBetLimitConfigDataVo implements Serializable {
    /**
     *  0 无  1标签限额  2特殊百分比限额 3特殊单注单场限额 4特殊vip限额
     */
    private Integer specialBettingLimitType;
    /**
     * 这个里面的数据
     */
    private List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList1=new ArrayList<>();
    /**
     * 串关的数据
     */
    private List<RcsUserSpecialBetLimitConfig> rcsUserSpecialBetLimitConfigList2=new ArrayList<>();
}
