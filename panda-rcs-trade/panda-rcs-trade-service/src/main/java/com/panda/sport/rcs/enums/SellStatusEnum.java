package com.panda.sport.rcs.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 开售状态
 * @Author : Paca
 * @Date : 2022-06-27 14:56
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Getter
@AllArgsConstructor
public enum SellStatusEnum {

    UNSOLD("Unsold", "未售"),
    OVERDUE_UNSOLD("Overdue_Unsold", "逾期未售"),
    APPLY_DELAY("Apply_Delay", "申请延期"),
    SOLD("Sold", "开售"),
    APPLY_STOP_SOLD("Apply_Stop_Sold", "申请停售"),
    STOP_SOLD("Stop_Sold", "停售"),
    EXPECTED_END_SOLD("Expected_End_Sold", "意外停售");

    private String status;
    private String name;

    public boolean isYes(String status) {
        return this.getStatus().equalsIgnoreCase(status);
    }
}
