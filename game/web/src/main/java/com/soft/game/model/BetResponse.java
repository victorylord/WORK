/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game.model;

import java.util.List;

import lombok.Data;

@Data
public class BetResponse {
    List<Long> perm;
    Long score;
    private Long prize;
    private Long orderNo;
}
