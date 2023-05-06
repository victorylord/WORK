/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game.model;

import java.util.List;

import lombok.Data;

@Data
public class BetOnce {
    List<Long> perm;
    Long score;
}
