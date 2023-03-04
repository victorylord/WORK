package com.panda.sport.rcs.trade.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LogTypeEnum {

    TIMELYBET(1, "及时注单接拒"),
    TRADE_TYPE(2, "赛事复盘"),
    CHAMPION_TYPE(3, "冠军赛事");

    private Integer code;
    private String value;
}
