/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game.generate.model;

import javax.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name = "game_user")
public class GameUser {
    @Id
    @Column(name = "id")
    private Long id;

    /**
     * 账号
     */
    @Column(name = "account")
    private String account;
}