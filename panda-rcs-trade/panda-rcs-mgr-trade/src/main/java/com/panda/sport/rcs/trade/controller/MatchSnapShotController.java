package com.panda.sport.rcs.trade.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.panda.sport.rcs.trade.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.pojo.RcsCode;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.trade.utils.mongopage.StringUtils;
import com.panda.sport.rcs.trade.wrapper.MatchSnapShotService;
import com.panda.sport.rcs.trade.wrapper.RcsCodeService;
import com.panda.sport.rcs.trade.wrapper.RcsLanguageInternationService;
import com.panda.sport.rcs.trade.wrapper.StandardMatchInfoService;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.MarketSnapShotVo;
import com.panda.sport.rcs.vo.MatchSnapShotVo;
import com.panda.sport.rcs.vo.OddsSnapShotVo;
import com.panda.sport.rcs.vo.SnapShotEntity;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description @Param 赛前快照接口 @Author kimi @Date 2020/7/8
 * @return
 */
@RestController
@RequestMapping(value = "MatchSnapShotController")
@Slf4j
public class MatchSnapShotController {
    @Autowired
    private MatchSnapShotService matchSnapShotService;
    @Autowired
    private RcsCodeService rcsCodeService;
    @Autowired
    private StandardMatchInfoService standardMatchInfoService;
    
    @Autowired
    private RcsLanguageInternationService rcsLnguageInternationService;
    /**
     * 赔率倍数100000
     */
    private static final BigDecimal BASE = new BigDecimal(BaseConstants.MULTIPLE_VALUE);
    /**
     * 滚球标识
     */
    private static final String MATCH_TYPE = "2";
    /**
     * 玩法和相关顺序
     * 玩法id
     */
    private static HashMap<Long, SnapShotEntity> codeMap = new HashMap<>();

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse<T> @Description 返回赛前快照数据 @Param [matchId] @Author
     * kimi @Date 2020/7/8
     */
    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public HttpResponse<MatchSnapShotVo> getList(Long matchId) {
        MatchSnapShotVo matchSnapShotVo = new MatchSnapShotVo();
        //存放盘口数据
        StandardMatchInfo standardMatchInfo = standardMatchInfoService.selectById(matchId);
        Integer matchStatus = standardMatchInfo.getMatchStatus();
        HashMap<Long, MarketSnapShotVo> marketSnapShotVoHashMap = new HashMap<>();
        try {
            //1:先晒掉没用的数据
            List<Long> playIdNameCodes = new ArrayList<>();
            List<Long> oddsIdNameCodes = new ArrayList<>();
            List<OddsSnapShotVo> oddsSnapShotVos = matchSnapShotService.selectMatchSnapShot(matchId, matchStatus);
            if (CollectionUtils.isEmpty(oddsSnapShotVos)) {
                return HttpResponse.fail("数据为空");
            }
            for (OddsSnapShotVo oddsSnapShotVo : oddsSnapShotVos) {
                if (!MATCH_TYPE.equals(oddsSnapShotVo.getMatchType()) && oddsSnapShotVo.getOddsId() != null) {
                    MarketSnapShotVo marketSnapShotVo = marketSnapShotVoHashMap.get(oddsSnapShotVo.getMarketId());
                    if (marketSnapShotVo == null) {
                        marketSnapShotVo = new MarketSnapShotVo();
                        marketSnapShotVoHashMap.put(oddsSnapShotVo.getMarketId(), marketSnapShotVo);
                        marketSnapShotVo.setMarketId(oddsSnapShotVo.getMarketId());
                        marketSnapShotVo.setMarketValue(oddsSnapShotVo.getMarketValue());
                        marketSnapShotVo.setPlayId(oddsSnapShotVo.getPlayId());
                        marketSnapShotVo.setPlayNameCode(oddsSnapShotVo.getPlayNameCode());
                        playIdNameCodes.add(oddsSnapShotVo.getPlayNameCode());
                    }
                    oddsIdNameCodes.add(oddsSnapShotVo.getOddsNameCode());
                    if (oddsSnapShotVo.getOddsValueMax() == null) {
                        if (oddsSnapShotVo.getOddsValue() != null) {
                            oddsSnapShotVo.setOddsValueMax(new BigDecimal(oddsSnapShotVo.getOddsValue()).divide(BASE, 2, BigDecimal.ROUND_HALF_UP));
                        }
                    }
                    marketSnapShotVo.getOddsSnapShotVoList().add(oddsSnapShotVo);
                }
            }
            //2：查询玩法编码多语言
            Map<Long, Map<String, String>> longI18nBeanMap = rcsLnguageInternationService.getCachedNamesMapByCodes(playIdNameCodes);
            Map<Long, Map<String, String>> cachedNamesMapByCodes = rcsLnguageInternationService.getCachedNamesMapByCodes(oddsIdNameCodes);
            //中文名字适配
            nameAdaptation(longI18nBeanMap);
            nameAdaptation(cachedNamesMapByCodes);
            //3:取排列规则
            if (CollectionUtils.isEmpty(codeMap)) {
                codeMapInit();
            }
            //4：在进行分组  先把有序列的玩法放进去
            List<MarketSnapShotVo> marketSnapShotVoList1 = matchSnapShotVo.getMarketSnapShotVoList1();
            List<MarketSnapShotVo> marketSnapShotVoList2 = matchSnapShotVo.getMarketSnapShotVoList2();
            List<MarketSnapShotVo> marketSnapShotVoList3 = matchSnapShotVo.getMarketSnapShotVoList3();
            List<MarketSnapShotVo> marketSnapShotVoList4 = matchSnapShotVo.getMarketSnapShotVoList4();
            List<MarketSnapShotVo> marketSnapShotVoList5 = matchSnapShotVo.getMarketSnapShotVoList5();
            Iterator<Map.Entry<Long, MarketSnapShotVo>> iterator = marketSnapShotVoHashMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Long, MarketSnapShotVo> next = iterator.next();
                MarketSnapShotVo value = next.getValue();
                SnapShotEntity snapShotEntity = codeMap.get(value.getPlayId());
                if (snapShotEntity != null) {
                    int group = snapShotEntity.getGroup();
                    switch (group) {
                        case 1:
                            marketSnapShotVoList1.add(value);
                            break;
                        case 2:
                            marketSnapShotVoList2.add(value);
                            break;
                        case 3:
                            marketSnapShotVoList3.add(value);
                            break;
                        case 4:
                            marketSnapShotVoList4.add(value);
                            break;
                        case 5:
                            marketSnapShotVoList5.add(value);
                            break;
                        default:
                            break;
                    }
                    iterator.remove();
                }
            }
            //5:对放进分组的玩法进行排序
            sort(marketSnapShotVoList1);
            sort(marketSnapShotVoList2);
            sort(marketSnapShotVoList3);
            sort(marketSnapShotVoList4);
            sort(marketSnapShotVoList5);
            //6:剩余的盘口 先排序然后   放到数量最少的里面
            Collection<MarketSnapShotVo> values = marketSnapShotVoHashMap.values();
            ArrayList<MarketSnapShotVo> arrayList = new ArrayList(values);
            sortByPlayId(arrayList);
            //数量最少的那一个
            for (MarketSnapShotVo marketSnapShotVo : arrayList) {
                List<MarketSnapShotVo> marketSnapShotVoList = getMinList(matchSnapShotVo);
                marketSnapShotVoList.add(marketSnapShotVo);
            }
            //计算出涨额
            calculationAmountIncrease(matchSnapShotVo.getMarketSnapShotVoList1(), longI18nBeanMap, cachedNamesMapByCodes);
            calculationAmountIncrease(matchSnapShotVo.getMarketSnapShotVoList2(), longI18nBeanMap, cachedNamesMapByCodes);
            calculationAmountIncrease(matchSnapShotVo.getMarketSnapShotVoList3(), longI18nBeanMap, cachedNamesMapByCodes);
            calculationAmountIncrease(matchSnapShotVo.getMarketSnapShotVoList4(), longI18nBeanMap, cachedNamesMapByCodes);
            calculationAmountIncrease(matchSnapShotVo.getMarketSnapShotVoList5(), longI18nBeanMap, cachedNamesMapByCodes);
            //返回数据
            return HttpResponse.success(matchSnapShotVo);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return HttpResponse.fail("服务器出错");
    }

    List<MarketSnapShotVo> getMinList(MatchSnapShotVo matchSnapShotVo) {
        List<MarketSnapShotVo> list = matchSnapShotVo.getMarketSnapShotVoList1();
        if (matchSnapShotVo.getMarketSnapShotVoList2().size() < list.size()) {
            list = matchSnapShotVo.getMarketSnapShotVoList2();
        }
        if (matchSnapShotVo.getMarketSnapShotVoList3().size() < list.size()) {
            list = matchSnapShotVo.getMarketSnapShotVoList3();
        }
        if (matchSnapShotVo.getMarketSnapShotVoList4().size() < list.size()) {
            list = matchSnapShotVo.getMarketSnapShotVoList4();
        }
        if (matchSnapShotVo.getMarketSnapShotVoList5().size() < list.size()) {
            list = matchSnapShotVo.getMarketSnapShotVoList5();
        }
        return list;
    }

    /**
     * @return void
     * @Description 计算出涨额
     * @Param [marketSnapShotVoList]
     * @Author kimi
     * @Date 2020/7/10
     **/
    private void calculationAmountIncrease(List<MarketSnapShotVo> marketSnapShotVoList, Map<Long, Map<String, String>> longI18nBeanMap, Map<Long, Map<String, String>> cachedNamesMapByCodes) {
        if (!CollectionUtils.isEmpty(marketSnapShotVoList)) {
            for (MarketSnapShotVo marketSnapShotVo : marketSnapShotVoList) {
                marketSnapShotVo.setPlayNameCodeList(longI18nBeanMap.get(marketSnapShotVo.getPlayNameCode()));
                List<OddsSnapShotVo> oddsSnapShotVoList = marketSnapShotVo.getOddsSnapShotVoList();
                int size = oddsSnapShotVoList.size();
                //最大投注项的id
                Long oddsIdMax = 0L;
                //最大投注项的金额
                Long oddsVlaueMax = 0L;
                //该盘口下注总金额
                Long bet = 0L;
                for (OddsSnapShotVo oddsSnapShotVo : oddsSnapShotVoList) {
                    //投注项名字适配
                    String marketValue = oddsSnapShotVo.getMarketValue();
                    if (marketValue == null || marketValue.length() == 0) {
                        oddsSnapShotVo.setOddsNameCodeList(cachedNamesMapByCodes.get(oddsSnapShotVo.getOddsNameCode()));
                    } else {
                        oddsSnapShotVo.setOddsNameCodeList(StringUtils.parseName(cachedNamesMapByCodes.get(oddsSnapShotVo.getOddsNameCode()), marketValue));
                    }
                    long l = oddsSnapShotVo.getBetAmount().longValue();
                    if (l > oddsVlaueMax) {
                        oddsVlaueMax = l;
                        oddsIdMax = oddsSnapShotVo.getOddsId();
                    }
                    bet += l;
                }
                //只计算两项盘和三项盘
                if (size == 2 || size == 3) {
                    //设置值
                    for (OddsSnapShotVo oddsSnapShotVo : oddsSnapShotVoList) {
                        if (oddsSnapShotVo.getOddsId().equals(oddsIdMax)) {
                            long l = oddsVlaueMax * 2 - bet;
                            oddsSnapShotVo.setAmountIncrease(new BigDecimal(l));
                        }
                    }
                }
            }
        }
    }


    private void codeMapInit() {
        List<RcsCode> rcsCodeList = rcsCodeService.selectRcsCods("snapshot");
        for (RcsCode rcsCode : rcsCodeList) {
            String childKey = rcsCode.getChildKey();
            String[] split = rcsCode.getValue().split("、");
            for (int x = 0; x < split.length; x++) {
                SnapShotEntity snapShotEntity = new SnapShotEntity();
                snapShotEntity.setPlayId(Long.parseLong(split[x]));
                snapShotEntity.setOrder((long) x);
                snapShotEntity.setGroup(Integer.parseInt(childKey));
                codeMap.put(snapShotEntity.getPlayId(), snapShotEntity);
            }
        }
    }

    /**
     * @return void
     * @Description 排序
     * @Param [values]
     * @Author kimi
     * @Date 2020/7/14
     **/
    void sortByPlayId(ArrayList<MarketSnapShotVo> values) {
        if (!CollectionUtils.isEmpty(values)) {
            Collections.sort(values, new Comparator<MarketSnapShotVo>() {

                @Override
                public int compare(MarketSnapShotVo o1, MarketSnapShotVo o2) {
                    if (o1.getPlayId() > o2.getPlayId()) {
                        return 1;
                    } else if (o1.getPlayId() < o2.getPlayId()) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
        }
    }

    /**
     * @return void
     * @Description 排序
     * @Param [marketSnapShotVoList]
     * @Author kimi
     * @Date 2020/7/9
     **/
    void sort(List<MarketSnapShotVo> marketSnapShotVoList) {
        if (!CollectionUtils.isEmpty(marketSnapShotVoList)) {
            Collections.sort(marketSnapShotVoList, new Comparator<MarketSnapShotVo>() {
                @Override
                public int compare(MarketSnapShotVo o1, MarketSnapShotVo o2) {
                    //先根据玩法排序
                    if (o1.getPlayId().equals(o2.getPlayId())) {
                        //根据盘口排序
                        if (o1.getMarketValue() == null && o2.getMarketValue() == null) {
                            return 0;
                        } else {
                            double v1 = 0;
                            if (o1.getMarketValue() != null &&!o1.getMarketValue().equals("")) {
                                if (org.apache.commons.lang3.math.NumberUtils.isNumber(o1.getMarketValue())){
                                    v1 = Double.parseDouble(o1.getMarketValue());
                                }
                            }
                            double v2 = 0;
                            if (o2.getMarketValue() != null&&!o2.getMarketValue().equals("")) {
                                if (org.apache.commons.lang3.math.NumberUtils.isNumber(o2.getMarketValue())){
                                    v2 = Double.parseDouble(o2.getMarketValue());
                                }
                            }
                            if (v1 < v2) {
                                return -1;
                            } else if (v1 > v2) {
                                return 1;
                            }
                            return 0;
                        }
                    } else {
                        Long order1 = codeMap.get(o1.getPlayId()).getOrder();
                        Long order2 = codeMap.get(o2.getPlayId()).getOrder();
                        if (order1 < order2) {
                            return -1;
                        } else if (order1 > order2) {
                            return 1;
                        }
                        return 0;
                    }
                }
            });
        }
    }
    
    public static void main(String[] args) {
		System.out.println(org.apache.commons.lang3.math.NumberUtils.isNumber("aaa"));
	}

    void nameAdaptation(Map<Long, Map<String, String>> cachedNamesMapByCodes) {
        if (!CollectionUtils.isEmpty(cachedNamesMapByCodes)) {
            for (Map<String, String> map : cachedNamesMapByCodes.values()) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    entry.setValue(StringUtils.parseName(entry.getValue()));
                }
            }
        }
    }

}
