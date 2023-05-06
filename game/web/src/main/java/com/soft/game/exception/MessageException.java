/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * Description Created by shuxiaogang date on 2018/7/17.
 */
@Getter
@Setter
public class MessageException extends RuntimeException {
    private String status;

    public MessageException(Throwable e) {
        super(e);
    }

    public MessageException(String message) {
        super(message);
        this.status = "fail";
    }

    public MessageException(String status, String message) {
        super(message);
        this.status = status;
    }
}
