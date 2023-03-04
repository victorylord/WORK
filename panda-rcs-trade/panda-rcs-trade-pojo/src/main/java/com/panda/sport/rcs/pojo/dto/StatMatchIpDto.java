package com.panda.sport.rcs.pojo.dto;

import lombok.Data;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo
 * @Description :  统计ip
 * @Date: 2020-06-07 16:27
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class StatMatchIpDto {
    /**
     * @Description 赛事id
     * @Param
     * @Author toney
     * @Date 16:28 2020/6/7
     * @return
     **/
    private Long matchId;
    /**
     * ip前三段地址
     */
    private String ipAddr;
    /**
     * 数量
     */
    private Integer nums;
    /**
     * 总记录数
     */
    private Integer total;
    /**
     * 排第几行
     */
    private Integer rownum;

    /**
     * 获取颜色级别
     *
     * @return
     */
    public Integer getColorLevel() {
        Integer position = Math.round(rownum.floatValue() / total.floatValue());
        if (position <= 0.1) {
            return 2;
        } else if (position <= 0.2) {
            return 1;
        } else {
            return 0;
        }
    }
}
