/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game.generate;

import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/**
 * Created by gongjiangchuan on 2017/4/14.
 */
public interface BaseMapper<T> extends Mapper<T>, MySqlMapper<T> {
}
