/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game.socket.message;

import java.io.Serializable;

import lombok.Data;

@Data
public class MessageOperateType implements Serializable {
    private static final long serialVersionUID = 5744599721648521251L;
    private String opType;
    private Long msgId;

    public MessageOperateType(String opType) {
        this.opType = opType;
    }

    public MessageOperateType() {
    }

    public String getMsgId() {
        return String.valueOf(msgId);
    }

    public Long getNumMsgId() {
        return msgId;
    }
}
