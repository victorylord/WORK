/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface BetDao {

    void updateAccount(@Param("account") String account,
                       @Param("betMoney") Long betMoney,
                       @Param("prize") Long prize);
}
