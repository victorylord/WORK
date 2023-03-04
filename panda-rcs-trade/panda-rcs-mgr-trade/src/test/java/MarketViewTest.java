import com.panda.sport.rcs.trade.controller.MarketViewController;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSONObject;
import com.panda.sport.rcs.pojo.dto.odds.MatchOddsConfig;
import com.panda.sport.rcs.trade.Bootstrap;
import com.panda.sport.rcs.trade.mq.impl.TradeMatchOddsConsumer;

import lombok.extern.slf4j.Slf4j;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  PACKAGE_NAME
 * @Description :  TODO
 * @Date: 2020-02-10 21:37
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Bootstrap.class)
@Slf4j
public class MarketViewTest {
    @Autowired
    private TradeMatchOddsConsumer tradeMatchOddsConsumer;


    @Test
    public void test() {
        String text = "{\"matchId\": \"1390017\",\"playConfigList\": [{\"oddsType\": \"MY\",\"placeConfig\": [{\"maxOdds\": \"101.0\",\"minOdds\": \"1.01\",\"placeNum\": 1,\"spread\": \"0.10\"},{\"maxOdds\": \"101.0\",\"minOdds\": \"1.01\",\"placeNum\": 2,\"spread\": \"0.24\"},{\"maxOdds\": \"101.0\",\"minOdds\": \"1.01\",\"placeNum\": 3,\"spread\": \"0.30\"}],\"playId\": \"39\"}]}";
        MatchOddsConfig matchConfig = JSONObject.parseObject(text, MatchOddsConfig.class);

        tradeMatchOddsConsumer.handleMs(matchConfig);
    }

    @Autowired
    private MarketViewController marketViewController;

    @Test
    public void testUpdateMarketStatus() {
        String str = "{\"marketStatus\":0,\"matchId\":\"3163272\",\"sportId\":\"1\",\"tradeLevel\":1,\"teamList\":[{\"id\":\"115366\",\"names\":{\"en\":\"Qatar\",\"zs\":\"卡塔尔\"},\"nameCode\":1751791,\"matchPosition\":\"home\",\"redCardNum\":null,\"dangerTeam\":null},{\"id\":\"111631\",\"names\":{\"en\":\"Ecuador\",\"zs\":\"厄瓜多尔\"},\"nameCode\":1696614,\"matchPosition\":\"away\",\"redCardNum\":null,\"dangerTeam\":null}],\"operatePageCode\":15,\"matchManageId\":\"4370911354823163272\",\"beforeParams\":{\"marketStatus\":0}}";
        MarketStatusUpdateVO req = JSONObject.parseObject(str, MarketStatusUpdateVO.class);
        marketViewController.updateMarketStatus(req);
    }
}
