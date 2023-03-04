package com.panda.rcs.warning.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.panda.rcs.warning.mapper.*;
import com.panda.rcs.warning.service.MatchOperateExceptionMonitorApi;
import com.panda.rcs.warning.utils.TradeUserUtils;
import com.panda.rcs.warning.vo.*;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.utils.i18n.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author :  koala
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.rcs.warning.service.impl
 * @Description :  监控服务类
 * @Date: 2022-07-19 14:50
 * --------  ---------  --------------------------
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MatchOperateExceptionMonitorApiImpl implements MatchOperateExceptionMonitorApi {
    private final MatchOperateExceptionMonitorMapper matchOperateExceptionMonitorMapper;
    private final RcsMatchMonitorMqLicenseMapper rcsMatchMonitorMqLicenseMapper;
    private final RcsMatchMonitorSettingMapper rcsMatchMonitorSettingMapper;
    private final RcsMatchMonitorListMapper rcsMatchMonitorListMapper;
    private final StandardSportMarketCategoryRefMapper standardSportMarketCategoryRefMapper;
    private final RedisClient redisClient;
    private final RcsLanguageInternationMapper rcsLanguageInternationMapper;
    private final StandardMatchInfoMapper standardMatchInfoMapper;
    private final RcsMatchMonitorErrorLogMapper rcsMatchMonitorErrorLogMapper;

    /**
     * @param type  1早盘 0滚球
     * @param level level 3: <2小时 2:>2小时<18 1:大于18小时
     */
    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    public void rollTheBallApproach(Integer type, Integer level) {

        //查询所有滚球赛事 1早盘 0滚球
        List<RollBallMatchInfo> rollBallMatchInfoList = matchOperateExceptionMonitorMapper.queryRollBallMatchInfo(type, level);
        ExecutorService executorService = Executors.newCachedThreadPool();
        Map<Long, List<RollBallMatchInfo>> matchMap = rollBallMatchInfoList.stream().collect(Collectors.groupingBy(RollBallMatchInfo::getMatchId));
        log.info("类型:{},级别:{},数据长度:{},查询出监控数据:{}", type, level, matchMap.size(), JSON.toJSONString(matchMap));
        for (Map.Entry<Long, List<RollBallMatchInfo>> entry : matchMap.entrySet()) {
            List<RollBallMatchInfo> list = entry.getValue();
            executorService.submit(() -> {
                for (RollBallMatchInfo rollBallMatchInfo : list) {
                    Long matchId = rollBallMatchInfo.getMatchId();
                    int playId = rollBallMatchInfo.getPlayId();
                    int sportId = rollBallMatchInfo.getSportId();
                    int matchType = rollBallMatchInfo.getMatchType();
                    //查询入库监控信息
                    RcsMatchMonitorMqLicense monitorExBean = rcsMatchMonitorMqLicenseMapper.selectOne(new LambdaQueryWrapper<RcsMatchMonitorMqLicense>().eq(RcsMatchMonitorMqLicense::getMatchId, matchId).eq(RcsMatchMonitorMqLicense::getPlayId, playId).eq(RcsMatchMonitorMqLicense::getMatchType, type));
                    //如果没有查询到监控信息就下次
                    if (Objects.isNull(monitorExBean)) continue;
                    //查询赛事球队信息
                    MatchOperateExListVo operateExListVo = matchOperateExceptionMonitorMapper.queryMatchByTimerAndMatchStatus(matchId);
                    //log.info("::赛事{}::玩法:{},断链信息:{}", matchId, playId, JSON.toJSONString(monitorExBean));
                    LambdaQueryWrapper<RcsMatchMonitorSetting> wrapper = new LambdaQueryWrapper<>();
                    //检查监控配置信息
                    List<RcsMatchMonitorSetting> rcsMatchMonitorSettingList = null;
                    //早盘
                    if (type == 1) {
                        long riskTime = (rollBallMatchInfo.getTimer() - System.currentTimeMillis()) / 1000;
                        wrapper.eq(RcsMatchMonitorSetting::getMatchType, type).lt(RcsMatchMonitorSetting::getRiskTime, riskTime).orderByDesc(RcsMatchMonitorSetting::getRiskTime).last("LIMIT 2");
                        rcsMatchMonitorSettingList = rcsMatchMonitorSettingMapper.selectList(wrapper);
                        //滚球
                    } else if (type == 0) {
                        wrapper.eq(RcsMatchMonitorSetting::getRiskKey, rollBallMatchInfo.getTournamentLevel())
                                .eq(RcsMatchMonitorSetting::getMatchType, type);
                        rcsMatchMonitorSettingList = rcsMatchMonitorSettingMapper.selectList(wrapper);
                    }
                    //log.info("::赛事{}::玩法:{},获取到监控配置::{}", matchId, playId, JSON.toJSONString(rcsMatchMonitorSettingList));
                    if (Objects.isNull(rcsMatchMonitorSettingList) || rcsMatchMonitorSettingList.isEmpty()) {
                        log.info("::赛事ID::{},没有找到模板配置", matchId);
                        continue;
                    }
                    //循环高危或者中危配置
                    long remainingSeconds = (System.currentTimeMillis() - monitorExBean.getUpdateTime()) / 1000;
                    long highSeconds = 0L;
                    long middleSeconds = 0L;
                    for (RcsMatchMonitorSetting rcsMatchMonitorSetting : rcsMatchMonitorSettingList) {
                        if (rcsMatchMonitorSetting.getRiskLevel() == 1) {
                            highSeconds = rcsMatchMonitorSetting.getRiskValue();
                        }
                        //判断是否高危
                        if (rcsMatchMonitorSetting.getRiskLevel() == 2) {
                            middleSeconds = rcsMatchMonitorSetting.getRiskValue();
                        }
                    }
                    //log.info("::赛事{}::玩法:{},当前时间:{},配置高危秒数:{},低危秒数:{}", matchId, playId, remainingSeconds, highSeconds, middleSeconds);
                    int risType = 3;
                    if (remainingSeconds >= highSeconds) {
                        risType = 1;
                    } else if (remainingSeconds >= middleSeconds) {
                        risType = 2;
                    }
                    String period = getMatchPeriod(matchId);
                    //足球玩法处理 上半场 ID 17 18 19 进入赛事阶段 0 6 正常监控
                    if (sportId == 1 && Arrays.asList(17, 18, 19).contains(playId) && !Arrays.asList(0, 6).contains(Integer.parseInt(period))) {
                        risType = 3;
                    }
                    //全场玩法 ID 1 2 4 进入赛事阶段 0 6 7 正常监控
                    if (sportId == 1 && Arrays.asList(1, 2, 4).contains(playId) && !Arrays.asList(0, 6, 7).contains(Integer.parseInt(period))) {
                        risType = 3;
                    }
                    //篮球 全场玩法ID 37 39 38 40 进入赛事阶段 0 1 2 13 14 15 16 正常监控
                    if (sportId == 2 && Arrays.asList(37, 39, 38, 40).contains(playId) && !Arrays.asList(0, 1, 2, 13, 14, 15, 16).contains(Integer.parseInt(period))) {
                        risType = 3;
                    }
                    //上半场 ID 43 19 18 42 进入赛事阶段 0 1 13 14 正常监控
                    if (sportId == 2 && Arrays.asList(43, 19, 18, 42).contains(playId) && !Arrays.asList(0, 1, 13, 14).contains(Integer.parseInt(period))) {
                        risType = 3;
                    }
                    //log.info("::赛事{}::玩法:{},监控等级:{}", matchId, playId, risType);
                    LambdaQueryWrapper<RcsMatchMonitorList> listLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    listLambdaQueryWrapper.eq(RcsMatchMonitorList::getMatchId, rollBallMatchInfo.getMatchId())
                            .eq(RcsMatchMonitorList::getSportId, rollBallMatchInfo.getSportId())
                            .eq(RcsMatchMonitorList::getMatchType, matchType)
                            .eq(RcsMatchMonitorList::getPlayId, rollBallMatchInfo.getPlayId());
                    RcsMatchMonitorList rcsMatchMonitorList = rcsMatchMonitorListMapper.selectOne(listLambdaQueryWrapper);
                    //log.info("::赛事{}::玩法:{},查询出异常监控信息::{}", matchId, playId, JSON.toJSONString(rcsMatchMonitorList));
                    //查询国际化信息
                    LambdaQueryWrapper<StandardSportMarketCategoryRef> playQuery = new LambdaQueryWrapper<>();
                    playQuery.eq(StandardSportMarketCategoryRef::getCategoryId, rollBallMatchInfo.getPlayId())
                            .eq(StandardSportMarketCategoryRef::getSportId, rollBallMatchInfo.getSportId());
                    StandardSportMarketCategoryRef standardSportMarketCategoryRef = standardSportMarketCategoryRefMapper.selectOne(playQuery);
                    if (Objects.nonNull(rcsMatchMonitorList)) {
                        log.info("::赛事{}::玩法:{},开始修改监控等级::{}", matchId, playId, risType);
                        RcsMatchMonitorList copyRcsMatchMonitorList = new RcsMatchMonitorList();
                        copyRcsMatchMonitorList.setId(rcsMatchMonitorList.getId());
                        copyRcsMatchMonitorList.setLevelsDanger(risType);
                        copyRcsMatchMonitorList.setMatchType(matchType);
                        copyRcsMatchMonitorList.setEventTime(monitorExBean.getUpdateTime());
                        //log.info("::赛事{}::玩法:{},要修改的异常监控信息::{}", matchId, playId, JSON.toJSONString(copyRcsMatchMonitorList));
                        int num = rcsMatchMonitorListMapper.updateById(copyRcsMatchMonitorList);
                        log.info("::赛事{}::玩法:{},修改的异常监控信息返回成功条数::{}", matchId, playId, num);
                    } else {
                        if (risType != 3) {
                            RcsMatchMonitorList insertRcsMatchMonitorList = new RcsMatchMonitorList();
                            insertRcsMatchMonitorList.setPlayIdCode(standardSportMarketCategoryRef.getNameCode());
                            insertRcsMatchMonitorList.setLevelsDanger(risType);
                            insertRcsMatchMonitorList.setMatchType(matchType);
                            insertRcsMatchMonitorList.setBeginTime(rollBallMatchInfo.getTimer());
                            insertRcsMatchMonitorList.setDataType(rcsMatchMonitorSettingList.get(0).getDataType());
                            insertRcsMatchMonitorList.setEventTime(monitorExBean.getEventTime());
                            insertRcsMatchMonitorList.setMatchManageId(String.valueOf(rollBallMatchInfo.getMatchManageId()));
                            insertRcsMatchMonitorList.setMatchId(rollBallMatchInfo.getMatchId());
                            insertRcsMatchMonitorList.setPlayId(rollBallMatchInfo.getPlayId());
                            insertRcsMatchMonitorList.setSportId(rollBallMatchInfo.getSportId());
                            insertRcsMatchMonitorList.setTournamentLevel(rollBallMatchInfo.getTournamentLevel());
                            insertRcsMatchMonitorList.setStandardTournamentId(rollBallMatchInfo.getStandardTournamentId());
                            insertRcsMatchMonitorList.setTeamNameCode(operateExListVo.getTeamNameCode());
                            insertRcsMatchMonitorList.setTourNameCode(operateExListVo.getTourNameCode());
                            int num = rcsMatchMonitorListMapper.insert(insertRcsMatchMonitorList);
                            log.info("::赛事{}::玩法:{},新增异常监控信息返回成功条数::{}", matchId, playId, num);

                        }
                    }
                    //有写入监控并且恢复断链
                    if (Objects.nonNull(rcsMatchMonitorList) && risType == 3) {
                        insertErrorLog(rollBallMatchInfo, standardSportMarketCategoryRef.getNameCode(), operateExListVo, monitorExBean);
                    }
                }
            });
        }
        executorService.shutdown();
    }

    @Override
    public void insertErrorLog(RollBallMatchInfo rollBallMatchInfo, Long playCode, MatchOperateExListVo operateExListVo, RcsMatchMonitorMqLicense monitorExBean) {
        RcsMatchMonitorErrorLog rcsMatchMonitorErrorLog = new RcsMatchMonitorErrorLog();
        rcsMatchMonitorErrorLog.setMatchId(rollBallMatchInfo.getMatchId());
        rcsMatchMonitorErrorLog.setPlayId(rollBallMatchInfo.getPlayId());
        rcsMatchMonitorErrorLog.setMatchManageId(String.valueOf(rollBallMatchInfo.getMatchManageId()));
        rcsMatchMonitorErrorLog.setPlayCode(playCode);
        String[] teamCodeArr = operateExListVo.getTeamNameCode().split(",");
        rcsMatchMonitorErrorLog.setHomeTeamCode(Long.valueOf(teamCodeArr[0]));
        rcsMatchMonitorErrorLog.setAwayTeamCode(Long.valueOf(teamCodeArr[1]));
        rcsMatchMonitorErrorLog.setTourCode(operateExListVo.getTourNameCode());
        rcsMatchMonitorErrorLog.setEventTime(monitorExBean.getEventTime());
        rcsMatchMonitorErrorLog.setRecoverTime(monitorExBean.getUpdateTime());
        rcsMatchMonitorErrorLog.setCreateTime(new Date());
        rcsMatchMonitorErrorLogMapper.insert(rcsMatchMonitorErrorLog);
    }

    @Override
    public Map<Integer, Object> queryRcsMatchMonitorSetting() {
        try {
            List<RcsMatchMonitorSetting> rcsMatchMonitorSettingList = rcsMatchMonitorSettingMapper.selectList(new QueryWrapper<>());
            Map<Integer, Object> returnMap = new HashMap<>();
            //早盘
            List<RcsMatchMonitorSetting> earlyList = rcsMatchMonitorSettingList.stream().filter(s -> s.getMatchType() == 1).collect(Collectors.toList());
            //滚球
            List<RcsMatchMonitorSetting> rollList = rcsMatchMonitorSettingList.stream().filter(s -> s.getMatchType() == 0).collect(Collectors.toList());
            returnMap.put(1, getChangeData(earlyList));
            returnMap.put(0, getChangeData(rollList));
            return returnMap;
        } catch (Exception e) {
            log.error("::{}::查询风控监控异常{}", CommonUtils.getLinkId(),e.getMessage(),e);
        }
        return null;
    }

    private Map<Integer, List<RcsMatchMonitorSetting>> getChangeData(List<RcsMatchMonitorSetting> list) {
        Map<Integer, List<RcsMatchMonitorSetting>> tempMap = new HashMap<>();
        tempMap.put(1, list.stream().filter(s -> s.getRiskLevel() == 1).collect(Collectors.toList()));
        tempMap.put(2, list.stream().filter(s -> s.getRiskLevel() == 2).collect(Collectors.toList()));
        return tempMap;
    }

    @Override
    public void updateRcsMatchMonitorSetting(RcsMatchMonitorSettingUpdate update) {
        try {
            if (Objects.isNull(update)) {
                throw new RcsServiceException("参数不能为空");
            }
            for (RcsMatchMonitorSetting rcsMatchMonitorSetting : update.getList()) {
                rcsMatchMonitorSettingMapper.updateById(rcsMatchMonitorSetting);
            }
        } catch (Exception e) {
            log.error("::{}::监控异常保存配置出问题{}", CommonUtils.getLinkId(),e.getMessage(),e);
        }

    }

    /**
     * 查询异常监控列表
     *
     * @param matchOperateListQuery 查询参数
     * @return 返回列表信息
     */

    @Override
    public IPage<RcsMatchMonitorList> selectMatchOperateList(MatchOperateListQuery matchOperateListQuery) {
        try {
            Integer chooseType = matchOperateListQuery.getChooseType();
            List<Long> matchList = null;
            if (ObjectUtils.isNotEmpty(chooseType)) {
                Integer userId = TradeUserUtils.getUserId();
                //仅我收藏
                if (chooseType == 3) {
                    matchList = matchOperateExceptionMonitorMapper.queryCollectMatch(userId);
                }
                //仅我操盘
                if (chooseType == 2) {
                    matchList = matchOperateExceptionMonitorMapper.queryTraderMatch(userId);
                }
            }
            List<Integer> monitorLevels = Objects.nonNull(matchOperateListQuery.getMonitorLevels()) ? matchOperateListQuery.getMonitorLevels() : Arrays.asList(1, 2);
            LambdaQueryWrapper<RcsMatchMonitorList> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Objects.nonNull(matchOperateListQuery.getMatchManageId()), RcsMatchMonitorList::getMatchManageId, matchOperateListQuery.getMatchManageId())
                    .in(Objects.nonNull(matchOperateListQuery.getSportIds()), RcsMatchMonitorList::getSportId, matchOperateListQuery.getSportIds())
                    .in(RcsMatchMonitorList::getLevelsDanger, monitorLevels)
                    .in(Objects.nonNull(matchOperateListQuery.getTournamentLevels()), RcsMatchMonitorList::getTournamentLevel, matchOperateListQuery.getTournamentLevels())
                    .in(Objects.nonNull(matchOperateListQuery.getMatchStatues()), RcsMatchMonitorList::getMatchType, matchOperateListQuery.getMatchStatues())
                    .in(Objects.nonNull(matchList), RcsMatchMonitorList::getMatchId, matchList).orderByAsc(RcsMatchMonitorList::getLevelsDanger).orderByAsc(RcsMatchMonitorList::getEventTime).orderByAsc(RcsMatchMonitorList::getBeginTime);
            Page<RcsMatchMonitorList> page = new Page<>(matchOperateListQuery.getCurrentPage(), matchOperateListQuery.getPageSize());
            Long pageStart = System.currentTimeMillis();
            //log.info("::监控异常监控列表分页查询前:{}", pageStart);
            IPage<RcsMatchMonitorList> dataPage = rcsMatchMonitorListMapper.selectPage(page, queryWrapper);
            Long pageEnd = System.currentTimeMillis() - pageStart;
            //log.info("::监控异常监控列表分页查询后:{}", pageStart - pageEnd);
            List<Long> nameCodeList = new ArrayList<>();
            List<Long> playNameCodeList = new ArrayList<>();
            List<Long> queryMatchList = dataPage.getRecords().stream().map(RcsMatchMonitorList::getMatchId).collect(Collectors.toList());
            for (RcsMatchMonitorList rcsMatchMonitorList : dataPage.getRecords()) {
                String[] arr = rcsMatchMonitorList.getTeamNameCode().split(",");
                nameCodeList.add(rcsMatchMonitorList.getTourNameCode());
                nameCodeList.add(Long.valueOf(arr[0]));
                nameCodeList.add(Long.valueOf(arr[1]));
                playNameCodeList.add(rcsMatchMonitorList.getPlayIdCode());
            }
            Map<Long, List<RcsLanguageInternation>> playMap = null;
            Map<Long, List<LanguageInternation>> tempMap = null;
            if (!playNameCodeList.isEmpty()) {
                playMap = rcsLanguageInternationMapper.selectList(new LambdaQueryWrapper<RcsLanguageInternation>().in(RcsLanguageInternation::getNameCode, playNameCodeList)).stream().collect(Collectors.groupingBy(RcsLanguageInternation::getNameCode));
            }
            if (!nameCodeList.isEmpty()) {
                tempMap = rcsLanguageInternationMapper.selectByLanguageTypeAndNameCodes(matchOperateListQuery.getLang(), nameCodeList).stream().collect(Collectors.groupingBy(LanguageInternation::getNameCode));
            }
            Map<Long, List<StandardMatchInfo>> standardMatchInfoMap = null;
            if (!queryMatchList.isEmpty()) {
                standardMatchInfoMap = standardMatchInfoMapper.selectList(new LambdaQueryWrapper<StandardMatchInfo>().in(StandardMatchInfo::getId, queryMatchList)).stream().collect(Collectors.groupingBy(StandardMatchInfo::getId));
            }
            Long end = System.currentTimeMillis() - pageEnd;
            //log.info("::监控异常监控列表分页查询查询国际化:{}", end - pageEnd);

            for (RcsMatchMonitorList rcsMatchMonitorListItem : dataPage.getRecords()) {
                String[] arr = rcsMatchMonitorListItem.getTeamNameCode().split(",");
                Map<String, String> itemMap = JSON.parseObject(playMap.get(rcsMatchMonitorListItem.getPlayIdCode()).get(0).getText(), HashMap.class);
                rcsMatchMonitorListItem.setPlayName(Objects.isNull(itemMap.get(matchOperateListQuery.getLang())) ? "" : itemMap.get(matchOperateListQuery.getLang()));
                rcsMatchMonitorListItem.setTourName(Objects.isNull(tempMap.get(rcsMatchMonitorListItem.getTourNameCode())) ? "" : tempMap.get(rcsMatchMonitorListItem.getTourNameCode()).get(0).getText());
                String homeTeamName = Objects.isNull(tempMap.get(Long.valueOf(arr[0]))) ? "" : tempMap.get(Long.valueOf(arr[0])).get(0).getText();
                String awayTeamName = Objects.isNull(tempMap.get(Long.valueOf(arr[1]))) ? "" : tempMap.get(Long.valueOf(arr[1])).get(0).getText();
                rcsMatchMonitorListItem.setTeamName(homeTeamName + "," + awayTeamName);
                rcsMatchMonitorListItem.setOperateMatchStatus(Objects.isNull(standardMatchInfoMap.get(rcsMatchMonitorListItem.getMatchId())) ? 1 : standardMatchInfoMap.get(rcsMatchMonitorListItem.getMatchId()).get(0).getOperateMatchStatus());
                rcsMatchMonitorListItem.setTimer((pageStart - rcsMatchMonitorListItem.getEventTime()) / 1000);
            }
            //log.info("::监控异常监控列表分页查询处理国际化后:{}", System.currentTimeMillis() - end);
            return dataPage;
        } catch (Exception e) {
            log.error("::{}::查询监控列表异常{}", CommonUtils.getLinkId(),e.getMessage(),e);
        }
        return null;
    }

    @Override
    public List<Long> queryMatchList(Integer type) {
        return matchOperateExceptionMonitorMapper.queryMatchList(type);
    }

    @Override
    public IPage<RcsMatchMonitorErrorLog> queryErrorLogInfo(PageQuery pageQuery, String lang) {
        Page<RcsMatchMonitorErrorLog> page = new Page<>(pageQuery.getCurrentPage(), pageQuery.getPageSize());
        IPage<RcsMatchMonitorErrorLog> dataPage = rcsMatchMonitorErrorLogMapper.selectPage(page, new LambdaQueryWrapper<RcsMatchMonitorErrorLog>().groupBy(RcsMatchMonitorErrorLog::getRecoverTime));
        List<Long> nameCodeList = new ArrayList<>();
        List<Long> playNameCodeList = new ArrayList<>();
        for (RcsMatchMonitorErrorLog rcsMatchMonitorErrorLog : dataPage.getRecords()) {
            nameCodeList.add(rcsMatchMonitorErrorLog.getTourCode());
            nameCodeList.add(rcsMatchMonitorErrorLog.getHomeTeamCode());
            nameCodeList.add(rcsMatchMonitorErrorLog.getAwayTeamCode());
            playNameCodeList.add(rcsMatchMonitorErrorLog.getPlayCode());
        }
        Map<Long, List<LanguageInternation>> tempMap = null;
        Map<Long, List<RcsLanguageInternation>> playMap = null;
        //处理国际化语言
        if (!playNameCodeList.isEmpty()) {
            playMap = rcsLanguageInternationMapper.selectList(new LambdaQueryWrapper<RcsLanguageInternation>().in(RcsLanguageInternation::getNameCode, playNameCodeList)).stream().collect(Collectors.groupingBy(RcsLanguageInternation::getNameCode));
        }
        if (!nameCodeList.isEmpty()) {
            tempMap = rcsLanguageInternationMapper.selectByLanguageTypeAndNameCodes(lang, nameCodeList).stream().collect(Collectors.groupingBy(LanguageInternation::getNameCode));
        }
        for (RcsMatchMonitorErrorLog rcsMatchMonitorErrorLog : dataPage.getRecords()) {
            Map<String, String> itemMap = JSON.parseObject(playMap.get(rcsMatchMonitorErrorLog.getPlayCode()).get(0).getText(), HashMap.class);
            rcsMatchMonitorErrorLog.setPlayName(Objects.isNull(itemMap.get(lang)) ? "" : itemMap.get(lang));
            rcsMatchMonitorErrorLog.setTourName(Objects.isNull(tempMap.get(rcsMatchMonitorErrorLog.getTourCode())) ? "" : tempMap.get(rcsMatchMonitorErrorLog.getTourCode()).get(0).getText());
            rcsMatchMonitorErrorLog.setHomeTeamName(Objects.isNull(tempMap.get(rcsMatchMonitorErrorLog.getHomeTeamCode())) ? "" : tempMap.get(rcsMatchMonitorErrorLog.getHomeTeamCode()).get(0).getText());
            rcsMatchMonitorErrorLog.setAwayTeamName(Objects.isNull(tempMap.get(rcsMatchMonitorErrorLog.getAwayTeamCode())) ? "" : tempMap.get(rcsMatchMonitorErrorLog.getAwayTeamCode()).get(0).getText());
        }
        return dataPage;
    }


    private String getMatchPeriod(Long matchId) {
        String periodRediskey = String.format("rcs:data:keyCache:matchTempInfo:%s", matchId);
        String period = redisClient.hGet(periodRediskey, "period");
        log.info("::异常监控获取赛事阶段,赛事id:{},periodRediskey:{},value={}", matchId, periodRediskey, period);
        return period;
    }
}
