/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game.generate.model;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "agent")
public class Agent {
    @Id
    @Column(name = "id")
    private Long id;

    /**
     * 代理唯一标识
     */
    @Column(name = "agent")
    private String agent;

    /**
     * md5Key
     */
    @Column(name = "md5_key")
    private String md5Key;

    /**
     * apiKey
     */
    @Column(name = "api_key")
    private String apiKey;
}