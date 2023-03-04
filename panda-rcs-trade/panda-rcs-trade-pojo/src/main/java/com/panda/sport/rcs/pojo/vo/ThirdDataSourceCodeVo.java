package com.panda.sport.rcs.pojo.vo;

import lombok.Data;

/**
 * @author :  carver
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.vo.tourTemplate
 * @Description :  三方赛事数据源对象
 * @Date: 2021-01-14 12:12
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class ThirdDataSourceCodeVo {
    /**
     * 三方赛事数据源id
     */
    private String thirdMatchSourceId;
    /**
     * 三方赛事数据源编码
     */
    private String dataSourceCode;
    /**
     * 是否商业数据源  1：是  0：否
     */
    private String commerce;
    /**
     * 是否支持事件  1：是  0：否
     */
    private String eventSupport;  
}
