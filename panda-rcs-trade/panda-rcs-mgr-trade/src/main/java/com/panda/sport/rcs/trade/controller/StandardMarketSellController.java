package com.panda.sport.rcs.trade.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.merge.api.ITradeMarketConfigApi;
import com.panda.merge.dto.*;
import com.panda.sport.data.rcs.dto.tournament.StandardMarketSellQueryDto;
import com.panda.sport.manager.api.IMarketCategorySellApi;
import com.panda.sport.manager.api.bo.*;
import com.panda.sport.manager.api.dto.*;
import com.panda.sport.manager.api.util.PandaPage;
import com.panda.sport.rcs.bean.RcsMarketSellPersonGroup;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.enums.TradeLevelEnum;
import com.panda.sport.rcs.enums.TradeStatusEnum;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.log.annotion.LogAnnotion;
import com.panda.sport.rcs.log.annotion.OperateLog;
import com.panda.sport.rcs.mapper.MarketCategorySetMapper;
import com.panda.sport.rcs.mapper.RcsLanguageInternationMapper;
import com.panda.sport.rcs.mapper.RcsMarketSellPersonGroupMapper;
import com.panda.sport.rcs.mapper.RcsTradingAssignmentMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplateMapper;
import com.panda.sport.rcs.mongo.I18nBean;
import com.panda.sport.rcs.pojo.RcsMatchCollection;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.dto.QueryPreLiveMatchDto;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import com.panda.sport.rcs.trade.enums.LinkedTypeEnum;
import com.panda.sport.rcs.trade.enums.MatchTypeEnum;
import com.panda.sport.rcs.trade.enums.TempTypeEnum;
import com.panda.sport.rcs.trade.service.DistanceSwitchServer;
import com.panda.sport.rcs.trade.service.TradeModeService;
import com.panda.sport.rcs.trade.service.TradeStatusService;
import com.panda.sport.rcs.trade.service.impl.OnSaleCommonServer;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.sell.SellSpecialGroupPersonVo;
import com.panda.sport.rcs.trade.wrapper.RcsMarketSellPersonGroupService;
import com.panda.sport.rcs.trade.wrapper.RcsMatchCollectionService;
import com.panda.sport.rcs.trade.wrapper.RcsStandardSportMarketSellService;
import com.panda.sport.rcs.trade.wrapper.StandardMatchInfoService;
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.utils.DataRealtimeApiUtils;
import com.panda.sport.rcs.utils.DataRealtimeApiUtils.ApiCall;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.*;
import com.panda.sport.rcs.vo.categoryset.CategorySetVo;
import com.panda.sports.api.ISystemUserOrgAuthApi;
import com.panda.sports.api.vo.SysOrgAuthVO;
import com.panda.sports.auth.rpc.IAuthRequiredPermission;
import com.panda.sports.auth.rpc.ISysUserService;
import com.panda.sports.auth.rpc.po.SysUserVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.Assert;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.poi.util.StringUtil;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

/**
 * 操盘手确认开售页面
 *
 * @author :  carver
 */
@RestController
@Slf4j
@RequestMapping("/standardMarketSell")
public class StandardMarketSellController {
    @Autowired
    private RcsTournamentTemplateMapper templateMapper;
    @Reference(check = false, lazy = true, retries = 3, timeout = 5000)
    private ITradeMarketConfigApi tradeMarketConfigApi;
    @Autowired
    private RcsStandardSportMarketSellService rcsStandardSportMarketSellService;
    @Autowired
    private MarketCategorySetMapper marketCategorySetMapper;
    @Autowired
    private RcsMatchCollectionService rcsMatchCollectionService;
    @Autowired
    private RcsMarketSellPersonGroupService rcsMarketSellPersonGroupService;
    @Autowired
    private RcsMarketSellPersonGroupMapper rcsMarketSellPersonGroupMapper;
    @Autowired
    private RcsTournamentTemplateMapper rcsTournamentTemplateMapper;
    @Autowired
    private RcsTradingAssignmentMapper tradingAssignmentMapper;

    @Autowired
    private TradeModeService tradeModeService;
    @Autowired
    private TradeStatusService tradeStatusService;
    @Autowired
    private StandardMatchInfoService standardMatchInfoService;
    @Autowired
    private DistanceSwitchServer distanceSwitchServerImpl;
    @Autowired
    private OnSaleCommonServer onSaleCommonServer;

    @Autowired
    private com.panda.sport.rcs.mapper.RcsLanguageInternationMapper rcsLanguageInternationMapper;

    @Reference(check = false, lazy = true, retries = 1, timeout = 100000)
    IMarketCategorySellApi marketCategorySellApi;
    @Reference(check = false, lazy = true, retries = 1, timeout = 5000)
    IAuthRequiredPermission authRequiredPermission;
    @Reference(check = false, lazy = true, retries = 1, timeout = 5000)
    ISystemUserOrgAuthApi systemUserOrgAuthApi;
    @Reference(check = false, lazy = true, retries = 1, timeout = 5000)
    ISysUserService iSysUserService;

    /**
     * 操盘手确认开售页面，获取预开售的赛事
     *
     * @return
     */
    @PostMapping("/list")
    public HttpResponse<IPage<StandardMarketSellVo>> listStandardMarketSellVo(@RequestBody StandardMarketSellQueryVo standardMarketSellQueryVo) {
        try {
            //验证前端必填参数
            Assert.notNull(standardMarketSellQueryVo.getSportId(), "体育类型ID不能为空！");
            Assert.notNull(standardMarketSellQueryVo.getSortType(), "排序类型不能为空！");
            if (standardMarketSellQueryVo.getHistoryFlag() == null) {
                standardMarketSellQueryVo.setHistoryFlag(0);
            }
            if (standardMarketSellQueryVo.getIsEarlyTrading() == null) {
                standardMarketSellQueryVo.setIsEarlyTrading(0);
            }
            IPage<StandardMarketSellVo> rtnList = rcsStandardSportMarketSellService.listStandardMarketSellVo(standardMarketSellQueryVo);
            return HttpResponse.success(rtnList);
        } catch (IllegalArgumentException ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(500, "获取确认开售的赛事，查询异常：" + ex.getMessage());
        }
    }

    /**
     * 确认开售
     * 1.调用融合API
     * 2.更新风控数据库，预开售赛事时间和状态
     *
     * @return
     */
    @PostMapping("/confirmSell")
    @LogAnnotion(name = "确认开售", keys = {"sportId", "matchInfoId", "matchStatus", "beginTime", "sellDateType", "sellMatchDate", "sortType", "filter", "operatorInfo", "beginTimeMillis",
            "endTimeMillis", "historyFlag", "isEarlyTrading", "marketCount", "cornerShow", "cardShow", "isDelete"},
            title = {"体育种类ID", "标准赛事ID", "赛事状态", "开赛时间", "开售时间类型", "开售时间", "排序类型", "筛选条件", "操作者信息", "比赛开始时间utc", "比赛结束时间utc", "" +
                    "历史赛程", "其他早盘", "盘口数", "角球是否展示", "罚牌是否展示", "删除 0:否 1:是"})
    public HttpResponse confirmStandardMarketSell(@RequestBody StandardMarketSellQueryVo standardMarketSellQueryVo) {
        try {
            //验证前端必填参数
            Assert.notNull(standardMarketSellQueryVo.getId(), "ID不能为空！");
            Assert.notNull(standardMarketSellQueryVo.getMatchInfoId(), "标准赛事ID不能为空！");
            Assert.notNull(standardMarketSellQueryVo.getSellDateType(), "开售时间类型不能为空！");
            Assert.notNull(standardMarketSellQueryVo.getSellMatchDate(), "开售时间不能为空！");
            return rcsStandardSportMarketSellService.confirmStandardMarketSell(standardMarketSellQueryVo);
        } catch (IllegalArgumentException ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(500, "执行融合确认开售时间API，调用异常：" + ex.getMessage());
        }
    }

    /**
     * 设置盘口数/角球/罚牌
     *
     * @return
     */
    @PostMapping("/configPlayShow")
    @LogAnnotion(name = "设置盘口数/角球/罚牌", keys = {"sportId", "matchInfoId", "matchStatus", "beginTime", "sellDateType", "sellMatchDate", "sortType", "filter", "operatorInfo", "beginTimeMillis",
            "endTimeMillis", "historyFlag", "isEarlyTrading", "marketCount", "cornerShow", "cardShow", "isDelete"},
            title = {"体育种类ID", "标准赛事ID", "赛事状态", "开赛时间", "开售时间类型", "开售时间", "排序类型", "筛选条件", "操作者信息", "比赛开始时间utc", "比赛结束时间utc", "" +
                    "历史赛程", "其他早盘", "盘口数", "角球是否展示", "罚牌是否展示", "删除 0:否 1:是"})
    public HttpResponse configPlayShow(@RequestBody StandardMarketSellQueryVo standardMarketSellQueryVo) {
        try {
            //验证前端必填参数
            Assert.notNull(standardMarketSellQueryVo.getId(), "ID不能为空！");
            Assert.notNull(standardMarketSellQueryVo.getMatchInfoId(), "标准赛事ID不能为空！");
//            Assert.notNull(standardMarketSellQueryVo.getMarketCount(), "盘口数不能为空！");
            Assert.notNull(standardMarketSellQueryVo.getCornerShow(), "角球设置不能为空！");
            Assert.notNull(standardMarketSellQueryVo.getCardShow(), "罚牌设置不能为空！");
            return rcsStandardSportMarketSellService.configPlayShow(standardMarketSellQueryVo);
        } catch (IllegalArgumentException ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(500, "风控设置操盘确认开售盘口数/角球/罚牌，执行异常：" + e.getMessage());
        }
    }


    /**
     * 根据赛种获取开售赛事数量统计
     *
     * @return
     */
    @PostMapping("/getMatchNumberByType")
    public HttpResponse getMatchNumberByType() {
        try {
            log.info("::{}::统计赛种开售赛事数量!!!!",CommonUtil.getRequestId());
            return HttpResponse.success(rcsStandardSportMarketSellService.getMatchNumberByType());
        } catch (IllegalArgumentException ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(500, "根据赛种获取开售赛事数量统计，执行异常：" + ex.getMessage());
        }
    }


    /**
     * 逻辑删除开售数据
     *
     * @return
     */
    @PostMapping("/delete")
    @LogAnnotion(name = "逻辑删除开售数据", keys = {"sportId", "matchInfoId", "matchStatus", "beginTime", "sellDateType", "sellMatchDate", "sortType", "filter", "operatorInfo", "beginTimeMillis",
            "endTimeMillis", "historyFlag", "isEarlyTrading", "marketCount", "cornerShow", "cardShow", "isDelete"},
            title = {"体育种类ID", "标准赛事ID", "赛事状态", "开赛时间", "开售时间类型", "开售时间", "排序类型", "筛选条件", "操作者信息", "比赛开始时间utc", "比赛结束时间utc", "" +
                    "历史赛程", "其他早盘", "盘口数", "角球是否展示", "罚牌是否展示", "删除 0:否 1:是"})
    public HttpResponse delete(@RequestBody StandardMarketSellQueryVo standardMarketSellQueryVo) {
        try {
            if (standardMarketSellQueryVo.getId() == null || standardMarketSellQueryVo.getIsDelete() == null || !(Arrays.asList(new Integer[]{0, 1}).contains(standardMarketSellQueryVo.getIsDelete()))) {
                throw new RcsServiceException("您给的数据不完整");
            }
            int delete = rcsStandardSportMarketSellService.delete(standardMarketSellQueryVo);
            if (delete > 0) {
                return HttpResponse.success();
            } else {
                return HttpResponse.fail("删除失败");
            }
        } catch (Exception ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(500, "删除错误" + ex.getMessage());
        }
    }

    @PostMapping("/match/info")
    public HttpResponse matchInfo(@RequestBody StandardMarketSellQueryV2Vo standardMarketSellQueryVo) {
        try {
            if (standardMarketSellQueryVo.getMatchId() == null) {
                return HttpResponse.fail("赛事id不能为null");
            }

            QueryMarketCategorySellDTO queryBean = new QueryMarketCategorySellDTO();
            queryBean.setMatchId(standardMarketSellQueryVo.getMatchId());
            Response<List<QueryMarketCategorySellBO>> response = DataRealtimeApiUtils.handleApi(queryBean, new ApiCall() {
                @Override
                @Trace
                public <R> Response<R> callApi(Request request) {
                    return marketCategorySellApi.queryMarketCategorySell(request);
                }
            });
            return HttpResponse.success();
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(500, e.getMessage());
        }
    }

    @PostMapping("/match/play")
    public HttpResponse matchPlay(@RequestBody StandardMarketSellQueryV2Vo standardMarketSellQueryVo,@RequestHeader(value = "lang",required = false)String lang) {
        try {
            if (standardMarketSellQueryVo.getMatchId() == null) {
                return HttpResponse.fail("赛事id不能为null");
            }
            if (standardMarketSellQueryVo.getSportId() == null) {
                return HttpResponse.fail("运动id不能为null");
            }
            Map<String, Object> result = Maps.newHashMap();

            QueryMarketCategorySellDTO queryBean = new QueryMarketCategorySellDTO();
            queryBean.setMatchId(standardMarketSellQueryVo.getMatchId());
            queryBean.setMarketTypes(Arrays.asList(standardMarketSellQueryVo.getMarketType()));
            queryBean.setSellStatus(standardMarketSellQueryVo.getMatchSellStatus());
            queryBean.setMatchStatus(standardMarketSellQueryVo.getMatchStatus());
            queryBean.setSportId(standardMarketSellQueryVo.getSportId());

            Request<QueryMarketCategorySellDTO> request = new Request();
            request.setData(queryBean);
            request.setDataSourceTime(System.currentTimeMillis());
            request.setLinkId(UUID.randomUUID().toString().replace("-", ""));
            request.setOperaterId(TradeUserUtils.getUserIdNoException().longValue());
            Response<List<QueryMarketCategorySellBO>> response = marketCategorySellApi.queryMarketCategorySell(request);
            if (response.getData() != null && response.getData().size() > 0) {
                List<QueryMarketCategorySellBO> rtnList = response.getData();
                List<Long> playIdList = rtnList.stream().map(map -> Long.parseLong(String.valueOf(map.getMarketCategoryId()))).collect(Collectors.toList());
                Map<String, Object> params = Maps.newHashMap();
                params.put("sportId", standardMarketSellQueryVo.getSportId());
                params.put("plays", StringUtils.join(playIdList, ","));
                params.put("lang",lang);
                List<CategorySetVo> list = marketCategorySetMapper.queryAllCategorySetList(params);
                /**
                 * 根据玩法集名称，针对玩法进行分组
                 */
                list.stream().forEach(item->{
                    if(item.getNameCode()!=null){
                        String namejson =item.getNameCode();
                        Map jsonMap=JSONObject.parseObject(namejson);
                        if (jsonMap!=null) {
                            if(jsonMap.get(lang)!=null && !jsonMap.get(lang).equals("")){
                                String name = jsonMap.get(lang).toString();
                                if(!name.equals("")){
                                    item.setName(name);
                                }
                            }
                        }
                    }
                });

                Map<String, List<CategorySetVo>> setMap = list.stream().collect(Collectors.groupingBy(CategorySetVo::getName, LinkedHashMap::new, Collectors.toList()));
                if (setMap.size() > 0) {
                    List<Map<String, Object>> categorySet = Lists.newArrayList();
                    result.put("category_set_list", categorySet);
                    int allCategoryCount = 0;
                    int sellCategoryCount = 0;

                    /**
                     * 处理归类到玩法集中的玩法
                     */
                    for (Map.Entry<String, List<CategorySetVo>> entry : setMap.entrySet()) {
                        Map<Long, String> category = entry.getValue().stream().collect(Collectors.toMap(CategorySetVo::getCategoryId, CategorySetVo::getText));
                        List<Map<String, Object>> playList = Lists.newArrayList();
                        //构建玩法集集合
                        Map<String, Object> categorySetMap = Maps.newHashMap();
                        categorySetMap.put("sell_count", 0);
                        categorySetMap.put("name", entry.getKey());
                        for (QueryMarketCategorySellBO playMap : rtnList) {
                            //玩法集中包含此玩法
                            if (category.containsKey(playMap.getMarketCategoryId())) {
                                Map<String, Object> play = Maps.newHashMap();
                                play.put("category_id", playMap.getMarketCategoryId());

                                JSONObject JSON=JSONObject.parseObject(category.get(playMap.getMarketCategoryId()));
                                if(JSON.get(lang)!=null){
                                    play.put("text", JSON.get(lang).toString());
                                }
                                play.put("isOddsValue", playMap.getIsOddsValue());
                                play.put("sell_status", playMap.getSellStatus());
                                play.put("isSell", playMap.getIsSell());
                                playList.add(play);

                                if ("Sold".equals(String.valueOf(play.get("sell_status")))) {
                                    Integer sellCount = Integer.parseInt(String.valueOf(categorySetMap.get("sell_count")));
                                    categorySetMap.put("sell_count", sellCount + 1);
                                }
                            }
                        }
                        categorySetMap.put("all_count", playList.size());
                        categorySetMap.put("playList", playList);
                        categorySet.add(categorySetMap);

                        //统计已开售玩法数量
                        sellCategoryCount = sellCategoryCount + Integer.parseInt(String.valueOf(categorySetMap.get("sell_count")));
                        //统计总玩法数量
                        allCategoryCount = allCategoryCount + category.size();
                    }


                    /**
                     * 玩法没有归类到玩法集中，单独处理玩法
                     */
                    //构建玩法集集合
                    Map<String, Object> categorySetMap = Maps.newHashMap();
                    categorySetMap.put("sell_count", 0);
                    if(lang.equals("en")){
                        categorySetMap.put("name", "other");
                    }else{
                        categorySetMap.put("name", "其他玩法");
                    }

                    List<Map<String, Object>> playList = Lists.newArrayList();
                    Map<Long, List<CategorySetVo>> otherSetMap = list.stream().collect(Collectors.groupingBy(CategorySetVo::getCategoryId, LinkedHashMap::new, Collectors.toList()));
                    for (QueryMarketCategorySellBO playMap : rtnList) {
                        if (!otherSetMap.containsKey(playMap.getMarketCategoryId())) {
                            Map<String, Object> play = Maps.newHashMap();
                            play.put("category_id", playMap.getMarketCategoryId());
                            Map<String, String> map = playMap.getNames();
                            if (map != null) {
                                play.put("text", playMap.getNames().get(lang));
                            }
                            play.put("isOddsValue", playMap.getIsOddsValue());
                            play.put("sell_status", playMap.getSellStatus());
                            play.put("isSell", playMap.getIsSell());
                            playList.add(play);

                            if ("Sold".equals(String.valueOf(play.get("sell_status")))) {
                                Integer sellCount = Integer.parseInt(String.valueOf(categorySetMap.get("sell_count")));
                                categorySetMap.put("sell_count", sellCount + 1);
                            }
                            allCategoryCount++;
                        }
                    }
                    categorySetMap.put("all_count", playList.size());
                    categorySetMap.put("playList", playList);

                    if (playList.size() > 0) {
                        categorySet.add(categorySetMap);
                        //统计总玩法和已开售玩法数量
                        sellCategoryCount = sellCategoryCount + Integer.parseInt(String.valueOf(categorySetMap.get("sell_count")));
                        result.put("sell_category_count", sellCategoryCount);
                        result.put("all_category_count", allCategoryCount);
                    }
                }
            }
            return HttpResponse.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(500, e.getMessage());
        }
    }

    @PostMapping("/match/play/marketInfo")
    public HttpResponse matchPlayMarketInfo(@RequestBody StandardMarketSellQueryV2Vo standardMarketSellQueryVo, HttpServletRequest request) {
        try {
            if (standardMarketSellQueryVo.getMatchId() == null) {
                return HttpResponse.fail("赛事id不能为null");
            }
            if (standardMarketSellQueryVo.getPlayId() == null) {
                return HttpResponse.fail("玩法id不能为null");
            }
            
            String lang = request.getHeader("lang");

            QueryMarketsDTO marketBean = new QueryMarketsDTO();
            marketBean.setMatchId(standardMarketSellQueryVo.getMatchId());
            marketBean.setMarketCategoryId(standardMarketSellQueryVo.getPlayId());
            marketBean.setHomeTeamNames(standardMarketSellQueryVo.getHomeTeamNames());
            marketBean.setAwayTeamNames(standardMarketSellQueryVo.getAwayTeamNames());
            Response<QueryMarketsBO> response = DataRealtimeApiUtils.handleApi(marketBean, new ApiCall() {
                @Override
                @Trace
                public <R> Response<R> callApi(Request request) {
                    Response<QueryMarketsBO> rs = marketCategorySellApi.queryMarkets(request);
                    if(Objects.nonNull(rs) && Objects.nonNull(rs.getData()) && Objects.nonNull(rs.getData().getQueryMarketsMarketBOList())){
                        List<QueryMarketsMarketBO> queryMarketsMarketBOList = rs.getData().getQueryMarketsMarketBOList();
                        for (QueryMarketsMarketBO queryMarketsMarketBO : queryMarketsMarketBOList) {
                            List<QueryMarketOddsBO> queryMarketOddsBOList = queryMarketsMarketBO.getQueryMarketOddsBOList();
                            for (QueryMarketOddsBO queryMarketOddsBO : queryMarketOddsBOList) {
                                String oddsType = queryMarketOddsBO.getOddsType();
                                if (StringUtils.isNotEmpty(oddsType)) {
                                    queryMarketOddsBO.setOddsType(String.valueOf(JSON.parseObject(oddsType, HashMap.class).get(lang)));
                                }
                            }
                        }
                    }else{
                        log.error("::{}::RPC接口返回无数据/match/play/marketInfo，",CommonUtil.getRequestId(standardMarketSellQueryVo.getMatchId(),standardMarketSellQueryVo.getPlayId()));
                    }
                    return (Response<R>) rs;
                }
            });
            return HttpResponse.success(response);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(500, e.getMessage());
        }
    }

    @PostMapping("/match/configChangeWeight")
    public HttpResponse configChangeWeight(@RequestBody StandardMarketSellQueryDto standardMarketSellQueryVo) {
        try {
            Assert.notNull(standardMarketSellQueryVo.getMatchId(), "赛事id不能为null");
            Assert.notNull(standardMarketSellQueryVo.getSportId(), "赛种不能为null");
            Assert.notNull(standardMarketSellQueryVo.getMarketType(), "盘口类型不能为null");
            Assert.notNull(standardMarketSellQueryVo.getMatchSellStatus(), "开售状态不能为null");
            Assert.notNull(standardMarketSellQueryVo.getSrWeight(), "sr权重不能为null");
            Assert.notNull(standardMarketSellQueryVo.getBcWeight(), "bc权重不能为null");
            Assert.notNull(standardMarketSellQueryVo.getBgWeight(), "bg权重不能为null");
            Assert.notNull(standardMarketSellQueryVo.getTxWeight(), "tx权重不能为null");
            Assert.notNull(standardMarketSellQueryVo.getRbWeight(), "rb权重不能为null");
            Assert.notNull(standardMarketSellQueryVo.getAoWeight(), "ao权重不能为null");
            Assert.notNull(standardMarketSellQueryVo.getPiWeight(), "pi权重不能为null");
            Assert.notNull(standardMarketSellQueryVo.getLsWeight(), "ls权重不能为null");
            log.info("::{}::::修改权重前端请求参数:{}",CommonUtil.getRequestId(), JSON.toJSONString(standardMarketSellQueryVo));

            //开售的赛事，赔率源权重切换
            HttpResponse response = rcsStandardSportMarketSellService.configChangeWeight(standardMarketSellQueryVo);
            ChangeLiveWeightDTO weightBean = JSON.parseObject(JSON.toJSONString(standardMarketSellQueryVo), ChangeLiveWeightDTO.class);
            log.info("::{}::::修改权重请求融合数据:{},",CommonUtil.getRequestId(), JSON.toJSONString(weightBean));
            Integer userId = TradeUserUtils.getUserId();
            weightBean.setUserId(userId.toString());
            Response<Object> objectResponse = DataRealtimeApiUtils.handleApi(weightBean, new ApiCall() {
                @Override
                @Trace
                public <R> Response<R> callApi(Request request) {
                    Response rs = marketCategorySellApi.changeWeight(request);
                    return rs;
                }
            });

            //kir-修改23427 和 23428 bug
            if (objectResponse.isSuccess()) {
                if (SportIdEnum.isFootball(standardMarketSellQueryVo.getSportId())) {
                    QueryWrapper<RcsTournamentTemplate> templateQueryWrapper = new QueryWrapper();
                    templateQueryWrapper.lambda().eq(RcsTournamentTemplate::getType, TempTypeEnum.MATCH.getId())
                            .eq(RcsTournamentTemplate::getTypeVal, standardMarketSellQueryVo.getMatchId())
                            .eq(RcsTournamentTemplate::getMatchType, MatchTypeEnum.LIVE.getId())
                            .eq(RcsTournamentTemplate::getSportId, standardMarketSellQueryVo.getSportId());
                    RcsTournamentTemplate template = rcsTournamentTemplateMapper.selectOne(templateQueryWrapper);
                    log.info("::{}::接拒单数据源切换-滚球赛事模板:{}",CommonUtil.getRequestId(), JsonFormatUtils.toJson(template));
                    if (!ObjectUtils.isEmpty(template) && org.apache.dubbo.common.utils.StringUtils.isNotEmpty(standardMarketSellQueryVo.getDataSouceCode())) {
                        //更新赛事模板接拒单事件源切换
                        rcsStandardSportMarketSellService.tournamentTemplateSettle(template);
                    }
                }
            }
            return response;
        } catch (IllegalArgumentException ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(500, e.getMessage());
        }
    }

    /**
     * 赛事开售处理方法
     *
     * @param standardMarketSellQueryVo 请求参数
     * @return 成功或者失败
     */
    @PostMapping("/match/confirmMarketCategorySell")
    public HttpResponse matchConfirmMarketCategorySell(@RequestBody StandardMarketSellQueryDto standardMarketSellQueryVo) {
        try {
            Assert.notNull(standardMarketSellQueryVo.getMatchId(), "赛事id不能为null");
            Assert.notNull(standardMarketSellQueryVo.getSportId(), "赛种id不能为null");
            log.info("::{}::::开售前端请求参数::{}",CommonUtil.getRequestId(), JSON.toJSONString(standardMarketSellQueryVo));
            standardMarketSellQueryVo.setUserId(String.valueOf(TradeUserUtils.getUserId()));
            //调用赛事开售方法
            onSaleCommonServer.confirmMarketCategorySell(standardMarketSellQueryVo);
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(500, e.getMessage());
        }
    }


    @PostMapping("/match/list")
    public HttpResponse matchList(@RequestBody StandardMarketSellQueryV2Vo standardMarketSellQueryVo) {
        try {
            Assert.notNull(standardMarketSellQueryVo.getSportId(), "赛种id不能为null");
            Assert.notNull(standardMarketSellQueryVo.getMarketType(), "盘口类型不能为null");
            Assert.notNull(standardMarketSellQueryVo.getIsFavorite(), "是否收藏不能为null");

            Integer userId = TradeUserUtils.getUserId();
            String marketType = standardMarketSellQueryVo.getMarketType();
            QueryPreLiveMatchDTO dto = BeanCopyUtils.copyProperties(standardMarketSellQueryVo, QueryPreLiveMatchDTO.class);
            dto.setSellType(marketType);
            dto.setOrderType(0);
            if (StringUtils.isEmpty(standardMarketSellQueryVo.getIsOneselfReporter())) {
                dto.setPreTrader(StringUtils.isNotBlank(standardMarketSellQueryVo.getUserId()) ? standardMarketSellQueryVo.getUserId() : standardMarketSellQueryVo.getPreTrader());
            }
            dto.setIsOneselfReporter(StringUtils.isNotBlank(standardMarketSellQueryVo.getIsOneselfReporter()) ? Integer.valueOf(standardMarketSellQueryVo.getIsOneselfReporter()) : null);
            //查询收藏赛事
            if (standardMarketSellQueryVo.getIsFavorite() == 1) {
                RcsMatchCollection co = new RcsMatchCollection();
                co.setUserId(userId.longValue());
                co.setSportId(standardMarketSellQueryVo.getSportId());
                boolean isLive = standardMarketSellQueryVo.getLive() == 1;
                co.setMatchType(isLive ? 2 : 1);
                Long aLong = DateUtils.stringToDateAddTwelveHour(DateUtils.getDateExpect(System.currentTimeMillis()));
                co.setBeginTime(aLong);
                co.setMarketType(standardMarketSellQueryVo.getMarketType());

                //查询指派赛事
                List<Long> traderMatchIds = tradingAssignmentMapper.queryTradeMatchIds(String.valueOf(userId), isLive ? 1 : 0);
                List<Long> matchIds = rcsMatchCollectionService.queryCollMatchIds(co, traderMatchIds);
                log.info("::{}::::早盘滚求盘开售列表::traderMatchIds参数:{},matchIds:{}",CommonUtil.getRequestId(), JSON.toJSONString(traderMatchIds), JSON.toJSONString(matchIds));
                dto.setMatchIds(matchIds);
                dto.setUserId(userId.toString());
                dto.setEndTimeFrom(null);
            }

            //我和我的部下
            Set<String> userIdSet = new HashSet<>();
            if (StringUtils.isNotEmpty(standardMarketSellQueryVo.getOrgIdOrPersons())) {
                String[] split = standardMarketSellQueryVo.getOrgIdOrPersons().split(",");
                Integer[] orgList = new Integer[split.length];
                for (int x = 0; x < split.length; x++) {
                    orgList[x] = Integer.valueOf(split[x]);
                }
                Set<Integer> orgAllUser = systemUserOrgAuthApi.getOrgAllUser(orgList);
                if (CollectionUtils.isNotEmpty(orgAllUser)) {
                    for (Integer i : orgAllUser) {
                        userIdSet.add(String.valueOf(i));
                    }
                }
            }
            //特殊关照组
            if (standardMarketSellQueryVo.getIsSpecialPersons() != null && (standardMarketSellQueryVo.getIsSpecialPersons() != null && standardMarketSellQueryVo.getIsSpecialPersons() == 1)) {
                List<RcsMarketSellPersonGroup> rcsMarketSellPersonGroupList = getRcsMarketSellPersonGroup(userId);
                if (CollectionUtils.isNotEmpty(rcsMarketSellPersonGroupList)) {
                    for (RcsMarketSellPersonGroup rcsMarketSellPersonGroup : rcsMarketSellPersonGroupList) {
                        userIdSet.add(rcsMarketSellPersonGroup.getPersonId().toString());
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(userIdSet)) {
                dto.setUserIdList(new ArrayList<>(userIdSet));
            }
            log.info("::{}::::早盘滚球盘开售列表DUBBO请求参数::{}",CommonUtil.getRequestId(), JSON.toJSONString(dto));
            Response<PandaPage<QueryPreLiveMatchDto>> response = DataRealtimeApiUtils.handleApi(dto, new ApiCall() {
                @Override
                @Trace
                public <R> Response<R> callApi(Request request) {
                    Response<PandaPage<QueryPreLiveMatchBO>> rs = marketCategorySellApi.preLiveMarketList(request);
                    Response<PandaPage<QueryPreLiveMatchDto>> rs1 = JSONObject.parseObject(JSONObject.toJSONString(rs), new TypeReference<Response<PandaPage<QueryPreLiveMatchDto>>>() {
                    });
                    PandaPage<QueryPreLiveMatchBO> data = rs.getData();
                    log.info("::{}::::早盘滚球盘开售列表DUBBO响应参数::{}",CommonUtil.getRequestId(), JSON.toJSONString(data));
                    if (data != null) {
                        List<QueryPreLiveMatchBO> rtnList = data.getRecords();
                        if (CollectionUtils.isEmpty(rtnList)) {
                            return (Response<R>) rs1;
                        }

                        //获取赛事模板来源
                        List<Long> matchIds = rtnList.stream().map(QueryPreLiveMatchBO::getMatchInfoId).collect(Collectors.toList());
                        RcsTournamentTemplate template = new RcsTournamentTemplate();
                        template.setSportId(Integer.valueOf(String.valueOf(standardMarketSellQueryVo.getSportId())));
                        template.setMatchType(marketType.equals("PRE") ? 1 : 0);
                        List<RcsTournamentTemplate> list = rcsTournamentTemplateMapper.queryTemplateSourceByMatchId(template, matchIds);
                        Map<Long, String> temp = list.stream().filter(filter -> null != filter.getTypeVal() && null != filter.getTemplateName()).collect(Collectors.toMap(RcsTournamentTemplate::getTypeVal, RcsTournamentTemplate::getTemplateName));

                        List<QueryPreLiveMatchDto> dts = new ArrayList<>();
                        for (QueryPreLiveMatchBO bo : rtnList) {
                            QueryPreLiveMatchDto obj = BeanCopyUtils.copyProperties(bo, QueryPreLiveMatchDto.class);
                            if (temp.containsKey(obj.getMatchInfoId())) {
                                obj.setTemplateLevel(temp.get(obj.getMatchInfoId()));
                            } else {
                                //专用模板
                                obj.setTemplateLevel("-1");
                            }
                            dts.add(obj);
                        }
                        rcsMatchCollectionService.existList(dts, userId.longValue(), marketType);
                        rs1.getData().setRecords(dts);
                    }
                    return (Response<R>) rs1;
                }
            });
            if (StringUtils.isNotEmpty(standardMarketSellQueryVo.getOrgIdOrPersons()) || (standardMarketSellQueryVo.getIsSpecialPersons() != null && standardMarketSellQueryVo.getIsSpecialPersons() == 1)) {
                if (CollectionUtils.isEmpty(userIdSet)) {
                    response.getData().setRecords(null);
                    return HttpResponse.success(response);
                }
            }
            return HttpResponse.success(response);
        } catch (IllegalArgumentException ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(500, e.getMessage());
        }
    }

    private List<RcsMarketSellPersonGroup> getRcsMarketSellPersonGroup(Integer userId) {
        Map<String, Object> columnMap = new HashMap<>();
        columnMap.put("user_id", userId);
        columnMap.put("is_valid", 1);
        return rcsMarketSellPersonGroupMapper.selectByMap(columnMap);
    }

    @PostMapping("/match/changeStatusSource")
    @OperateLog(operateType = OperateLogEnum.DATA_SOURCE_CHANGE, operateParamter = OperateLogEnum.STATUS_SOURCE)
    public HttpResponse matchChangeStatusSource(@RequestBody StandardMarketSellQueryV2Vo standardMarketSellQueryVo) {
        try {
            ChangeMatchStatusDataSourceDTO dto = BeanCopyUtils.copyProperties(standardMarketSellQueryVo, ChangeMatchStatusDataSourceDTO.class);

            Integer userId = TradeUserUtils.getUserId();
            Map<String, Object> userMap = authRequiredPermission.getLoginUser(userId, TradeUserUtils.NEW_PLATFORM);
            String userName = String.valueOf(userMap.get("userCode"));
            String orgId = String.valueOf(userMap.get("orgId"));
            dto.setDepartmentId(orgId);
            dto.setUserId(String.valueOf(userId));
            dto.setUsername(userName);

            Response<Boolean> response = DataRealtimeApiUtils.handleApi(dto, new ApiCall() {
                @Override
                @Trace
                public <R> Response<R> callApi(Request request) {
                    return marketCategorySellApi.changeMatchStatusDataSource(request);
                }
            });

            return HttpResponse.success(response);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(-1, e.getMessage());
        }
    }

    @PostMapping("/match/changeEventSource")
    @OperateLog(operateType = OperateLogEnum.DATA_SOURCE_CHANGE, operateParamter = OperateLogEnum.EVENT_SOURCE)
    public HttpResponse matchChangeEventSource(@RequestBody StandardMarketSellQueryDto standardMarketSellQueryVo) {
        HttpResponse httpResponse;
        String linkId = CommonUtils.mdcPut();
        try {
            Assert.notNull(standardMarketSellQueryVo.getSportId(), "赛种id不能为null");
            Assert.notNull(standardMarketSellQueryVo.getMatchId(), "赛事id不能为null");
            Assert.notNull(standardMarketSellQueryVo.getDataSouceCode(), "事件源不能为null");

            ChangeBusinessEventSaleDTO dto = new ChangeBusinessEventSaleDTO();
            dto.setId(standardMarketSellQueryVo.getId());
            dto.setDataType(standardMarketSellQueryVo.getDataSouceCode());
            dto.setSportId(standardMarketSellQueryVo.getSportId());
            dto.setUserId(String.valueOf(TradeUserUtils.getUserId()));
            Response response = DataRealtimeApiUtils.handleApi(linkId + "_changeEventSource_trade", dto, new ApiCall() {
                @Override
                @Trace
                public <R> Response<R> callApi(Request request) {
                    return marketCategorySellApi.changeBusinessEvent(request);
                }
            });
            if (response.isSuccess()) {
                // 赛事封盘，只有滚球封盘
                StandardMatchInfo matchInfo = standardMatchInfoService.getById(standardMarketSellQueryVo.getMatchId());
                if (matchInfo != null && RcsConstant.isLive(matchInfo.getMatchStatus())) {
                    MarketStatusUpdateVO updateVO = new MarketStatusUpdateVO();
                    updateVO.setTradeLevel(TradeLevelEnum.MATCH.getLevel());
                    updateVO.setSportId(standardMarketSellQueryVo.getSportId());
                    updateVO.setMatchId(standardMarketSellQueryVo.getMatchId());
                    updateVO.setMarketStatus(TradeStatusEnum.SEAL.getStatus());
                    updateVO.setLinkedType(LinkedTypeEnum.EVENT_SOURCE_CHANGE.getCode());
                    updateVO.setMatchType(0);
                    updateVO.setLinkId(linkId);
                    String result = tradeStatusService.updateTradeStatus(updateVO);
                    log.info("::{}::事件源切换->赛事封盘完成：" + result,CommonUtil.getRequestId(standardMarketSellQueryVo.getMatchId()));
                }
                //更新赛事模板接拒单事件源切换
                rcsStandardSportMarketSellService.updateTemplateEventSourceConfig(standardMarketSellQueryVo);
            }
            httpResponse = HttpResponse.success(response);
        } catch (IllegalArgumentException ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            httpResponse = HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            httpResponse = HttpResponse.error(500, e.getMessage());
        } finally {
            CommonUtils.mdcRemove();
        }
        httpResponse.setLinkId(linkId);
        return httpResponse;
    }

    @PostMapping("/match/changeAnimationAndVideo")
    public HttpResponse matchChangeAnimationAndVideo(@RequestBody StandardMarketSellQueryV2Vo standardMarketSellQueryVo) {
        try {
            ChangeSaleParamDTO dto = BeanCopyUtils.copyProperties(standardMarketSellQueryVo, ChangeSaleParamDTO.class);
            Response response = DataRealtimeApiUtils.handleApi(dto, new ApiCall() {
                @Override
                @Trace
                public <R> Response<R> callApi(Request request) {
                    return marketCategorySellApi.changeLiveWeight(request);
                }
            });
            return HttpResponse.success(response);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(-1, e.getMessage());
        }
    }

    /**
     * 根据赛种获取开售赛事数量统计
     * <p>
     * 盘口类型marketType  PRE :早盘    LIVE :滾球
     * 前端只要传一个  盘口类型即可  手工计算今天到后面14天所有日期 作为条件传参
     * 调用融合只用如下参数 marketType  startTimeFrom endTimeFrom
     */
    @PostMapping("/getMarketSellNumberByType")
    public HttpResponse getMarketSellNumberByType(@RequestBody StandardMarketSellQueryV2Vo standardMarketSellQueryVo) {
        try {
            if (standardMarketSellQueryVo.getMarketType() == null) {
                return HttpResponse.fail("盘口类型不能为null");
            }
            //构建赛事开始和结束时间
            String date = DateUtils.getStandardCurrentTime();
            Date matchDate = DateUtils.dateStrToDate(date + " 12:00:00");
            //构建开始账务日期中午12点
            long beginTimeMillis = matchDate.getTime();
            long currentTime = System.currentTimeMillis();
            if (currentTime < beginTimeMillis) {
                //当前日期小于12点，所以需要往前推一天，构建昨天日期
                long oneDay = 24 * 60 * 60 * 1000;
                beginTimeMillis = beginTimeMillis - oneDay;
            }
            //构建结束账务日期，因为开售列表数据，其他早盘只统计延后7天的数据，所以这里需控制时间维度，加上日期条件和早盘日期条件共14天的维度
            long endTimeLong = DateUtils.addNDay(matchDate, 14).getTime();
            long endTimeMillis = endTimeLong - 1L;

            QueryPreLiveMatchDTO dto = BeanCopyUtils.copyProperties(standardMarketSellQueryVo, QueryPreLiveMatchDTO.class);
            dto.setSellType(standardMarketSellQueryVo.getMarketType());
            dto.setStartTimeFrom(beginTimeMillis);
            dto.setEndTimeFrom(endTimeMillis);
            dto.setIsFavorite(0);
            dto.setMatchStatus("Enable");
            Response<List<Map<Long, Integer>>> response = DataRealtimeApiUtils.handleApi(dto, new ApiCall() {
                @Override
                @Trace
                public <R> Response<R> callApi(Request request) {
                    Response<List<Map<Long, Integer>>> rs = marketCategorySellApi.queryMatchCountBySportId(request);
                    return (Response<R>) rs;
                }
            });
            return HttpResponse.success(response.getData());
        } catch (IllegalArgumentException ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(500, "根据赛种获取开售赛事数量统计，执行异常：" + ex.getMessage());
        }
    }

    /**
     * 输入用户名或用户代码时自动联想列出相关操盘手信息
     * 调用业务API
     * params1 固定风控编码1007
     * params2 用户名或用户代码
     */
    @PostMapping("/getSysUserByCondition")
    public HttpResponse getSysUserByCondition(@RequestBody StandardMarketSellQueryV2Vo standardMarketSellQueryVo) {
        try {
            if (standardMarketSellQueryVo.getUserCode() == null) {
                return HttpResponse.fail("用户名不能为null");
            }
            List<SysUserVO> list = iSysUserService.getSysUserByCondition(10007, standardMarketSellQueryVo.getUserCode());
            return HttpResponse.success(list);
        } catch (Exception ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(500, "调用业务查询用户信息API接口，执行异常：" + ex.getMessage());
        }
    }

    /**
     * 点击双方球队名称，弹窗显示玩法赔率
     *
     * @param standardMarketSellQueryVo
     * @return
     */
    @PostMapping("/match/getPlayOdds")
    public HttpResponse getPlayOdds(@RequestBody StandardMarketSellQueryV2Vo standardMarketSellQueryVo) {
        try {
            if (standardMarketSellQueryVo.getMatchId() == null) {
                return HttpResponse.fail("赛事id不能为null");
            }
            if (standardMarketSellQueryVo.getMarketType() == null) {
                return HttpResponse.fail("盘口类型不能为null");
            }
            QueryPreLiveMarketOddsDTO dto = BeanCopyUtils.copyProperties(standardMarketSellQueryVo, QueryPreLiveMarketOddsDTO.class);
            Long[] plays = {1L, 2L, 4L};
            dto.setMarketCategoryIds(Arrays.asList(plays));
            Response<List<QueryPreLiveMarketOddsBO>> response = DataRealtimeApiUtils.handleApi(dto, new ApiCall() {
                @Override
                @Trace
                public <R> Response<R> callApi(Request request) {
                    Response<List<QueryPreLiveMarketOddsBO>> rs = marketCategorySellApi.queryPreLiveMarketOdds(request);
                    return (Response<R>) rs;
                }
            });
            return HttpResponse.success(response.getData());
        } catch (Exception ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(500, "调用融合查询赔率弹窗接口，执行异常：" + ex.getMessage());
        }
    }

    /**
     * 切换视频或者动画的开关状态
     *
     * @param standardMarketSellQueryVo
     * @return
     */
    @PostMapping("/match/updateVideoAndAnimation")
    public HttpResponse updateVideoAndAnimation(@RequestBody StandardMarketSellQueryV2Vo standardMarketSellQueryVo) {
        try {
            if (standardMarketSellQueryVo.getMatchId() == null) {
                return HttpResponse.fail("赛事id不能为null");
            }
            UpdateVideoAnimationDTO dto = BeanCopyUtils.copyProperties(standardMarketSellQueryVo, UpdateVideoAnimationDTO.class);
            Integer userId = TradeUserUtils.getUserId();
            dto.setUserId(userId.toString());
            Response response = DataRealtimeApiUtils.handleApi(dto, new ApiCall() {
                @Override
                @Trace
                public <R> Response<R> callApi(Request request) {
                    Response rs = marketCategorySellApi.updateVideoAndAnimation(request);
                    return (Response<R>) rs;
                }
            });
            return HttpResponse.success(response.getData());
        } catch (Exception ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(500, "调用融合切换视频或者动画的开关状态接口，执行异常：" + ex.getMessage());
        }
    }

    /**
     * 手工完赛
     *
     * @param standardMarketSellQueryVo
     * @return
     */
    @PostMapping("/match/finishedMatch")
    public HttpResponse finishedMatch(@RequestBody StandardMarketSellQueryV2Vo standardMarketSellQueryVo) {
        try {
            if (standardMarketSellQueryVo.getMatchId() == null) {
                return HttpResponse.fail("赛事id不能为null");
            }
            FinishedMatchDTO dto = BeanCopyUtils.copyProperties(standardMarketSellQueryVo, FinishedMatchDTO.class);
            dto.setMatchInfoId(standardMarketSellQueryVo.getMatchId());
            Integer userId = TradeUserUtils.getUserId();
            dto.setUserId(userId.toString());
            Response response = DataRealtimeApiUtils.handleApi(dto, new ApiCall() {
                @Override
                @Trace
                public <R> Response<R> callApi(Request request) {
                    Response rs = marketCategorySellApi.finishedMatch(request);
                    return (Response<R>) rs;
                }
            });
            return HttpResponse.success(response.getData());
        } catch (Exception ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(500, ex.getMessage());
        }
    }

    /**
     * 手工重新开赛
     *
     * @param standardMarketSellQueryVo
     * @return
     */
    @PostMapping("/match/reopenMatch")
    public HttpResponse reopenMatch(@RequestBody StandardMarketSellQueryV2Vo standardMarketSellQueryVo) {
        try {
            if (standardMarketSellQueryVo.getMatchId() == null) {
                return HttpResponse.fail("赛事id不能为null");
            }
            ReopenMatchDTO dto = BeanCopyUtils.copyProperties(standardMarketSellQueryVo, ReopenMatchDTO.class);
            dto.setMatchInfoId(standardMarketSellQueryVo.getMatchId());
            if (StringUtils.isNotBlank(standardMarketSellQueryVo.getMatchStatus())) {
                dto.setMatchStatus(Integer.valueOf(standardMarketSellQueryVo.getMatchStatus()));
            }
            Integer userId = TradeUserUtils.getUserId();
            dto.setUserId(userId.toString());
            Response response = DataRealtimeApiUtils.handleApi(dto, new ApiCall() {
                @Override
                @Trace
                public <R> Response<R> callApi(Request request) {
                    Response rs = marketCategorySellApi.reopenMatch(request);
                    return (Response<R>) rs;
                }
            });
            return HttpResponse.success(response.getData());
        } catch (Exception ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(500, ex.getMessage());
        }
    }

    /**
     * 查询联赛名称
     *
     * @param standardMarketSellQueryVo
     * @return
     */
    @PostMapping("/match/queryTournamentInfo")
    public HttpResponse queryTournamentInfo(@RequestBody StandardMarketSellQueryV2Vo standardMarketSellQueryVo) {
        try {
            QueryTournamentInfoDTO dto = BeanCopyUtils.copyProperties(standardMarketSellQueryVo, QueryTournamentInfoDTO.class);

            //查询收藏赛事
            if (standardMarketSellQueryVo.getIsFavorite() == 1) {
                Integer userId = TradeUserUtils.getUserId();
                RcsMatchCollection co = new RcsMatchCollection();
                co.setUserId(userId.longValue());
                co.setSportId(standardMarketSellQueryVo.getSportId());
                boolean isLive = standardMarketSellQueryVo.getLive() == 1;
                co.setMatchType(isLive ? 2 : 1);
                Long aLong = DateUtils.stringToDateAddTwelveHour(DateUtils.getDateExpect(System.currentTimeMillis()));
                co.setBeginTime(aLong);
                //查询指派赛事
                List<Long> traderMatchIds = tradingAssignmentMapper.queryTradeMatchIds(String.valueOf(userId), isLive ? 1 : 0);
                List<Long> matchIds = rcsMatchCollectionService.queryCollMatchIds(co, traderMatchIds);
                dto.setMatchIds(matchIds);
            }
            Response response = DataRealtimeApiUtils.handleApi(dto, new ApiCall() {
                @Override
                @Trace
                public <R> Response<R> callApi(Request request) {
                    Response rs = marketCategorySellApi.queryTournamentInfo(request);
                    return (Response<R>) rs;
                }
            });
            return HttpResponse.success(response.getData());
        } catch (Exception ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(500, ex.getMessage());
        }
    }

    /**
     * 查询联赛区域名称
     *
     * @param standardMarketSellQueryVo
     * @return
     */
    @PostMapping("/match/getSellTournamentRegionList")
    public HttpResponse getSellTournamentRegionList(@RequestBody StandardMarketSellQueryV2Vo standardMarketSellQueryVo) {
        try {
            QueryTournamentInfoDTO dto = BeanCopyUtils.copyProperties(standardMarketSellQueryVo, QueryTournamentInfoDTO.class);

            //查询收藏赛事
            if (standardMarketSellQueryVo.getIsFavorite() == 1) {
                Integer userId = TradeUserUtils.getUserId();
                RcsMatchCollection co = new RcsMatchCollection();
                co.setUserId(userId.longValue());
                co.setSportId(standardMarketSellQueryVo.getSportId());
                boolean isLive = standardMarketSellQueryVo.getLive() == 1;
                co.setMatchType(isLive ? 2 : 1);
                Long aLong = DateUtils.stringToDateAddTwelveHour(DateUtils.getDateExpect(System.currentTimeMillis()));
                co.setBeginTime(aLong);

                //查询指派赛事
                List<Long> traderMatchIds = tradingAssignmentMapper.queryTradeMatchIds(String.valueOf(userId), isLive ? 1 : 0);
                List<Long> matchIds = rcsMatchCollectionService.queryCollMatchIds(co, traderMatchIds);
                dto.setMatchIds(matchIds);
            }
            Response response = DataRealtimeApiUtils.handleApi(dto, new ApiCall() {
                @Override
                @Trace
                public <R> Response<R> callApi(Request request) {
                    Response rs = marketCategorySellApi.getSellTournamentRegionList(request);
                    return (Response<R>) rs;
                }
            });
            return HttpResponse.success(response.getData());
        } catch (Exception ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(500, ex.getMessage());
        }
    }

    /**
     * 获取我的部下组织树
     *
     * @return
     */
    @RequestMapping(value = "/match/getMySubordinate", method = RequestMethod.GET)
    public HttpResponse getMySubordinate() {
        try {
            Integer userId = TradeUserUtils.getUserId();
            HashMap<String, Object> data = (HashMap<String, Object>) systemUserOrgAuthApi.getOrgTreeByUserId(userId);
            Object orgTreeByUserId = data.get("data");
            if (orgTreeByUserId != null) {
                List<HashMap<String, Object>> list = (List<HashMap<String, Object>>) orgTreeByUserId;
                List<Object> children = (List) list.get(0).get("children");
                return HttpResponse.success(children);
            }
            return HttpResponse.success(null);
        } catch (Exception ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage());
            return HttpResponse.error(500, "调用业务接口，获取我的部下组织树，异常：" + ex.getMessage());
        }
    }

    /**
     * 获取操盘部门所有人员列表
     *
     * @return
     */
    @GetMapping("/match/getSpecialGroupPerson")
    public HttpResponse getSpecialGroupPerson(Integer sportId) {
        try {
            if (sportId == null) {
                return HttpResponse.fail("体育种类id不能为null");
            }
            List<SysOrgAuthVO> list = systemUserOrgAuthApi.getCaopanMembers();
            List<SellSpecialGroupPersonVo> specialList = BeanCopyUtils.copyPropertiesList(list, SellSpecialGroupPersonVo.class);
            //根据操盘部所有人员，刷选已关注的特殊人员
            LambdaQueryWrapper<RcsMarketSellPersonGroup> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(RcsMarketSellPersonGroup::getSportId, sportId)
                    .eq(RcsMarketSellPersonGroup::getUserId, TradeUserUtils.getUserId())
                    .eq(RcsMarketSellPersonGroup::getIsValid, 1);
            List<RcsMarketSellPersonGroup> finishList = rcsMarketSellPersonGroupService.list(wrapper);
            Map<Long, Long> personMap = finishList.stream().collect(Collectors.toMap(RcsMarketSellPersonGroup::getPersonId, RcsMarketSellPersonGroup::getId));
            for (SellSpecialGroupPersonVo p : specialList) {
                Long u = Long.valueOf(p.getUserId());
                if (personMap.containsKey(u)) {
                    //标记为特殊关注人员
                    p.setIsSpecial(1);
                }
            }
            return HttpResponse.success(specialList);
        } catch (Exception ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(500, "调用业务接口，获取操盘部所有人员，异常：" + ex.getMessage());
        }
    }


    /**
     * 保存已选特殊关注人员
     *
     * @return
     */
    @PostMapping("/match/saveSpecialGroupPerson")
    public HttpResponse saveSpecialGroupPerson(@RequestBody StandardMarketSellQueryV2Vo standardMarketSellQueryVo) {
        try {
            if (standardMarketSellQueryVo.getSportId() == null) {
                return HttpResponse.fail("体育种类id不能为null");
            }
            Integer userId = TradeUserUtils.getUserId();
            standardMarketSellQueryVo.setUserId(userId.toString());
            rcsMarketSellPersonGroupService.saveSpecialGroupPerson(standardMarketSellQueryVo);
            return HttpResponse.success();
        } catch (Exception ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(500, "保存特殊关注人员，异常：" + ex.getMessage());
        }
    }

    /**
     * 查下历史赛事
     *
     * @param standardMarketSellQueryVo
     * @return
     */
    @PostMapping("/match/list/history")
    public HttpResponse matchListHistory(@RequestBody StandardMarketSellQueryV2Vo standardMarketSellQueryVo) {
        try {
            Assert.notNull(standardMarketSellQueryVo.getSportId(), "赛种不能为null");
            Assert.notNull(standardMarketSellQueryVo.getStartTimeFrom(), "开始时间不能为null");
            Assert.notNull(standardMarketSellQueryVo.getEndTimeFrom(), "结束时间不能为null");

            HistoryMatchQueryRequestDTO dto = BeanCopyUtils.copyProperties(standardMarketSellQueryVo, HistoryMatchQueryRequestDTO.class);
            Response<HistoryMatchQueryResponseBO> response = DataRealtimeApiUtils.handleApi(dto, new ApiCall() {
                @Override
                @Trace
                public <R> Response<R> callApi(Request request) {
                    Response<HistoryMatchQueryResponseBO> rs = marketCategorySellApi.queryHistoryMatch(request);
                    return (Response<R>) rs;
                }
            });
            return HttpResponse.success(response);
        } catch (IllegalArgumentException ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(500, e.getMessage());
        }
    }

    /**
     * 查询历史赛事联信息
     *
     * @param standardMarketSellQueryVo
     * @return
     */
    @PostMapping("/match/queryHistoryMatchTournamentInfo")
    public HttpResponse queryHistoryMatchTournamentInfo(@RequestBody StandardMarketSellQueryV2Vo standardMarketSellQueryVo) {
        try {
            Assert.notNull(standardMarketSellQueryVo.getSportId(), "赛种不能为null");
            Assert.notNull(standardMarketSellQueryVo.getStartTimeFrom(), "开始时间不能为null");
            Assert.notNull(standardMarketSellQueryVo.getEndTimeFrom(), "结束时间不能为null");

            HistoryMatchTournamentInfoRequestDTO dto = BeanCopyUtils.copyProperties(standardMarketSellQueryVo, HistoryMatchTournamentInfoRequestDTO.class);
            Response response = DataRealtimeApiUtils.handleApi(dto, new ApiCall() {
                @Override
                @Trace
                public <R> Response<R> callApi(Request request) {
                    Response rs = marketCategorySellApi.queryHistoryMatchTournamentInfo(request);
                    return (Response<R>) rs;
                }
            });
            return HttpResponse.success(response.getData());
        } catch (IllegalArgumentException ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(500, ex.getMessage());
        }
    }

    /**
     * 查询历史赛事联赛区域名称
     *
     * @param standardMarketSellQueryVo
     * @return
     */
    @PostMapping("/match/getHistorySellTournamentRegionList")
    public HttpResponse getHistorySellTournamentRegionList(@RequestBody StandardMarketSellQueryV2Vo standardMarketSellQueryVo) {
        try {
            Assert.notNull(standardMarketSellQueryVo.getSportId(), "赛种不能为null");
            Assert.notNull(standardMarketSellQueryVo.getStartTimeFrom(), "开始时间不能为null");
            Assert.notNull(standardMarketSellQueryVo.getEndTimeFrom(), "结束时间不能为null");

            HistoryMatchTournamentInfoRequestDTO dto = BeanCopyUtils.copyProperties(standardMarketSellQueryVo, HistoryMatchTournamentInfoRequestDTO.class);
            Response response = DataRealtimeApiUtils.handleApi(dto, new ApiCall() {
                @Override
                @Trace
                public <R> Response<R> callApi(Request request) {
                    Response rs = marketCategorySellApi.getHistorySellTournamentRegionList(request);
                    return (Response<R>) rs;
                }
            });
            return HttpResponse.success(response.getData());
        } catch (IllegalArgumentException ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(500, ex.getMessage());
        }
    }
}
