import com.panda.sport.data.rcs.api.Request;
import com.panda.sport.rcs.trade.Bootstrap;
import com.panda.sport.rcs.trade.service.RcsMarketCategorySetApiImpl;
import com.panda.sport.rcs.trade.wrapper.impl.MarketCategorySetServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  PACKAGE_NAME
 * @Description :  TODO
 * @Date: 2020-05-14 14:31
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Bootstrap.class)
@Slf4j
public class MarketCategorySetServiceImplTest {
    @Autowired
    private RcsMarketCategorySetApiImpl rcsMarketCategorySetApi;
    @Test
    public void test(){
        Request<Long> request = new Request<>();
        request.setData(1L);
        rcsMarketCategorySetApi.putSportMarketCategorySetPlay(request);
    }
}
