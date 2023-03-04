package com.panda.sport.rcs.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.trade.vo
 * @Description :  TODO
 * @Date: 2020-07-08 16:02
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class MatchSnapShotVo {
    private List<MarketSnapShotVo> marketSnapShotVoList1 = new ArrayList<>();
    private List<MarketSnapShotVo> marketSnapShotVoList2 = new ArrayList<>();
    private List<MarketSnapShotVo> marketSnapShotVoList3 = new ArrayList<>();
    private List<MarketSnapShotVo> marketSnapShotVoList4 = new ArrayList<>();
    private List<MarketSnapShotVo> marketSnapShotVoList5 = new ArrayList<>();
}
