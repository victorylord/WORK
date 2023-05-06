/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game.service;

import com.soft.game.model.BetResponse;
import com.soft.game.model.BetRequest;
import com.soft.game.model.BetStakEeffectRequest;
import com.soft.game.model.BetStakEeffectResponse;

public interface BetService {
    BetResponse bet(BetRequest betRequest);

    BetStakEeffectResponse betStakEeffect(BetStakEeffectRequest betStakEeffectRequest);
}
