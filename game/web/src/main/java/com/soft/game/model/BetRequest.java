/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game.model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class BetRequest {
    private String account;// 账户
    private Long totalBet;// 总投注数
    private Long betMultiplier;// 投注乘数
}