package com.panda.sport.rcs.trade.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HandleStatusEnum {

    THREE("3", "取消"),
    FOUR("4", "手动接单"),
    FIVE("5", "手动拒单"),
    EIGHT("8", "暂停接单"),
    NINE("9", "暂停拒单"),
    TEN("10", "暂停忽略"),
    ;

    private String code;
    private String value;

    public static String codeValue(String code) {
        for (HandleStatusEnum statusEnum : HandleStatusEnum.values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum.getValue();
            }
        }
        return null;
    }
}
