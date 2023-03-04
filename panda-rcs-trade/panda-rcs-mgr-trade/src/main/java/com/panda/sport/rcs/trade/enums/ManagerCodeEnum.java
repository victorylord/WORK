package com.panda.sport.rcs.trade.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操盘平台
 */
@Getter
@AllArgsConstructor
public enum ManagerCodeEnum {
    PA("PA", "Panda操盘平台"),
    MTS("MTS", "MTS操盘平台");

    private String id;
    private String name;

    public boolean isYes(String managerCode) {
        return this.getId().equalsIgnoreCase(managerCode);
    }

}
