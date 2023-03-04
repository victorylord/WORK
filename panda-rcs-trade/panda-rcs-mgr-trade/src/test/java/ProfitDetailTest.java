import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.trade.Bootstrap;
import com.panda.sport.rcs.trade.controller.ProfitDetailController;
import com.panda.sport.rcs.trade.vo.profit.ProfitDetailParam;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  PACKAGE_NAME
 * @Description :  TODO
 * @Date: 2020-03-05 20:30
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Bootstrap.class)
@Slf4j
public class ProfitDetailTest {
    @Autowired
    private ProfitDetailController profitDetailController;

    @Test
    public void test()
    {
        ProfitDetailParam param =new ProfitDetailParam();

        param.setDate(1582300800000L);
        param.setMatchType(1);
        param.setSprotId(1L);

        List<Long> ids =new ArrayList<>();
        ids.add(3374L);
        ids.add(1343L);
        param.setStandardTournamentIds(ids);
        param.setOtherMorningMarket(0);
        System.out.println(JsonFormatUtils.toJson(profitDetailController.getList(param)));
    }
}
