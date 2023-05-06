/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import tk.mybatis.spring.annotation.MapperScan;

/**
 * Hello world!
 *
 * @author java-cmc
 * @version 1.0.0
 * @since 1.0.0
 */
@SpringBootApplication
@MapperScan(basePackages = "com.soft.game.generate.mapper")
public class GameApplication {
    public static void main(String[] args) {
        SpringApplication.run(GameApplication.class, args);
    }
}
