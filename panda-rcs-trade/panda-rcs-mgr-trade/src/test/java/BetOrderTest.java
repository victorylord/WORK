import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.panda.sport.data.rcs.api.RcsUserConfigApiService;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mapper.StandardSportTournamentMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsLabelSportVolumePercentage;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.StandardSportTournament;
import com.panda.sport.rcs.trade.Bootstrap;
import com.panda.sport.rcs.trade.controller.BetOrderController;
import com.panda.sport.rcs.trade.controller.MessageController;
import com.panda.sport.rcs.trade.mq.impl.MessageConsumerUtil;
import com.panda.sport.rcs.trade.wrapper.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;

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
public class BetOrderTest {
    @Autowired
    RcsUserConfigApiService rcsUserConfigService;
    @Autowired
    BetOrderController betOrderController;
    @Autowired
    private RcsTournamentOrderAcceptConfigService rcsTournamentOrderAcceptConfigService;
    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    private StandardSportTournamentMapper standardSportTournamentMapper;
    @Autowired
    private RcsMatchOrderAcceptConfigService rcsMatchOrderAcceptConfigService;
    @Autowired
    private RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private MessageController messageController;
    @Autowired
    private MessageConsumerUtil messageConsumerUtil;
    @Autowired
    RcsTradingAssignmentService rcsTradingAssignmentService;
    @Autowired
    protected ProducerSendMessageUtils sendMessage;
    @Test
    public void test() {
        sendMessage.sendMessage("USER_PROFILE_TAGS_TOPIC","{\"type\":2,\"entity\":{\"id\":136,\"tagColor\":\"2\",\"tagDetail\":\"90天内，盈利金额位于[-10000,0)区间n盈利金额=派彩金额-已结算注单的投注金额\",\"tagImgUrl\":\"1\",\"tagName\":\"VIP青铜\",\"tagType\":2}}");
//        RcsUserSpecialBetLimitConfigVo list = rcsUserConfigService.getList(139842617596850180L);
//        String s = JSONObject.toJSONString(list);
//        Integer integer = rcsTradeConfigService.selectDataSource("86063", "6", "1240167107829211137");
    }

    @Test
    public void setHalftimeTest(){


    }

    @Test
    public void xxx() {
        List<StandardMatchInfo> standardMatchInfos = standardMatchInfoMapper.selectByMap(new HashMap<>());
        for (StandardMatchInfo standardMatchInfo : standardMatchInfos) {
            rcsMatchOrderAcceptConfigService.init(standardMatchInfo.getId());
        }
        List<StandardSportTournament> standardSportTournaments = standardSportTournamentMapper.selectByMap(new HashMap<>());
        for (StandardSportTournament standardSportTournament : standardSportTournaments) {
            rcsTournamentOrderAcceptConfigService.init(standardSportTournament.getId());
        }
    }
    @Autowired
    IRcsLabelSportVolumePercentageService rcsLabelSportVolumePercentageService;
    @Autowired
    private RedisClient redisClient;
    @Test
    public void xxxx(){
        LambdaQueryWrapper<RcsLabelSportVolumePercentage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RcsLabelSportVolumePercentage::getTagId, 17);
        List<RcsLabelSportVolumePercentage> list = rcsLabelSportVolumePercentageService.list(wrapper);
        if (!CollectionUtils.isEmpty(list)) {
            String userTagLevelKeyOld = "risk:user:tag:level:" + 17;
            //删除缓存
            list.forEach(e -> {
                redisClient.delete(userTagLevelKeyOld + e.getSportId());
            });
        }
    }
}
