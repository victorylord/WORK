//import com.panda.sport.rcs.enums.MarketStatusEnum;
//import com.panda.sport.rcs.mapper.RcsMatchConfigMapper;
//import com.panda.sport.rcs.mapper.RcsMatchMarketConfigLogsMapper;
//import com.panda.sport.rcs.pojo.RcsMatchPlayConfig;
//import com.panda.sport.rcs.trade.Bootstrap;
//import com.panda.sport.rcs.trade.wrapper.impl.MarketViewServiceImpl;
//import com.panda.sport.rcs.vo.RcsMatchConfigVo;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = Bootstrap.class)
//@Slf4j
//public class MongoTest {
//
//	//@Reference(check = false, lazy=true,retries=3,timeout=10000)
//	@Autowired
//	private MarketViewServiceImpl marketViewServiceImpl;
//	@Autowired
//	private RcsMatchMarketConfigLogsMapper rcsMatchMarketConfigLogsMapper;
//	@Autowired
//	private RcsMatchConfigMapper rcsMatchConfigMapper;
//	@Test
//	public void test() {
////		List<RcsMatchConfigVo> rcsMatchConfigVos = rcsMatchConfigMapper.selectMatchByMatchId(9624);
////		List<RcsMatchConfigVo> rcsMatchConfigVos1 = rcsMatchConfigMapper.selectMatchPlayByMatchId(120652);
////		List<RcsMatchConfigVo> rcsMatchConfigVos2 = rcsMatchConfigMapper.selectPlayByMatchId(138899);
////
////		Map<String, Object> columnMap = new HashMap<>();
////		rcsMatchMarketConfigLogsMapper.selectByMap(columnMap);
////
////		RcsMatchPlayConfig rcsMatchPlayConfig = new RcsMatchPlayConfig();
////		rcsMatchPlayConfig.setMatchId(120652L);
////		rcsMatchPlayConfig.setPlayId(1);
////		rcsMatchPlayConfig.setStatus(MarketStatusEnum.SEAL.getState());
////		try {
////			marketViewServiceImpl.updateMatchPlayIdConfigByStatus(rcsMatchPlayConfig);
////		} catch (Exception e) {
////			log.error(e.getMessage(), e);
////		}
////	}
//
//}
