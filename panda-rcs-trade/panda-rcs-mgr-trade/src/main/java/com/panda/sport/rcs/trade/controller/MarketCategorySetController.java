package com.panda.sport.rcs.trade.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.util.UuidUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.google.common.collect.Maps;
import com.panda.sport.data.rcs.api.Response;
import com.panda.sport.rcs.common.CategorySetMargin;
import com.panda.sport.rcs.common.ProducerTopicEnum;
import com.panda.sport.rcs.constants.RcsErrorInfoConstants;
import com.panda.sport.rcs.constants.RedisKeys;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.MarketKindEnum;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.log.annotion.LogAnnotion;
import com.panda.sport.rcs.mapper.RcsMarketCategorySetRelationMapper;
import com.panda.sport.rcs.mapper.StandardSportTournamentMapper;
import com.panda.sport.rcs.mongo.CategoryCollection;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.*;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.MarketCategorySetService;
import com.panda.sport.rcs.trade.wrapper.RcsMarketCategorySetMarginService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketCategoryService;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.MarketCategoryQueryVO;
import com.panda.sport.rcs.vo.MarketCategorySetResVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.panda.sport.rcs.enums.PlayStateEnum.e;

/**
 * @author Felix
 * 玩法集管理
 */
@Slf4j
@RestController
@RequestMapping(value = "categorySet")
public class MarketCategorySetController {
    private final static String TAGS = "margin";
    @Autowired
    MarketCategorySetService marketCategorySetService;
    @Autowired
    StandardSportTournamentMapper standardSportTournamentMapper;
    @Autowired
    RcsMarketCategorySetMarginService rcsMarketCategorySetMarginService;
    @Autowired
    private StandardSportMarketCategoryService standardSportMarketCategoryService;
    @Autowired
    private RcsMarketCategorySetRelationMapper rcsMarketCategorySetRelationMapper;
    @Autowired
    private ProducerSendMessageUtils sendMessage;

    @Autowired
    private RedisClient redisClient;

    private static String MATCH_PLAY_SET_KEY = "rcs:match:event:play:set";
    private static String RCS_PLAY_SET_UPDATE_TOPIC = "RCS_PLAY_SET_UPDATE_TOPIC";
    private static Integer RCS_PLAY_SET_CLOSE_STATS = 3;

    /**
     * 玩法集列表
     *
     * @return
     */
    @RequestMapping(value = "findCategorySetList")
    public HttpResponse<Map<String, Object>> findCategorySetList(RcsMarketCategorySet rcsMarketCategorySet) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            List<RcsMarketCategorySet> categorySetList = marketCategorySetService.findCategorySetList(rcsMarketCategorySet);
            resultMap.put("categorySetList", categorySetList);
            return HttpResponse.success(resultMap);
        } catch (Exception e) {
            log.error("::{}::玩法集列表:{}", CommonUtil.getRequestId(rcsMarketCategorySet.getId()), e.getMessage(), e);
            return HttpResponse.error(RcsErrorInfoConstants.FAIL, "风控服务器出问题");
        }
    }

    /**
     * @Description 分页玩法集列表
     * @Param [rcsMarketCategorySet, current, size]
     * current 当前页 size页数大小
     * @Author kimi
     * @Date 2020/2/13
     * @return com.panda.sport.rcs.vo.HttpResponse<java.util.Map < java.lang.String, java.lang.Object>>
     **/
    @RequestMapping(value = "findPageCategorySetList")
    public HttpResponse<Map<String, Object>> findPageCategorySetList(RcsMarketCategorySet rcsMarketCategorySet, @RequestParam("current") int current, @RequestParam("size") int size) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            IPage categorySetList = marketCategorySetService.findPageCategorySetList(rcsMarketCategorySet, current, size);
            resultMap.put("categorySetList", categorySetList);
            return HttpResponse.success(resultMap);
        } catch (Exception e) {
            log.error("::{}::分页玩法集列表:{}", CommonUtil.getRequestId(rcsMarketCategorySet.getId()), e.getMessage(), e);
            return HttpResponse.error(RcsErrorInfoConstants.FAIL, "风控服务器出问题");
        }
    }

    /**
     * 修改玩法集
     *
     * @param rcsMarketCategorySet
     */
    @RequestMapping(value = "updateCategorySet", method = RequestMethod.PUT)
    @LogAnnotion(name = "修改玩法集", keys = {"sportId", "type", "name", "tournamentLevel", "orderNo", "returnRate", "status", "remark"},
        title = {"运动种类id", "玩法集类型", "玩法集名称", "关联联赛等级", "排序值", "返回率", "玩法状态", "备注"})
    public HttpResponse<Map<String, Object>> updateCategorySet(@RequestBody List<RcsMarketCategorySet> rcsMarketCategorySet) {
        try {
            log.info("::{}::修改玩法集，操盘手:{}", CommonUtil.getRequestId(), TradeUserUtils.getUserIdNoException());
            marketCategorySetService.updateCategorySetList(rcsMarketCategorySet);
            return HttpResponse.success();
        } catch (RcsServiceException e) {
            log.error("::{}::修改玩法集:{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(RcsErrorInfoConstants.FAIL, "风控服务器出问题"+e.getMessage());
        } catch (Exception e) {
            log.error("::{}::修改玩法集:{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(RcsErrorInfoConstants.FAIL, "风控服务器出问题");
        }
    }

    /**
     * 玩法列表
     *
     * @param standardSportMarketCategory
     * @return
     */
    @RequestMapping(value = "findMarketCategoryList")
    public HttpResponse<Map<String, Object>> findCategorySetList(StandardSportMarketCategory standardSportMarketCategory) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            List<StandardSportMarketCategory> standardSportMarketCategoryList = marketCategorySetService.findStandardSportMarketCategoryList(standardSportMarketCategory);
            resultMap.put("standardSportMarketCategoryList", standardSportMarketCategoryList);
            return HttpResponse.success(resultMap);
        } catch (Exception e) {
            log.error("::{}::玩法列表:{}", CommonUtil.getRequestId(standardSportMarketCategory.getId()), e.getMessage(), e);
            return HttpResponse.error(RcsErrorInfoConstants.FAIL, "风控服务器出问题");
        }
    }

    /**
     * 足球矩阵玩法列表
     *
     * @return
     */
    @RequestMapping(value = "findMarketCategoryListForSoccer")
    public HttpResponse<Map<String, Object>> findMarketCategoryListForSoccer() {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            List<StandardSportMarketCategoryRefReqVo> marketCategoryListForSoccer = marketCategorySetService.findMarketCategoryListForSoccer();
            resultMap.put("marketCategoryListForSoccer", marketCategoryListForSoccer);
            return HttpResponse.success(resultMap);
        } catch (Exception e) {
            log.error("::{}::足球矩阵玩法列表:{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(RcsErrorInfoConstants.FAIL, "风控服务器出问题");
        }
    }

    /**
     * 玩法集内容
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "findMarketCategoryContent")
    public HttpResponse<Map<String, Object>> findMarketCategoryContent(@RequestParam Integer id) {
        try {
            Map<String, Object> resultMap = new HashMap<>();
            List<StandardSportMarketCategory> findMarketCategoryContent = marketCategorySetService.findMarketCategoryContent(id);
            resultMap.put("findMarketCategoryContent", findMarketCategoryContent);
            return HttpResponse.success(resultMap);
        } catch (Exception e) {
            log.error("::{}::玩法集内容:{}", CommonUtil.getRequestId(id), e.getMessage(), e);
            return HttpResponse.error(RcsErrorInfoConstants.FAIL, "风控服务器出问题");

        }
    }

    /**
     * 新建玩法集 & 新增玩法内容
     *
     * @param operatingParam
     * @return
     */
    @RequestMapping(value = "addCategorySetAndCategory", method = RequestMethod.POST)
    public HttpResponse<Map<String, Object>> addCategorySetAndCategory(@RequestBody Map<String, Object> operatingParam) {
        try {
            Map<String, Object> result = marketCategorySetService.addCategorySetAndCategory(operatingParam);
            return getMapHttpResponse(result,operatingParam);
        } catch (RcsServiceException e) {
            log.error("::{}::addCategorySetAndCategory:{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(RcsErrorInfoConstants.FAIL, "风控操作失败" + e.getMessage());
        } catch (Exception e) {
            log.error("::{}::addCategorySetAndCategory:{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(RcsErrorInfoConstants.FAIL, "数据更新失败");
        }
    }

    /**
     * 编辑玩法集 & 新增or修改玩法内容
     *
     * @param operatingParam
     * @return
     */
    @RequestMapping(value = "updateCategorySetAndCategory")
    public HttpResponse<Map<String, Object>> updateCategorySetAndCategory(@RequestBody Map<String, Object> operatingParam) {
        try {
            log.info("::{}::开始修改玩法集内容:{},操盘手:{}",CommonUtil.getRequestId(), JSON.toJSONString(operatingParam), TradeUserUtils.getUserId());
            Map<String, Object> result = marketCategorySetService.updateCategorySetAndCategory(operatingParam);
            return getMapHttpResponse(result,operatingParam);
        } catch (RcsServiceException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(RcsErrorInfoConstants.FAIL,"风控操作失败 "+e.getMessage());
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(RcsErrorInfoConstants.FAIL, "数据更新失败");
        }
    }

    private HttpResponse<Map<String, Object>> getMapHttpResponse(Map<String, Object> result,Map<String, Object> operatingParam) throws UnsupportedEncodingException {
        Response res = (Response) result.get("Response");
        //发送MQ  只发送风控的玩法集
        RcsMarketCategorySet rcsMarketCategorySet = (RcsMarketCategorySet) result.get("rcsMarketCategorySet");
        if (null != rcsMarketCategorySet && rcsMarketCategorySet.getType() == 1) {
            sendMargin(rcsMarketCategorySet);
        }
        HttpResponse httpResponse;
        try {
             httpResponse = BeanCopyUtils.copyProperties(res, HttpResponse.class);
        }catch (NullPointerException e){
            httpResponse=new HttpResponse();
            httpResponse.setCode(res.getCode());
            httpResponse.setMsg(res.getMsg());
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        result.remove("Response");
        httpResponse.setData(result);
        // 发送玩法集变更到业务
        RcsMarketCategorySet set = JSONObject.parseObject(JSONObject.toJSONString(operatingParam.get("marketCategorySet")),RcsMarketCategorySet.class);
        set.setId(rcsMarketCategorySet.getId());
        operatingParam.put("marketCategorySet",set);
        sendPlayCategoryMQ(operatingParam);
        //清除redis缓存
        String cacheKey = String.format(RedisKeys.RCSCACHE_MARKET_CATEGORY_SET, rcsMarketCategorySet.getSportId());
        redisClient.delete(cacheKey);
        List<RcsMarketCategorySetRelation> relationList = JSONArray.parseArray(JSONObject.toJSONString(operatingParam.get("categoryList")), RcsMarketCategorySetRelation.class);
        if (CollectionUtils.isNotEmpty(relationList)){
            clearPlaySetCache(relationList);
        }
        return httpResponse;
    }
    /**
     * @Description   //清楚玩法集对应的玩法缓存
     * @Param [rcsMarketCategorySet]
     * @Author  Sean
     * @Date  20:25 2020/9/8
     * @return void
     **/
    private void clearPlaySetCache(List<RcsMarketCategorySetRelation> relations) {
        log.info("::{}::玩法集有变更，清除玩法集缓存",CommonUtil.getRequestId());
        redisClient.delete(MATCH_PLAY_SET_KEY);
//        if (CollectionUtils.isNotEmpty(relations)){
//            for (RcsMarketCategorySetRelation relation : relations){
//                if (ObjectUtils.isNotEmpty(relation)){
////                    String playSetKey = String.format(MATCH_PLAY_SET_KEY,relation.getMarketCategoryId());
//                }
//            }
//        }
    }

    /*******1.0补丁版本*********/

    /**
     * 批量删除玩法集内容
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "deleteCategorySetContent")
    @LogAnnotion(name = "批量删除玩法集内容", keys = {"id"}, title = {"玩法集id"})
    public HttpResponse deleteCategorySetContent(@RequestParam ArrayList<Long> id) {
        try {
            log.info("::{}::开始删除玩法集内容:{},操盘手:{}",CommonUtil.getRequestId(), JSON.toJSONString(id), TradeUserUtils.getUserId());
            return HttpResponse.success(marketCategorySetService.deleteCategorySetContent(id));
        } catch (Exception e) {
            log.error("::{}::开始删除玩法集内容:{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(RcsErrorInfoConstants.FAIL, "风控服务器出问题");
        }
    }

    /**
     * 删除玩法集
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "deleteCategorySet")
    @LogAnnotion(name = "删除玩法集", keys = {"id"}, title = {"玩法集id"})
    public HttpResponse deleteCategorySet(@RequestParam Long id) {
        try {
            log.info("::{}::开始删除玩法集:{},操盘手:{}",CommonUtil.getRequestId(), JSON.toJSONString(id), TradeUserUtils.getUserId());
            RcsMarketCategorySet rcsMarketCategorySet = marketCategorySetService.getById(id);
            marketCategorySetService.deleteCategorySet(id);
            //清除redis缓存
            String cacheKey = String.format(RedisKeys.RCSCACHE_MARKET_CATEGORY_SET, rcsMarketCategorySet.getSportId());
            redisClient.delete(cacheKey);

            //清楚玩法集对应玩法缓存
            QueryWrapper<RcsMarketCategorySetRelation> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(RcsMarketCategorySetRelation ::getMarketCategorySetId,id);
            List<RcsMarketCategorySetRelation> relations = rcsMarketCategorySetRelationMapper.selectList(queryWrapper);
            if (CollectionUtils.isNotEmpty(relations)){
                clearPlaySetCache(relations);
            }
            // 发送玩法集变更到业务
            if (ObjectUtils.isNotEmpty(rcsMarketCategorySet)){
                rcsMarketCategorySet.setStatus(RCS_PLAY_SET_CLOSE_STATS);
                Map<String,Object> map = Maps.newHashMap();
                map.put("marketCategorySet",rcsMarketCategorySet);
                map.put("categoryList",relations);
                String uuid = UuidUtils.generateUuid();
                log.info("::{}::删除玩法集uuid={}",CommonUtil.getRequestId(),uuid);
                sendMessage.sendMessage(RCS_PLAY_SET_UPDATE_TOPIC,null,uuid,map);
            }
            return HttpResponse.success();
        } catch (Exception e) {
            log.error("::{}::开始删除玩法集:{}", CommonUtil.getRequestId(id), e.getMessage(), e);
            return HttpResponse.error(RcsErrorInfoConstants.FAIL, "风控服务器出问题");
        }
    }


    public void sendMargin(RcsMarketCategorySet rcsMarketCategorySet) throws UnsupportedEncodingException {
        //margin设置
        LinkedHashMap<String, RcsMarketCategorySetMargin> resultFindMargin = new LinkedHashMap<>(6);
        List<RcsMarketCategorySetMargin> findMargin = rcsMarketCategorySetMarginService.findMargin(rcsMarketCategorySet.getId());
        Collections.sort(findMargin, (a, b) -> b.getTimeFrame().compareTo(a.getTimeFrame()));
        findMargin.forEach(margin -> {
            resultFindMargin.put(CategorySetMargin.getTimeFrame(margin.getTimeFrame()), margin);
        });
        //玩法
        List<StandardSportMarketCategory> findMarketCategoryContent = marketCategorySetService.findMarketCategoryContent(rcsMarketCategorySet.getId().intValue());
        rcsMarketCategorySet.setMargin(resultFindMargin);
        rcsMarketCategorySet.setCategoryList(findMarketCategoryContent);
        Message msg = new Message(ProducerTopicEnum.Margin.getValue(), TAGS, JsonFormatUtils.toJson(rcsMarketCategorySet).getBytes(RemotingHelper.DEFAULT_CHARSET));
        sendMessage.sendMessage(msg);
    }

    /**
     * 查询玩法集列表
     *
     * @param marketCategoryQueryVO
     * @return
     * @author Paca
     */
    @PostMapping("/list")
    public HttpResponse list(@RequestBody MarketCategoryQueryVO marketCategoryQueryVO) {
        Long sportId = marketCategoryQueryVO.getSportId();
        if (sportId == null || sportId <= 0) {
            return HttpResponse.fail("运动种类ID[sportId]有误");
        }
        Long matchId = marketCategoryQueryVO.getMatchId();
        if (matchId == null || matchId <= 0) {
            return HttpResponse.fail("赛事ID[matchId]有误");
        }
        try {
            List<MarketCategorySetResVO> list = marketCategorySetService.list(sportId, matchId, marketCategoryQueryVO.getMatchSnapshot());
            return HttpResponse.success(list);
        } catch (Exception e) {
			log.error("::{}::查询玩法集列表异常:{}", CommonUtil.getRequestId(marketCategoryQueryVO.getMatchId()), e.getMessage(), e);
            return HttpResponse.fail("查询玩法集列表异常：" + e.getMessage());
        }
    }

    @PostMapping("/getAllTemplateId")
    public HttpResponse getAllCategoryTemplateId(@RequestBody MarketCategoryQueryVO marketCategoryQueryVO) {
        Long sportId = marketCategoryQueryVO.getSportId();
        if (sportId == null || sportId <= 0) {
            return HttpResponse.fail("运动种类ID有误");
        }
        try {
            Map<Long, Integer> map = standardSportMarketCategoryService.getAllCategoryTemplateId(sportId);
            return HttpResponse.success(map);
        } catch (Exception e) {
			log.error("::{}::获取所有玩法模板ID异常:{}", CommonUtil.getRequestId(marketCategoryQueryVO.getMatchId()), e.getMessage(), e);
            return HttpResponse.fail("获取所有玩法模板ID异常：" + e.getMessage());
        }
    }

    /**
     * 查询玩法集下盘口详情
     *
     * @param marketCategoryQueryVO
     * @return
     * @author Paca
     */
    @PostMapping("/marketDetail")
    public HttpResponse marketDetail(@RequestBody MarketCategoryQueryVO marketCategoryQueryVO) {
        Long sportId = marketCategoryQueryVO.getSportId();
        Long matchId = marketCategoryQueryVO.getMatchId();
        Long categorySetId = marketCategoryQueryVO.getCategorySetId();
        if (sportId == null || sportId <= 0) {
            return HttpResponse.fail("运动种类ID有误");
        }
        if (matchId == null || matchId <= 0) {
            return HttpResponse.fail("赛事ID有误");
        }
        if (categorySetId == null || categorySetId < 0) {
            return HttpResponse.fail("玩法集ID有误");
        }
        MarketKindEnum marketKindEnum = MarketKindEnum.getMarketKindByValue(marketCategoryQueryVO.getMarketOddsKind());
        if (marketKindEnum == null) {
            return HttpResponse.fail("赔率类型有误");
        }
        try {
            CategoryCollection result = marketCategorySetService.marketDetail(marketCategoryQueryVO);
            return HttpResponse.success(result);
        } catch (RcsServiceException e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        } catch (Exception e) {
            log.error("::{}::查询玩法集下盘口详情异常:{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("查询玩法集下盘口详情异常：" + e.getMessage());
        }
    }
    /**
     * @Description   //玩法集有变动发送MQ消息到业务
     * @Param [operatingParam]
     * @Author  Sean
     * @Date  19:58 2020/9/25
     * @return void
     **/
    private void sendPlayCategoryMQ(Map<String, Object> operatingParam){
        String uuid = UuidUtils.generateUuid();
        sendMessage.sendMessage(RCS_PLAY_SET_UPDATE_TOPIC,null,uuid,operatingParam);
    }
}
