package com.panda.sport.rcs.trade.vo;

import com.panda.sport.rcs.pojo.RcsLabelLimitConfig;
import com.panda.sport.rcs.pojo.RcsLabelSportVolumePercentage;
import com.panda.sport.rcs.pojo.TUserLevel;
import com.panda.sport.rcs.vo.RcsLabelLimitConfigVo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: 风控措施管理日志
 * @description:
 * @author: skyKong
 * @create:
 **/
@Data
public class BusinessLogVo {
    /**
     * 旧数据
     */
    List<RcsLabelSportVolumePercentage> oldLabelSportVolumePercentages=new ArrayList();
    /**
     *新数据
     */
    List<RcsLabelSportVolumePercentage> newsLabelSportVolumePercentages=new ArrayList<>();

    /**
     * 旧数据
     */
    List<RcsLabelLimitConfig> oldLabelLimitConfigs=new ArrayList<>();
    /**
     * 新数据
     * */
    List<RcsLabelLimitConfigVo> newsLabelLimitConfigs=new ArrayList<>();

    /**
     * 操作用户ID
     * */
    private String userId;

    private List<TUserLevel> tUserLevels=new ArrayList<>();

}
