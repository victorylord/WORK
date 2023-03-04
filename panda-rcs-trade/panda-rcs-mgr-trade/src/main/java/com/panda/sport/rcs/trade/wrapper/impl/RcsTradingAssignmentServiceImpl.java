package com.panda.sport.rcs.trade.wrapper.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.panda.merge.dto.Request;
import com.panda.merge.dto.Response;
import com.panda.merge.dto.ResultCode;
import com.panda.sport.manager.api.IMarketCategorySellApi;
import com.panda.sport.manager.api.dto.UpdateOperateTraderDTO;
import com.panda.sport.rcs.cache.RcsCacheUtils;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.MarketCategorySetMapper;
import com.panda.sport.rcs.mapper.RcsCategorySetTraderWeightMapper;
import com.panda.sport.rcs.mapper.RcsShiftMapper;
import com.panda.sport.rcs.mapper.RcsStandardSportMarketSellMapper;
import com.panda.sport.rcs.mapper.RcsTradingAssignmentMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.mongo.MatchMarketLiveBean;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsCategorySetTraderWeight;
import com.panda.sport.rcs.pojo.RcsMarketCategorySet;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsStandardSportMarketSell;
import com.panda.sport.rcs.pojo.RcsSysUser;
import com.panda.sport.rcs.pojo.RcsTradingAssignment;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.vo.TradingAssignmentDataVo;
import com.panda.sport.rcs.trade.service.TradeVerificationService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.ChangePersonLiableVo;
import com.panda.sport.rcs.trade.vo.TradingAssignmentSubPlayVo;
import com.panda.sport.rcs.trade.vo.TradingAssignmentVo;
import com.panda.sport.rcs.trade.wrapper.MatchService;
import com.panda.sport.rcs.trade.wrapper.RcsCategorySetTraderWeightService;
import com.panda.sport.rcs.trade.wrapper.RcsSysUserService;
import com.panda.sport.rcs.trade.wrapper.RcsTradingAssignmentService;
import com.panda.sport.rcs.trade.wrapper.SportMatchViewService;
import com.panda.sport.rcs.utils.StringUtils;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.RcsMarketCategorySetVo;
import com.panda.sports.api.ISystemUserOrgAuthApi;
import com.panda.sports.api.vo.ShortSysUserVO;
import com.panda.sports.api.vo.SysTraderVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2020-11-07 16:50
 **/
@Service
@Slf4j
public class RcsTradingAssignmentServiceImpl extends ServiceImpl<RcsTradingAssignmentMapper, RcsTradingAssignment> implements RcsTradingAssignmentService {
    @Autowired
    private RcsStandardSportMarketSellMapper rcsStandardSportMarketSellMapper;
    @Autowired
    private MarketCategorySetMapper rcsMarketCategorySetMapper;
    @Autowired
    private MatchService matchService;
    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;
    @Autowired
    private RcsTradingAssignmentMapper rcsTradingAssignmentMapper;
    @Autowired
    private SportMatchViewService sportMatchViewService;
    @Autowired
    private RcsCategorySetTraderWeightService rcsCategorySetTraderWeightService;
    @Autowired
    private RcsSysUserService rcsSysUserService;
    @Autowired
    private RcsShiftMapper rcsShiftMapper;
    @Autowired
    MarketCategorySetMapper marketCategorySetMapper;
    @Autowired
    private RcsCategorySetTraderWeightMapper rcsCategorySetTraderWeightMapper;
    @Autowired
    private MongoTemplate mongotemplate;
    @Autowired
    private ProducerSendMessageUtils sendMessage;
    @Autowired
    private TradeVerificationService tradeVerificationService;
    @Reference(check = false, lazy = true, retries = 1, timeout = 100000)
    private IMarketCategorySellApi iMarketCategorySellApi;
    @Reference(check = false, lazy = true, retries = 1, timeout = 5000)
    private ISystemUserOrgAuthApi systemUserOrgAuthApi;
//    private static HashMap<Integer,SysTraderVO>  sysTraderVOIdHashMap=new HashMap<>();
//    private static HashMap<String,SysTraderVO>  sysTraderVONameHashMap=new HashMap<>();

    private static Cache<String, SysTraderVO> sysTraderVOIdCache = RcsCacheUtils.newSyncSimpleCache(1000, 20000, 600);
    private static Cache<String, SysTraderVO> sysTraderVONameCache = RcsCacheUtils.newSyncSimpleCache(1000, 20000, 600);
    private static Cache<String, ShortSysUserVO> shortSysUserVOIdCache = RcsCacheUtils.newSyncSimpleCache(1000, 20000, 600);
    private static Cache<String, ShortSysUserVO> shortSysUserVONameCache = RcsCacheUtils.newSyncSimpleCache(1000, 20000, 600);

    //    private static HashMap<Integer,ShortSysUserVO>  shortSysUserVOIdHashMap=new HashMap<>();
//    private static HashMap<String,ShortSysUserVO>  shortSysUserVONameHashMap=new HashMap<>();
    //private static Long updateTime=0L;
    //19 操盘管理层       26 操盘总监     14 资深操盘手
    private static String TRADER_SENIOR = "14";
    private static String TRADER_MANAGER = "19";
    private static String TRADER_GENERAL = "26";
//    @Override
//    public List<RcsTradingAssignment> sellAddRcsTradingAssignment(Long sportId,Long matchId) {
//        Map<String, Object> columnMap=new HashMap<>();
//        columnMap.put("match_id", matchId);
//        if (!isConfirm) {
//             List<RcsTradingAssignment> rcsTradingAssignmentList1 = rcsTradingAssignmentMapper.selectByMap(columnMap);
//             if (!CollectionUtils.isEmpty(rcsTradingAssignmentList1)) {
//                 return null;
//             }
//         }
//        RcsStandardSportMarketSell rcsStandardSportMarketSell = rcsStandardSportMarketSellMapper.selectRcsStandardSportMarketSellByMatchInfoId(matchId);
//        List<RcsMarketCategorySetVo> rcsMarketCategorySetVoList = rcsMarketCategorySetMapper.selectRcsMarketCategorySet(sportId);
//        if (!CollectionUtils.isEmpty(rcsMarketCategorySetVoList)){
//            RcsMarketCategorySetVo rcsMarketCategorySetVo1=new RcsMarketCategorySetVo();
//            rcsMarketCategorySetVo1.setId(-1L);
//            rcsMarketCategorySetVoList.add(rcsMarketCategorySetVo1);
//            List<RcsTradingAssignment> rcsTradingAssignmentList=new ArrayList<>();
//            for (RcsMarketCategorySetVo rcsMarketCategorySetVo:rcsMarketCategorySetVoList){
//                if (rcsStandardSportMarketSell.getPreTraderId()!=null &&rcsStandardSportMarketSell.getPreTraderId().length()>0) {
//                    RcsTradingAssignment rcsTradingAssignment = new RcsTradingAssignment();
//                    rcsTradingAssignment.setMatchId(matchId);
//                    rcsTradingAssignment.setMatchType(0);
//                    rcsTradingAssignment.setUserId(rcsStandardSportMarketSell.getPreTraderId());
//                    rcsTradingAssignment.setPlayCollectionId(rcsMarketCategorySetVo.getId());
//                    rcsTradingAssignment.setStatus(1);
//                    rcsTradingAssignmentList.add(rcsTradingAssignment);
//                }
//                if (rcsStandardSportMarketSell.getLiveTraderId()!=null &&rcsStandardSportMarketSell.getLiveTraderId().length()>0) {
//                    RcsTradingAssignment rcsTradingAssignment1 = new RcsTradingAssignment();
//                    rcsTradingAssignment1.setMatchId(matchId);
//                    rcsTradingAssignment1.setMatchType(1);
//                    rcsTradingAssignment1.setUserId(rcsStandardSportMarketSell.getLiveTraderId());
//                    rcsTradingAssignment1.setPlayCollectionId(rcsMarketCategorySetVo.getId());
//                    rcsTradingAssignment1.setStatus(1);
//                    rcsTradingAssignmentList.add(rcsTradingAssignment1);
//                }
//            }
//            if (!CollectionUtils.isEmpty(rcsTradingAssignmentList)){
//                saveBatch(rcsTradingAssignmentList);
//                matchService.updateTraderNums(matchId,1);
//                return  rcsTradingAssignmentList;
//            }
//        }
//        return null;
//    }

    @Override
    public boolean tradeJurisdictionByPlayId(Long sportId, Long matchId, Long playId, Integer matchType) {
        try {
            Integer userId = TradeUserUtils.getUserId();
            StandardMatchInfo standardMatchInfo = standardMatchInfoMapper.selectById(matchId);
            if (matchType == null) {
                if (standardMatchInfo.getMatchStatus() == 1 || standardMatchInfo.getMatchStatus() == 2 || standardMatchInfo.getMatchStatus() == 10) {
                    matchType = 0;
                } else {
                    matchType = 1;
                }
            }
            if (isPreTrade(matchType)) {
                return true;
            }
            if (sportId == null) {
                sportId = standardMatchInfo.getSportId();
            }
            if (verify(matchId, matchType, userId)) {
                return true;
            }
            List<Long> longs = rcsMarketCategorySetMapper.selectPlayIdListByPlaySet(sportId);
            if (!CollectionUtils.isEmpty(longs) && longs.contains(playId)) {
                List<Long> playIds = rcsMarketCategorySetMapper.selectPlayIdList(sportId, matchId, 1 - matchType, userId.longValue());
                if (!CollectionUtils.isEmpty(playIds)) {
                    return playIds.contains(playId);
                }
                return false;
            } else {
                return verifyOthers(matchId, 1 - matchType, userId);
            }
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean tradeJurisdictionByPlayId(Long playId, StandardMatchInfo matchInfo) {
        try {
            Long matchId = matchInfo.getId();
            Long sportId = matchInfo.getSportId();
            // 0-滚球，1-早盘
            Integer matchType = RcsConstant.isLive(matchInfo.getMatchStatus()) ? 0 : 1;
            Integer userId = TradeUserUtils.getUserId();
            if (isPreTrade(matchType)) {
                return true;
            }
            if (verify(matchId, matchType, userId)) {
                return true;
            }
            List<Long> longs = rcsMarketCategorySetMapper.selectPlayIdListByPlaySet(sportId);
            if (!CollectionUtils.isEmpty(longs) && longs.contains(playId)) {
                List<Long> playIds = rcsMarketCategorySetMapper.selectPlayIdList(sportId, matchId, 1 - matchType, userId.longValue());
                if (!CollectionUtils.isEmpty(playIds)) {
                    return playIds.contains(playId);
                }
                return false;
            } else {
                return verifyOthers(matchId, 1 - matchType, userId);
            }
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean tradeJurisdictionByPlayIdList(Long sportId, Long matchId, List<Long> playIdList, Integer matchType) {
        try {
            StandardMatchInfo standardMatchInfo = standardMatchInfoMapper.selectById(matchId);
            if (matchType == null) {
                if (standardMatchInfo.getMatchStatus() == 2 || standardMatchInfo.getMatchStatus() == 1 || standardMatchInfo.getMatchStatus() == 10) {
                    matchType = 0;
                } else {
                    matchType = 1;
                }
            }
            if (isPreTrade(matchType)) {
                return true;
            }
            if (sportId == null) {
                sportId = standardMatchInfo.getSportId();
            }
            Integer userId = TradeUserUtils.getUserId();
            List<Long> playIdListFengKong = new ArrayList<>();
            List<Long> playIdListQiTa = new ArrayList<>();
            if (verify(matchId, matchType, userId)) {
                return true;
            }
            List<Long> longs = rcsMarketCategorySetMapper.selectPlayIdListByPlaySet(sportId);
            if (CollectionUtils.isEmpty(longs)) {
                playIdListQiTa = longs;
            } else {
                for (Long id : playIdList) {
                    if (longs.contains(id)) {
                        playIdListFengKong.add(id);
                    } else {
                        playIdListQiTa.add(id);
                    }
                }
            }
            if (!CollectionUtils.isEmpty(playIdListFengKong)) {
                List<Long> playIds = rcsMarketCategorySetMapper.selectPlayIdList(sportId, matchId, 1 - matchType, userId.longValue());
                if (!CollectionUtils.isEmpty(playIds) && !playIds.containsAll(playIdListFengKong)) {
                    log.info("::{}::风控有玩法：" + JSON.toJSONString(playIds) + "有权限的玩法" + JSON.toJSONString(playIdListFengKong),CommonUtil.getRequestId());
                    return false;
                }
            }
            if (!CollectionUtils.isEmpty(playIdListQiTa)) {
                return verifyOthers(matchId, 1 - matchType, userId);
            }
            return true;
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean tradeJurisdictionByPlayIdList(List<Long> playIdList, StandardMatchInfo matchInfo) {
        try {
            Long matchId = matchInfo.getId();
            Long sportId = matchInfo.getSportId();
            // 0-滚球，1-早盘
            Integer matchType = RcsConstant.isLive(matchInfo.getMatchStatus()) ? 0 : 1;
            Integer userId = TradeUserUtils.getUserId();
            if (isPreTrade(matchType)) {
                return true;
            }
            if (verify(matchId, matchType, userId)) {
                return true;
            }
            List<Long> playIdListFengKong = new ArrayList<>();
            List<Long> playIdListQiTa = new ArrayList<>();
            List<Long> longs = rcsMarketCategorySetMapper.selectPlayIdListByPlaySet(sportId);
            if (CollectionUtils.isEmpty(longs)) {
                playIdListQiTa = longs;
            } else {
                for (Long id : playIdList) {
                    if (longs.contains(id)) {
                        playIdListFengKong.add(id);
                    } else {
                        playIdListQiTa.add(id);
                    }
                }
            }
            if (!CollectionUtils.isEmpty(playIdListFengKong)) {
                List<Long> playIds = rcsMarketCategorySetMapper.selectPlayIdList(sportId, matchId, 1 - matchType, userId.longValue());
                if (!CollectionUtils.isEmpty(playIds) && !playIds.containsAll(playIdListFengKong)) {
                    log.info("::{}::风控有玩法：" + JSON.toJSONString(playIds) + "有权限的玩法" + JSON.toJSONString(playIdListFengKong),CommonUtil.getRequestId());
                    return false;
                }
            }
            if (!CollectionUtils.isEmpty(playIdListQiTa)) {
                return verifyOthers(matchId, 1 - matchType, userId);
            }
            return true;
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean tradeJurisdictionByPlaySet(Long sportId, Long matchId, Long playSet, Integer matchType) {
        try {
            if (matchType == null) {
                StandardMatchInfo standardMatchInfo = standardMatchInfoMapper.selectById(matchId);
                if (matchType == null) {
                    if (standardMatchInfo.getMatchStatus() == 10 || standardMatchInfo.getMatchStatus() == 2 || standardMatchInfo.getMatchStatus() == 1) {
                        matchType = 0;
                    } else {
                        matchType = 1;
                    }
                }
                if (sportId == null) {
                    sportId = standardMatchInfo.getSportId();
                }
            }
            if (isPreTrade(matchType)) {
                return true;
            }
            Integer userId = TradeUserUtils.getUserId();
            if (verify(matchId, matchType, userId)) {
                return true;
            }
            List<Long> playSetList = rcsMarketCategorySetMapper.selectPlaySetList(sportId, matchId, 1 - matchType, userId.longValue());
            if (!CollectionUtils.isEmpty(playSetList)) {
                return playSetList.contains(playSet);
            }
            return false;
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean tradeJurisdictionByMarketId(Long marketId, Integer matchType) {
        try {
            StandardSportMarket standardSportMarket = standardSportMarketMapper.selectById(marketId);
            StandardMatchInfo standardMatchInfo = standardMatchInfoMapper.selectById(standardSportMarket.getStandardMatchInfoId());
            if (matchType == null) {
                if (standardMatchInfo.getMatchStatus() == 10 || standardMatchInfo.getMatchStatus() == 2 || standardMatchInfo.getMatchStatus() == 1) {
                    matchType = 0;
                } else {
                    matchType = 1;
                }
            }
            if (isPreTrade(matchType)) {
                return true;
            }
            Integer userId = TradeUserUtils.getUserId();
            if (verify(standardMatchInfo.getId(), matchType, userId)) {
                return true;
            }
            List<Long> longs = rcsMarketCategorySetMapper.selectPlayIdListByPlaySet(standardMatchInfo.getSportId());
            if (!CollectionUtils.isEmpty(longs) && longs.contains(standardSportMarket.getMarketCategoryId())) {
                List<Long> playIds = rcsMarketCategorySetMapper.selectPlayIdList(standardMatchInfo.getSportId(), standardMatchInfo.getId(), 1 - matchType, userId.longValue());
                if (!CollectionUtils.isEmpty(playIds)) {
                    return playIds.contains(standardSportMarket.getMarketCategoryId());
                }
                return false;
            } else {
                return verifyOthers(standardMatchInfo.getId(), 1 - matchType, userId);
            }
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean tradeJurisdictionByMatchId(Long matchId) {
        return true;
//        try {
//            RcsStandardSportMarketSell rcsStandardSportMarketSell = rcsStandardSportMarketSellMapper.selectRcsStandardSportMarketSellByMatchInfoId(matchId);
//            StandardMatchInfo standardMatchInfo = standardMatchInfoMapper.selectById(matchId);
//            Integer userId = TradeUserUtils.getUserId();
//            String tradeId;
//            if (standardMatchInfo.getMatchStatus() == 10 || standardMatchInfo.getMatchStatus() == 2 || standardMatchInfo.getMatchStatus() == 1) {
//                tradeId = rcsStandardSportMarketSell.getLiveTraderId();
//            } else {
//                tradeId = rcsStandardSportMarketSell.getPreTraderId();
//            }
//            if (userId .equals( tradeId)) {
//                return true;
//            }
//            return false;
//        }catch (Exception e){
//            log.error(e.getMessage(),e);
//            return false;
//        }
    }

    /**
     * @param matchId   验证其他类
     * @param matchType
     * @param userId
     * @return
     */
    private boolean verifyOthers(Long matchId, Integer matchType, Integer userId) {
        Map<String, Object> columnMap = new HashMap<>();
        columnMap.put("match_id", matchId);
        columnMap.put("match_type", matchType);
        columnMap.put("user_id", userId);
        columnMap.put("play_collection_id", -1);
        columnMap.put("status", 1);
        List<RcsTradingAssignment> rcsTradingAssignmentList = rcsTradingAssignmentMapper.selectByMap(columnMap);
        if (CollectionUtils.isEmpty(rcsTradingAssignmentList)) {
            return false;
        }
        return true;
    }

    /**
     * @Description: 是权限人直接返回true
     * @Param: [matchId, matchType, userId]
     * @return: boolean
     * @Author: KIMI
     * @Date: 2020/11/13
     */
    private boolean verify(Long matchId, Integer matchType, Integer userId) {
        RcsStandardSportMarketSell rcsStandardSportMarketSell = rcsStandardSportMarketSellMapper.selectRcsStandardSportMarketSellByMatchInfoId(matchId);
        if (matchType == 0) {
            return String.valueOf(userId).equals(rcsStandardSportMarketSell.getLiveTraderId());
        } else {
            return String.valueOf(userId).equals(rcsStandardSportMarketSell.getPreTraderId());
        }
    }

    /**
     * 早盘先去掉权限  0是滚球
     *
     * @param matchType
     * @return
     */
    private boolean isPreTrade(Integer matchType) {
        if (matchType != null && matchType == 0) {
            return false;
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public HttpResponse add(TradingAssignmentDataVo tradingAssignmentDataVo, String tradeId) {
        Long sportId = tradingAssignmentDataVo.getSportId();
        Long matchId = tradingAssignmentDataVo.getMatchId();
        Integer matchType = tradingAssignmentDataVo.getMatchType();
        String nameIds = tradingAssignmentDataVo.getNameIds();
        String[] split = nameIds.split(";");
        List<Integer> idList = new ArrayList<>();
        List<String> nameList = new ArrayList<>();
        for (String s : split) {
            SysTraderVO traderDataByName = getTraderDataByName(s);
            //log.info("::{}::获取到操盘生信息={}",CommonUtil.getRequestId(), JSON.toJSONString(traderDataByName));
            if (traderDataByName == null) {
                nameList.add(s);
            } else {
                idList.add(traderDataByName.getId());
            }
        }
        if (!CollectionUtils.isEmpty(nameList)) {
            StringBuilder s = new StringBuilder();
            for (String name : nameList) {
                s.append(name).append(";");
            }
            s.append("等账号不存在，请修正后确认指派");
            return HttpResponse.fail(s);
        }
        Integer num = 0;
        int traderId = 0;
        if (!CollectionUtils.isEmpty(idList)) {
            List<RcsMarketCategorySetVo> rcsMarketCategorySetVos = rcsMarketCategorySetMapper.selectRcsMarketCategorySet(sportId);
            if (CollectionUtils.isEmpty(rcsMarketCategorySetVos)) {
                log.info("::{}::玩法集为空，体育种类Id:" + sportId,CommonUtil.getRequestId());
                return HttpResponse.success();
            }
            List<RcsTradingAssignment> rcsTradingAssignmentList = new ArrayList<>();
            List<Integer> longs = rcsTradingAssignmentMapper.selectUserId(matchId, matchType);
            //负责人
            RcsStandardSportMarketSell rcsStandardSportMarketSell1 = rcsStandardSportMarketSellMapper.selectRcsStandardSportMarketSellByMatchInfoId(matchId);
            if (matchType == 0 && rcsStandardSportMarketSell1.getPreTraderId() != null && rcsStandardSportMarketSell1.getPreTraderId().length() > 0) {
                traderId = Integer.parseInt(rcsStandardSportMarketSell1.getPreTraderId());
                num = 1;
            } else if (matchType == 1 && rcsStandardSportMarketSell1.getLiveTraderId() != null && rcsStandardSportMarketSell1.getLiveTraderId().length() > 0) {
                traderId = Integer.parseInt(rcsStandardSportMarketSell1.getLiveTraderId());
                num = 1;
            }
            for (Integer userId : idList) {
                if (!CollectionUtils.isEmpty(longs)) {
                    if (longs.contains(userId) || userId == traderId) {
                        return HttpResponse.fail("有名字已经被指派，不需要重复指派，名字为:" + getTraderDataById(userId).getUserCode());
                    }
                }
                for (RcsMarketCategorySetVo rcsMarketCategorySetVo : rcsMarketCategorySetVos) {
                    RcsTradingAssignment rcsTradingAssignment = new RcsTradingAssignment();
                    rcsTradingAssignment.setMatchId(matchId);
                    rcsTradingAssignment.setMatchType(matchType);
                    rcsTradingAssignment.setUserId(String.valueOf(userId));
                    rcsTradingAssignment.setOptionUserId(String.valueOf(tradeId));
                    rcsTradingAssignment.setStatus(1);
                    rcsTradingAssignment.setPlayCollectionId(rcsMarketCategorySetVo.getId());
                    rcsTradingAssignmentList.add(rcsTradingAssignment);
                }
                RcsTradingAssignment rcsTradingAssignment1 = new RcsTradingAssignment();
                rcsTradingAssignment1.setMatchId(matchId);
                rcsTradingAssignment1.setMatchType(matchType);
                rcsTradingAssignment1.setUserId(String.valueOf(userId));
                rcsTradingAssignment1.setOptionUserId(String.valueOf(tradeId));
                rcsTradingAssignment1.setStatus(1);
                rcsTradingAssignment1.setPlayCollectionId(-1L);
                rcsTradingAssignmentList.add(rcsTradingAssignment1);
            }
            log.info("::{}::为赛事指定操盘手 入库数据={}", CommonUtil.getRequestId(), JSON.toJSONString(rcsTradingAssignmentList));
            saveBatch(rcsTradingAssignmentList);
        }
        //更新mogo指派数据
        num = num + rcsTradingAssignmentMapper.selectTradingAssignmentCount(matchId, matchType, traderId);
        matchService.updateTraderNums(matchId, num);
        return HttpResponse.success();
    }

    @Override
    public SysTraderVO getTraderDataById(Integer tradeId) {
        if (null == sysTraderVOIdCache.getIfPresent(String.valueOf(tradeId))) {
            initTraderData();
        }
        return sysTraderVOIdCache.getIfPresent(String.valueOf(tradeId));
    }

    @Override
    public ShortSysUserVO getShortSysUserById(Integer tradeId) {
        if (null == shortSysUserVOIdCache.getIfPresent(String.valueOf(tradeId))) {
            initShortSysUserVO();
        }
        ShortSysUserVO shortSysUserVO = shortSysUserVOIdCache.getIfPresent(String.valueOf(tradeId));
        if (shortSysUserVO == null) {
            shortSysUserVO = new ShortSysUserVO();
            shortSysUserVO.setUserCode("");
        }
        return shortSysUserVO;
    }


    @Override
    public SysTraderVO getTraderDataByName(String tradeName) {
        initTraderData();
        return sysTraderVONameCache.getIfPresent(tradeName);
    }

    private void initTraderData() {
        List<SysTraderVO> sysTraderVOS = new ArrayList<>();
        try {
            sysTraderVOS = systemUserOrgAuthApi.traderUser();
        } catch (Exception e) {
            List<RcsSysUser> list = rcsSysUserService.list();
            if (!CollectionUtils.isEmpty(list)) {
                for (RcsSysUser rcsSysUser : list) {
                    SysTraderVO sysTraderVO = new SysTraderVO();
                    sysTraderVO.setPositionId(rcsSysUser.getPositionId());
                    sysTraderVO.setOrgId(rcsSysUser.getOrgId());
                    sysTraderVO.setWorkCode(rcsSysUser.getWorkCode());
                    sysTraderVO.setUserCode(rcsSysUser.getUserCode());
                    sysTraderVO.setId(rcsSysUser.getId().intValue());
                    sysTraderVO.setLogicDelete(rcsSysUser.getLogicDelete());
                    sysTraderVO.setEnabled(rcsSysUser.getEnabled());
                    sysTraderVOS.add(sysTraderVO);
                }
            } else {
                throw new RcsServiceException("业务 ISystemUserOrgAuthApi traderUser() 报错");
            }
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        if (!CollectionUtils.isEmpty(sysTraderVOS)) {
            for (SysTraderVO sysTraderVO1 : sysTraderVOS) {
                sysTraderVOIdCache.put(String.valueOf(sysTraderVO1.getId()), sysTraderVO1);
                sysTraderVONameCache.put(sysTraderVO1.getUserCode(), sysTraderVO1);
            }
        }
    }

    private void initShortSysUserVO() {
        List<ShortSysUserVO> sysTraderVOS = new ArrayList<>();
        try {
            sysTraderVOS = systemUserOrgAuthApi.getShortSysUserList();
        } catch (Exception e) {
            List<RcsSysUser> list = rcsSysUserService.list();
            if (!CollectionUtils.isEmpty(list)) {
                for (RcsSysUser rcsSysUser : list) {
                    ShortSysUserVO shortSysUserVO = new ShortSysUserVO();
                    shortSysUserVO.setPositionId(rcsSysUser.getPositionId());
                    shortSysUserVO.setOrgId(rcsSysUser.getOrgId());
                    shortSysUserVO.setWorkCode(rcsSysUser.getWorkCode());
                    shortSysUserVO.setUserCode(rcsSysUser.getUserCode());
                    shortSysUserVO.setId(rcsSysUser.getId().intValue());
                    shortSysUserVO.setLogicDelete(1 == rcsSysUser.getLogicDelete());
                    shortSysUserVO.setEnabled(1 == rcsSysUser.getEnabled());
                    sysTraderVOS.add(shortSysUserVO);
                }
            } else {
                throw new RcsServiceException("业务 ISystemUserOrgAuthApi getShortSysUserList() 报错");
            }
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        if (!CollectionUtils.isEmpty(sysTraderVOS)) {
            for (ShortSysUserVO sysTraderVO1 : sysTraderVOS) {
                shortSysUserVOIdCache.put(String.valueOf(sysTraderVO1.getId()), sysTraderVO1);
                shortSysUserVONameCache.put(sysTraderVO1.getUserCode(), sysTraderVO1);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HttpResponse update(List<RcsTradingAssignment> rcsTradingAssignmentList, Integer userId) {
        List<String> userIdList = new ArrayList<>();
        List<RcsTradingAssignment> rcsTradingAssignmentList1 = new ArrayList<>();
        int num = 0;
        Integer tradeId = 0;
        if (!CollectionUtils.isEmpty(rcsTradingAssignmentList)) {
            RcsStandardSportMarketSell rcsStandardSportMarketSell = rcsStandardSportMarketSellMapper.selectRcsStandardSportMarketSellByMatchInfoId(rcsTradingAssignmentList.get(0).getMatchId());
            int matchType = rcsTradingAssignmentList.get(0).getMatchType();
            if (matchType == 0 && rcsStandardSportMarketSell.getPreTraderId() != null && rcsStandardSportMarketSell.getPreTraderId().length() > 0) {
                num = 1;
                tradeId = Integer.parseInt(rcsStandardSportMarketSell.getPreTraderId());
            } else if (matchType == 1 && rcsStandardSportMarketSell.getLiveTraderId() != null && rcsStandardSportMarketSell.getLiveTraderId().length() > 0) {
                num = 1;
                tradeId = Integer.parseInt(rcsStandardSportMarketSell.getLiveTraderId());
            }
            for (RcsTradingAssignment rcsTradingAssignment : rcsTradingAssignmentList) {
                if (rcsTradingAssignment.getUserId().equals(String.valueOf(tradeId))) {
                    continue;
                }
                userIdList.add(rcsTradingAssignment.getUserId());
                List<Long> playCollectionIdList = rcsTradingAssignment.getPlayCollectionIdList();
                if (!CollectionUtils.isEmpty(playCollectionIdList)) {
                    for (Long playId : playCollectionIdList) {
                        RcsTradingAssignment rcsTradingAssignment1 = new RcsTradingAssignment();
                        rcsTradingAssignment1.setMatchId(rcsTradingAssignment.getMatchId());
                        rcsTradingAssignment1.setMatchType(rcsTradingAssignment.getMatchType());
                        rcsTradingAssignment1.setUserId(rcsTradingAssignment.getUserId());
                        rcsTradingAssignment1.setPlayCollectionId(playId);
                        rcsTradingAssignment1.setStatus(1);
                        rcsTradingAssignment1.setOptionUserId(String.valueOf(userId));
                        rcsTradingAssignmentList1.add(rcsTradingAssignment1);
                    }
                }
            }
        }
        if (!CollectionUtils.isEmpty(userIdList)) {
            rcsTradingAssignmentMapper.deleteTradingAssignmentByUserIdList(rcsTradingAssignmentList.get(0).getMatchId(), rcsTradingAssignmentList.get(0).getMatchType(), userIdList);
        }
        saveBatch(rcsTradingAssignmentList1);
        num = num + rcsTradingAssignmentMapper.selectTradingAssignmentCount(rcsTradingAssignmentList.get(0).getMatchId(), rcsTradingAssignmentList.get(0).getMatchType(), tradeId);
        matchService.updateTraderNums(rcsTradingAssignmentList.get(0).getMatchId(), num);
        return HttpResponse.success();
    }


    @Override
    public void deleteByIdAndMatchId(Integer matchId, Integer matchType, Integer traderId) {
        rcsTradingAssignmentMapper.deleteByIdAndMatchId(matchId, matchType, traderId);
    }

    @Transactional
    @Override
    public HttpResponse changePersonLiable(ChangePersonLiableVo changePersonLiableVo, Integer userId, Integer appId) {
        Integer matchId = changePersonLiableVo.getMatchId();
        Integer sportId = changePersonLiableVo.getSportId();
        Integer matchType = changePersonLiableVo.getMatchType();
        Integer tradeId = changePersonLiableVo.getTradeId();
        UpdateOperateTraderDTO updateOperateTraderDTO = new UpdateOperateTraderDTO();
        updateOperateTraderDTO.setStandardMatchId(matchId.longValue());
        updateOperateTraderDTO.setUserId(userId);
        updateOperateTraderDTO.setSportId(sportId.longValue());
        SysTraderVO traderData = getTraderDataById(tradeId);
        changePersonLiableVo.setTraderName(traderData.getUserCode());
        updateOperateTraderDTO.setOperateTrader(traderData.getUserCode());
        updateOperateTraderDTO.setMarketType(matchType);
        updateOperateTraderDTO.setAppId(appId);
        Request<UpdateOperateTraderDTO> request = new Request<>();
        request.setData(updateOperateTraderDTO);
        String requestId = TradeVerificationService.getRequestIdStatic();
        request.setLinkId(requestId);
        request.setDataSourceTime(System.currentTimeMillis());
        request.setOperaterId(userId.longValue());
        request.setGlobalId(requestId);
        log.info("::{}::变更赛事负责人in:{}", requestId, JSONObject.toJSONString(request));
        //修改开售数据  融合到我们的库需要时间
        Response response = iMarketCategorySellApi.updateOperateTrader(request);
        log.info("::{}::变更赛事负责人out:{}", requestId, JSONObject.toJSONString(response));
        if (response.getCode() == ResultCode.SUCCESS.getCode()) {
            //更新mongo操盘手
//            sportMatchViewService.updateTrader(changePersonLiableVo);
//            deleteByIdAndMatchId(matchId, matchType, tradeId);
//            List<RcsMarketCategorySetVo> rcsMarketCategorySetVoList = rcsMarketCategorySetMapper.selectRcsMarketCategorySet(sportId.longValue());
//            rcsStandardSportMarketSellMapper.updateRcsStandardSportMarketSellTradeId(matchId,matchType,tradeId,traderData.getUserCode());
//            if (!CollectionUtils.isEmpty(rcsMarketCategorySetVoList)){
//                List<RcsTradingAssignment> rcsTradingAssignmentList=new ArrayList<>();
//                for (RcsMarketCategorySetVo rcsMarketCategorySetVo:rcsMarketCategorySetVoList){
//                    RcsTradingAssignment rcsTradingAssignment=new RcsTradingAssignment();
//                    rcsTradingAssignment.setMatchId(Long.valueOf(matchId));
//                    rcsTradingAssignment.setMatchType(matchType);
//                    rcsTradingAssignment.setUserId(charge);
//                    rcsTradingAssignment.setPlayCollectionId(rcsMarketCategorySetVo.getId());
//                    rcsTradingAssignment.setStatus(1);
//                    rcsTradingAssignment.setOptionUserId(String.valueOf(userId));
//                    rcsTradingAssignmentList.add(rcsTradingAssignment);
//                }
//                RcsTradingAssignment rcsTradingAssignment=new RcsTradingAssignment();
//                rcsTradingAssignment.setMatchId(Long.valueOf(matchId));
//                rcsTradingAssignment.setMatchType(matchType);
//                rcsTradingAssignment.setUserId(charge);
//                rcsTradingAssignment.setPlayCollectionId(-1L);
//                rcsTradingAssignment.setStatus(1);
//                rcsTradingAssignment.setOptionUserId(String.valueOf(userId));
//                rcsTradingAssignmentList.add(rcsTradingAssignment);
//                saveBatch(rcsTradingAssignmentList);
//            }
            return HttpResponse.success();
        } else {
            return HttpResponse.failToMsg("融合服务器故障");
        }
    }

    @Override
    public boolean hasTraderJurisdiction(RcsMatchMarketConfig config) {
        // 优化单33554，早盘不做操盘权限限制
        if ((!ObjectUtils.isEmpty(config) && !ObjectUtils.isEmpty(config.getMatchType())) && config.getMatchType() == 1) {
            return Boolean.TRUE;
        }
        try {
            Integer userId = TradeUserUtils.getUserId();
            RcsSysUser rcsSysUser = rcsSysUserService.getById(userId);
            if (!ObjectUtils.isEmpty(rcsSysUser) && StringUtils.isNotBlank(rcsSysUser.getRoles()) && rcsSysUser.getLogicDelete() == 0) {
                String[] role = rcsSysUser.getRoles().split(",");
                List<String> rs = Arrays.asList(role);
                if (rs.contains(TRADER_GENERAL)) {
                    return Boolean.TRUE;
                } else if (rs.contains(TRADER_MANAGER) || rs.contains(TRADER_SENIOR)) {           // 不是总监增加球种限制
                    Integer sportId = rcsShiftMapper.getShiftByUserId(userId);
                    if (!ObjectUtils.isEmpty(sportId)) {
                        if (sportId == 300 && config.getSportId() > 2) {
                            return Boolean.TRUE;
                        } else if (sportId != 300 && config.getSportId() == sportId.longValue()) {
                            return Boolean.TRUE;
                        }
                    }

                } else {
                    // 是操盘手
                    Integer plays = rcsCategorySetTraderWeightService.selectPlayIdBySetId(config, userId);
                    if (plays.intValue() > 0) {
                        return Boolean.TRUE;
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取不到用户");
        };
        return Boolean.FALSE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setWeights(List<TradingAssignmentVo> tradingAssignmentVos) {
        log.info("::{}::,setWeights0:{}", TradeVerificationService.getRequestIdStatic(),JSONObject.toJSONString(tradingAssignmentVos));
        Long matchId = tradingAssignmentVos.get(0).getMatchId();
        Long sportId = tradingAssignmentVos.get(0).getSportId();
        Integer marketType = tradingAssignmentVos.get(0).getMarketType();
        if (null == matchId || null == sportId||null==marketType) {
            throw new RcsServiceException("入参错误");
        }
        String traderId = "";
        QueryWrapper<RcsStandardSportMarketSell> sellWrapper = new QueryWrapper<>();
        sellWrapper.lambda().eq(RcsStandardSportMarketSell::getMatchInfoId, matchId);
        //得到开售信息
        RcsStandardSportMarketSell sellBean = rcsStandardSportMarketSellMapper.selectOne(sellWrapper);
        log.info("::{}::,setWeights1:{}", TradeVerificationService.getRequestIdStatic(),JSONObject.toJSONString(sellBean));
        if (marketType == 0) {
            traderId = sellBean.getLiveTraderId();
        } else if (marketType == 1) {
            traderId = sellBean.getPreTraderId();
        } else {
            throw new RcsServiceException("没设置盘口类型");
        }
        //得到玩法体集
        ArrayList<RcsCategorySetTraderWeight> list = new ArrayList<>();
        //封装新数据
        for (TradingAssignmentVo tradingAssignmentVo : tradingAssignmentVos) {
            List<TradingAssignmentSubPlayVo> sysTraderWeightSetList = tradingAssignmentVo.getSysTraderWeightList();
            for (TradingAssignmentSubPlayVo tradingAssignmentSubPlayVo : sysTraderWeightSetList) {
                List<RcsCategorySetTraderWeight> sysTraderWeightPlayList = tradingAssignmentSubPlayVo.getSysTraderWeightList();
                boolean masterTrader = false;
                int weight = 0;
                for (RcsCategorySetTraderWeight rcsCategorySetTraderWeight1 : sysTraderWeightPlayList) {
                    RcsCategorySetTraderWeight rcsCategorySetTraderWeight = new RcsCategorySetTraderWeight();
                    rcsCategorySetTraderWeight.setSetNo(tradingAssignmentVo.getSetNo());
                    rcsCategorySetTraderWeight.setMatchId(matchId);
                    rcsCategorySetTraderWeight.setTraderId(rcsCategorySetTraderWeight1.getTraderId());
                    rcsCategorySetTraderWeight.setWeight(rcsCategorySetTraderWeight1.getWeight());
                    rcsCategorySetTraderWeight.setTraderCode(rcsCategorySetTraderWeight1.getTraderCode());
                    rcsCategorySetTraderWeight.setMarketType(rcsCategorySetTraderWeight1.getMarketType());
                    rcsCategorySetTraderWeight.setSportId(sportId);
                    rcsCategorySetTraderWeight.setVersion(1);
                    rcsCategorySetTraderWeight.setTypeId(rcsCategorySetTraderWeight1.getTypeId());
                    list.add(rcsCategorySetTraderWeight);
                    weight = weight + rcsCategorySetTraderWeight1.getWeight();
                    if (traderId.equals(String.valueOf(rcsCategorySetTraderWeight1.getTraderId()))) {
                        masterTrader = true;
                    }
                }
                if (100 != weight) {
                    throw new RcsServiceException("权重合值不等于100");
                }
                if (!masterTrader) {
                    throw new RcsServiceException("没有设置主操盘手:" + traderId);
                }
            }
        }
        //得到新旧数据差集
        QueryWrapper<RcsCategorySetTraderWeight> rcsCategorySetTraderWeightQueryWrapper = new QueryWrapper<>();
        rcsCategorySetTraderWeightQueryWrapper.lambda().eq(RcsCategorySetTraderWeight::getMatchId, matchId);
        rcsCategorySetTraderWeightQueryWrapper.lambda().eq(RcsCategorySetTraderWeight::getMarketType, marketType);
        List<RcsCategorySetTraderWeight> rcsCategorySetTraderWeights = rcsCategorySetTraderWeightMapper.selectList(rcsCategorySetTraderWeightQueryWrapper);
        log.info("::{}::,setWeights2:{}", TradeVerificationService.getRequestIdStatic(),JSONObject.toJSONString(rcsCategorySetTraderWeights));
        List<RcsCategorySetTraderWeight> differenceSet = getDifferenceSet(rcsCategorySetTraderWeights, list);
        ArrayList<Object> delIds = new ArrayList<>();
        for (RcsCategorySetTraderWeight rcsCategorySetTraderWeight : differenceSet) {
            delIds.add(rcsCategorySetTraderWeight.getId());
        }
        //插入权重
        rcsCategorySetTraderWeightMapper.batchInsertOrUpdate(list);
        //删除差集权重  保留新数据  删除旧数据
        if (CollectionUtil.isNotEmpty(delIds)) {
            rcsCategorySetTraderWeightMapper.deleteBatchIds(delIds);
        }
        //设置操盘人数
        Map<Long, List<RcsCategorySetTraderWeight>> collect = list.stream().collect(Collectors.groupingBy(RcsCategorySetTraderWeight::getTraderId));
        StandardMatchInfo standardMatchInfo = standardMatchInfoMapper.selectById(matchId);
        Update update = new Update();
        if ((1 == marketType.intValue() && 0 == standardMatchInfo.getMatchStatus())
                || (0 == marketType.intValue() && 1 == standardMatchInfo.getMatchStatus())) {
            update.set("traderNum", collect.size());
            mongotemplate.updateFirst(new Query().addCriteria(Criteria.where("matchId").is(matchId)), update, MatchMarketLiveBean.class);
        }
        //发送到融合
        List<Map<String, Object>> traderList = list.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(o -> String.valueOf(o.getTraderId())))), ArrayList::new))
                .stream().distinct().map(e -> {
                    Map<String, Object> map = new HashMap();
                    map.put("traderId", e.getTraderId());
                    map.put("traderCode", e.getTraderCode());
                    return map;
                }).collect(Collectors.toList());
        Request<HashMap> sendRequest = new Request<>();
        sendRequest.setLinkId(CommonUtil.getRequestId());
        HashMap<String, Object> map = new HashMap<>();
        map.put("sportId", sportId);
        map.put("matchId", matchId);
        map.put("marketType", marketType);
        map.put("trader", traderList);
        sendRequest.setData(map);
        sendMessage.sendMessage("RCS_TRADE_NUM", null, CommonUtil.getRequestId(), sendRequest);
    }

    /**
     * 得到差集
     *
     * @param aSet
     * @param bSet
     * @return
     */
    private List<RcsCategorySetTraderWeight> getDifferenceSet(List<RcsCategorySetTraderWeight> aSet, List<RcsCategorySetTraderWeight> bSet) {
        List<RcsCategorySetTraderWeight> dSet = aSet.stream()
                .filter(notComment -> !bSet.stream().map(all -> all.getTraderId() + "_" + all.getTypeId() + "_" + all.getMatchId() + "_" + all.getMarketType()).collect(Collectors.toList()).
                        contains(notComment.getTraderId() + "_" + notComment.getTypeId() + "_" + notComment.getMatchId() + "_" + notComment.getMarketType())).collect(Collectors.toList());
        return dSet;
    }
}
