package com.panda.sport.rcs.vo.statistics;

import com.panda.sport.rcs.vo.CustomizedEventBeanVo;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.vo.statistics
 * @Description :  TODO
 * @Date: 2020-07-22 11:14
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class MatchEventVo {
    /**
     * 总页数
     **/
    private Integer pageNum;
    /**
     * 事件数据
     **/
    private List<CustomizedEventBeanVo> customizedEventBeanVoList;

    /**
     * 多数据源list
     */
    private Map<String,List<CustomizedEventBeanVo>> customizedEventBeanVoListMap;
}
