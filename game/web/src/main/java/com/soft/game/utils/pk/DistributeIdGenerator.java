/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game.utils.pk;

import com.soft.game.utils.RedisUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * DistributeIdGenerator
 *
 * @author huangsiping
 */
@Slf4j
public class DistributeIdGenerator {
    private static final String MACHINE_NO_SIGN_KEY = "MACHINE_NO_SIGN_KEY";

    SnowflakeSequence iw = null;

    private static Long nodeIndex = 0L;

    private DistributeIdGenerator() {
        nodeIndex = RedisUtil.incr(MACHINE_NO_SIGN_KEY) % 32;
        log.info("This machine no is {}", nodeIndex);
        if (null != nodeIndex && nodeIndex > -1) {
            iw = new SnowflakeSequence(nodeIndex, 0,
                    System.currentTimeMillis());
        } else {
            iw = new SnowflakeSequence(System.currentTimeMillis());
        }
    }

    private static DistributeIdGenerator instance = null;

    public static synchronized DistributeIdGenerator getInstance() {
        if (instance == null) {
            instance = new DistributeIdGenerator();
        }
        return instance;
    }

    public long nextId() {
        return iw.getId();
    }
}
