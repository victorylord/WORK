//package com.panda.sport.rcs.trade.callable;
//
//import com.panda.sport.rcs.enums.MatchLevelEnum;
//import com.panda.sport.rcs.pojo.StandardSportMarket;
//
//import java.util.HashMap;
//import java.util.concurrent.Callable;
//
///**
// * @author :  myname
// * @Project Name :  rcs-parent
// * @Package Name :  com.panda.sport.rcs.trade.callable
// * @Description :  TODO
// * @Date: 2020-01-18 18:55
// * @ModificationHistory Who    When    What
// * --------  ---------  --------------------------
// */
//public class RpcUpdateStatusTask implements Callable {
//    StandardSportMarket standardSportMarket;
//
//    public RpcUpdateStatusTask(StandardSportMarket standardSportMarket) {
//        this.standardSportMarket = standardSportMarket;
//    }
//
//    @Override
//    public Object call() throws Exception {
//        putTradeMarketConfig(MatchLevelEnum.MATCH_MARKET.getLevel(), rcsMatchPlayConfig.getPlayId().toString(), null, standardSportMarket.getId(), 1 - rcsMatchPlayConfig.getDataSource(),
//                new HashMap<>());
//    }
//}
