import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.panda.sport.data.rcs.api.Response;
import com.panda.sport.data.rcs.api.tournament.TournamentTemplateService;
import com.panda.sport.data.rcs.dto.tournament.TournamentTemplateDTO;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSONObject;
import com.panda.sport.data.rcs.api.Request;
import com.panda.sport.data.rcs.dto.MarketCategoryCetBean;
import com.panda.sport.rcs.pojo.RcsMarketCategorySet;
import com.panda.sport.rcs.trade.Bootstrap;
import com.panda.sport.rcs.trade.service.RcsMarketCategorySetApiImpl;

import lombok.extern.slf4j.Slf4j;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  PACKAGE_NAME
 * @Description :  TODO
 * @Date: 2020-02-01 12:06
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Bootstrap.class)
@Slf4j
public class PlaySetTest {

    @Autowired
    RcsMarketCategorySetApiImpl marketCategorySetApiService;
    @Autowired
    TournamentTemplateService tournamentTemplateService;

    List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    @Test
    public void xxx() {
        Map<Integer, com.panda.sport.data.rcs.api.Response> putSportMarketCategorySetMap = new HashMap<>();
        Map<Integer, com.panda.sport.data.rcs.api.Response> putSportMarketCategorySetPlayMap = new HashMap<>();
        for (Integer sportId : list) {
            Request<MarketCategoryCetBean> requestParam = new Request<MarketCategoryCetBean>();
            MarketCategoryCetBean bean = new MarketCategoryCetBean();
            bean.setSportId(sportId.longValue());
            requestParam.setData(bean);
            com.panda.sport.data.rcs.api.Response res = marketCategorySetApiService.putSportMarketCategorySet(requestParam);

            List<RcsMarketCategorySet> listSet = (List<RcsMarketCategorySet>) res.getData();
            for (RcsMarketCategorySet set : listSet) {
                Request<Long> requestParam2 = new Request<Long>();
                requestParam2.setData(set.getId());
                com.panda.sport.data.rcs.api.Response res2 = marketCategorySetApiService.putSportMarketCategorySetPlay(requestParam2);
                putSportMarketCategorySetPlayMap.put(set.getId().intValue(), res2);
            }

            putSportMarketCategorySetMap.put(sportId, res);
        }

        System.out.println(JSONObject.toJSONString(putSportMarketCategorySetMap));
        System.out.println(JSONObject.toJSONString(putSportMarketCategorySetPlayMap));

    }

    @Test
    public void test() {
        Request<TournamentTemplateDTO> req = new Request<>();
        TournamentTemplateDTO dto = new TournamentTemplateDTO();
        dto.setStandardMatchId(1877201L);
        dto.setDataSourceCode("BC");
        req.setData(dto);
        Response rp = tournamentTemplateService.queryAcceptConfigByMatchId(req);
        log.info("rp:" + JsonFormatUtils.toJson(rp));
    }
    @Test
    public void test11() {
      String aa="5555";
      String bb=filterChar(aa);
      System.out.println(bb);
    }
    String filterChar(String strValue){
        String strChar="%%";
        if(strValue.contains(strChar)){
            return  strValue.replace(strChar,"%").trim();
        }else{
            return strValue;
        }
    }
}
