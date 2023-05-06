/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game.socket.message;

import org.apache.commons.lang3.StringUtils;

/**
 * WS前后端信息交互中opType类型
 */
public enum SocketMessageOpType {
    PING("PING"), PONG("PONG"), CHANGEBETMONEY("CHANGEBETMONEY"), BET("BET"), BETSTAKEEFFECT("BETSTAKEEFFECT");

    private String code;

    SocketMessageOpType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static SocketMessageOpType getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (SocketMessageOpType opType : SocketMessageOpType.values()) {
            if (opType.getCode().equals(code)) {
                return opType;
            }
        }
        return null;
    }

}
