package com.panda.sport.rcs.trade.wrapper.impl;

import com.alibaba.nacos.common.util.UuidUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.panda.merge.common.utils.TimeUtils;
import com.panda.merge.dto.Request;
import com.panda.sport.rcs.mapper.RcsMatchTradeMemoMapper;
import com.panda.sport.rcs.mongo.MatchMarketLiveBean;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsMatchTradeMemo;
import com.panda.sport.rcs.pojo.RcsMatchUserMemoRef;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.dto.MatchTradeMemoRemindDTO;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.MatchTradeMemoDetailVo;
import com.panda.sport.rcs.trade.wrapper.RcsMatchTradeMemoService;
import com.panda.sport.rcs.trade.wrapper.RcsMatchUserMemoRefService;
import com.panda.sport.rcs.trade.wrapper.StandardMatchInfoService;
import com.panda.sport.rcs.vo.I18nItemVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @ClassName RcsMatchUserMemoRefServiceImpl
 * @Description: TODO
 * @Author riben
 * @Date 2021/2/3
 **/
@Service
@Slf4j
public class RcsMatchTradeMemoServiceImpl extends ServiceImpl<RcsMatchTradeMemoMapper, RcsMatchTradeMemo> implements RcsMatchTradeMemoService {

    @Autowired
    private StandardMatchInfoService standardMatchInfoService;

    @Autowired
    private RcsMatchUserMemoRefService  matchUserMemoRefService;

    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;

    @Autowired
    MongoTemplate mongotemplate;

    public IPage<RcsMatchTradeMemo> getMemoPage(RcsMatchTradeMemo matchTradeMemo, Boolean byCondition){
        if(matchTradeMemo.getStandardMatchId() == null){
            log.error("标准赛事Id不能为空");
            return null;
        }
        //matchTradeMemo作为查询条件，将操盘手ID置空防止误传条件影响查询结果
        if(!byCondition){
            matchTradeMemo.setTraderId(null);
            matchTradeMemo.setTraderName(null);
        }
        QueryWrapper<RcsMatchTradeMemo> query = new QueryWrapper();
        query.lambda().eq(RcsMatchTradeMemo :: getStandardMatchId, matchTradeMemo.getStandardMatchId()).orderByDesc(RcsMatchTradeMemo :: getRecordTime);
        if(matchTradeMemo.getOperateStage() != null){
            query.lambda().eq(RcsMatchTradeMemo :: getOperateStage, matchTradeMemo.getOperateStage());
        }
        if(matchTradeMemo.getTraderId() != null){
            query.lambda().eq(RcsMatchTradeMemo :: getTraderId, matchTradeMemo.getTraderId());
        }
        if(matchTradeMemo.getTraderName() != null){
            query.lambda().like(RcsMatchTradeMemo :: getTraderName, matchTradeMemo.getTraderName());
        }
        if(matchTradeMemo.getText() != null){
            query.lambda().like(RcsMatchTradeMemo :: getText, matchTradeMemo.getText());
        }
        if(matchTradeMemo.getStartTime() != null){
            query.lambda().gt(RcsMatchTradeMemo :: getRecordTime, matchTradeMemo.getStartTime());
        }
        if(matchTradeMemo.getEndTime() != null){
            query.lambda().lt(RcsMatchTradeMemo :: getRecordTime, matchTradeMemo.getEndTime());
        }
        IPage<RcsMatchTradeMemo> memoPage = new Page(matchTradeMemo.getPageNo(),matchTradeMemo.getPageSize());
        memoPage = baseMapper.selectPage(memoPage, query);
        return memoPage;
    }

    @Transactional
    public MatchTradeMemoDetailVo getMemoDetail(RcsMatchTradeMemo matchTradeMemo){
        String memoId = matchTradeMemo.getId();
        String traderId = matchTradeMemo.getTraderId();
        String traderName = matchTradeMemo.getTraderName();
        Long standardMatchId = matchTradeMemo.getStandardMatchId();
        //备忘录ID及标准赛事ID不能同时为空时抛出异常
        if(StringUtils.isBlank(memoId) && standardMatchId == null){
            throw new RuntimeException("查询备忘录明细，备忘录ID及标准赛事ID不能同时为空！");
        }
        //若页面未传备忘录Id,比如点击小喇叭图标查询时，获取
        RcsMatchTradeMemo memoInfo = null;
        IPage<RcsMatchTradeMemo> memoPage;
        //页面点击详情时，先获取备忘录ID对应的备忘录信息
        if(!StringUtils.isBlank(memoId)){
            memoInfo = baseMapper.selectById(memoId);
            if(memoInfo == null){
                throw  new RuntimeException("查找赛事明细，未找到备忘录：" + memoId + " 对应信息！");
            }
        }
        //如果页面点击详情，不传赛事Id 比如点击小喇叭图标查询时，,则从对应备忘鲁中重新获取赛事id
        if(standardMatchId == null && memoInfo != null){
            standardMatchId = memoInfo.getStandardMatchId();
            matchTradeMemo.setStandardMatchId(standardMatchId);
        }
        //获取赛事的所有备忘录
        memoPage = getMemoPage(matchTradeMemo, false);
        List<RcsMatchTradeMemo> matchMemos  = memoPage.getRecords();
        //此处处理仅为 防止页面展示备忘录浏览次数不统一问题
        if(!StringUtils.isBlank(memoId)){
            for(RcsMatchTradeMemo e : matchMemos){
                if(memoId.equals(e.getId())) memoInfo = e;
            }
        }
        //取出未读的备忘录的第一条
        if(memoPage != null && !CollectionUtils.isEmpty(memoPage.getRecords()) && StringUtils.isBlank(memoId)){
            List<String> readMemos = baseMapper.getTraderReadMemos(traderId, standardMatchId);
            matchMemos = memoPage.getRecords().stream().filter(e -> !readMemos.contains(e.getId()) && !traderId.equals(e.getTraderId())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(matchMemos)){
                memoInfo = memoPage.getRecords().get(0);
            }else {
                memoInfo = matchMemos.get(0);
            }
        }

        MatchTradeMemoDetailVo detail = new MatchTradeMemoDetailVo();
        detail.setStandardMatchId(standardMatchId);
        if(memoPage == null || CollectionUtils.isEmpty(memoPage.getRecords())){
            log.error("未获取到标准赛事对应操盘备忘录，标准赛事Id:" + matchTradeMemo.getStandardMatchId());
            return null;
        }
        handleBrowsHistory(memoInfo, traderId, traderName.trim());
        handleMatchInfo(memoInfo);
        detail.setMatchTradeMemo(memoInfo);
        detail.setMemoPage(memoPage);
        return detail;
    }

    private void handleMatchInfo(RcsMatchTradeMemo memoInfo) {
        Query matchQuery = new Query();
        matchQuery.fields().include("score");
        matchQuery.fields().include("period");
        matchQuery.fields().include("secondsMatchStart");
        matchQuery.fields().include("tournamentNames");
        matchQuery.fields().include("tournamentLevel");
        matchQuery.fields().include("matchStatus");
        matchQuery.addCriteria(Criteria.where("matchId").is(memoInfo.getStandardMatchId()));
        MatchMarketLiveBean matchInfo = mongotemplate.findOne(matchQuery, MatchMarketLiveBean.class);
        if(matchInfo == null){
            return;
        }
        memoInfo.setSecondsMatchStart(matchInfo.getSecondsMatchStart());
        memoInfo.setMatchPeriodId(Long.valueOf(matchInfo.getPeriod()));
        memoInfo.setCurrentStatus(matchInfo.getMatchStatus());
        String tourNameStr = "tournamentName(tournamentLevel级联赛)";
        List<I18nItemVo> tournamentNames = matchInfo.getTournamentNames();
        if(!CollectionUtils.isEmpty(tournamentNames)){
            Map<String, List<I18nItemVo>> tournamenGroup = tournamentNames.stream().collect(Collectors.groupingBy(I18nItemVo :: getLanguageType));
            if(!CollectionUtils.isEmpty(tournamenGroup.get("zs"))){
                tourNameStr = tourNameStr.replace("tournamentName",tournamenGroup.get("zs").get(0).getText());
            }else if(!CollectionUtils.isEmpty(tournamenGroup.get("en"))){
                tourNameStr = tourNameStr.replace("tournamentName",tournamenGroup.get("en").get(0).getText());
            }
        }
        String tournamentLevelStr = matchInfo.getTournamentLevel() == null || matchInfo.getTournamentLevel() == 0 ? "无等" : String.valueOf(matchInfo.getTournamentLevel());
        tourNameStr = tourNameStr.replace("tournamentLevel",tournamentLevelStr);
        memoInfo.setTournamentName(tourNameStr);
        String scoreStr = matchInfo.getScore();
        if(StringUtils.isBlank(scoreStr)) return;
        String[] scores = scoreStr.split(":");
        memoInfo.setHomeScore(Integer.valueOf(scores[0]));
        memoInfo.setAwayScore(Integer.valueOf(scores[1]));
    }


    /**
     * 处理历史浏览记录，并更新赛事操盘备忘录记录   备忘录、赛事、操盘手关系状态记录
     * @param memoInfo
     * @param traderId
     * @param traderName
     */
    private void handleBrowsHistory(RcsMatchTradeMemo memoInfo, String traderId, String traderName){
        //如果是本人查看自己创建的备忘录则不做浏览历史处理
        if(traderId.equals(memoInfo.getTraderId())){
            return;
        }
        String memoId = memoInfo.getId();
        memoInfo.setBrowseCount(memoInfo.getBrowseCount() + 1);
        String traderNamesStr = memoInfo.getBrowseHistory();
        List<String> traderNames;
        if(StringUtils.isBlank(traderNamesStr)){
            traderNames = Lists.newArrayList();
        }else{
            traderNames = Lists.newArrayList(traderNamesStr.split(","));
        }
        if(traderNames.contains(traderName)){
            traderNames.remove(traderName);
        }
        traderNames.add(0, traderName);
        memoInfo.setBrowseHistory(traderNames.stream().collect(Collectors.joining(",")));
        Long updateTime = TimeUtils.millsSecondsEast8ZoneGmt();
        baseMapper.updateMatchTradeMemoWhenBrows(memoId, memoInfo.getBrowseCount(), memoInfo.getBrowseHistory(), updateTime);

        //若关系已存在，则不保存关系记录
        RcsMatchUserMemoRef existRef = baseMapper.getTraderMemoRef(memoId, traderId, memoInfo.getStandardMatchId());
        if(existRef != null){
            return;
        }
        RcsMatchUserMemoRef matchUserMemoRef = new RcsMatchUserMemoRef();
        matchUserMemoRef.setStandardMatchId(memoInfo.getStandardMatchId());
        matchUserMemoRef.setTraderId(traderId);
        matchUserMemoRef.setMemoId(memoId);
        matchUserMemoRef.setReadStatus(1);
        matchUserMemoRef.setCreateTime(updateTime);
        matchUserMemoRef.setUpdateTime(updateTime);
        matchUserMemoRefService.save(matchUserMemoRef);
        List<Long> otherUserIds = Lists.newArrayList(Long.valueOf(traderId));
        notifyWs(memoInfo, otherUserIds,true);
    }




    @Transactional
    public Boolean saveMemo(RcsMatchTradeMemo matchTradeMemo){
        log.info("::{}::开始保存赛事操盘备忘录数据",CommonUtil.getRequestId());
        Long standardMatchId = matchTradeMemo.getStandardMatchId();
        StandardMatchInfo matchInfo = standardMatchInfoService.getById(standardMatchId);
        if(matchInfo == null){
            log.error("::{}::未获取到对应赛事信息，保存失败！",CommonUtil.getRequestId());
            return Boolean.FALSE;
        }
        String memoId = UuidUtils.generateUuid().replace("-","");
        BeanUtils.copyProperties(matchInfo, matchTradeMemo);
        matchTradeMemo.setId(memoId);
        Integer operateStage = getOperateStageBymatchInfo(matchInfo);
        matchTradeMemo.setOperateStage(operateStage);
        matchTradeMemo.setTraderName(matchTradeMemo.getTraderName());
        Long now = TimeUtils.millsSecondsEast8ZoneGmt();
        matchTradeMemo.setRecordTime(now);
        matchTradeMemo.setModifyTime(now);
        Boolean success = save(matchTradeMemo);
        List<Long> otherUserIds = baseMapper.getOtherUserIds(matchTradeMemo.getTraderId());
        notifyWs(matchTradeMemo, otherUserIds,false);
        return success;
    }
    private Integer getOperateStageBymatchInfo(StandardMatchInfo matchInfo){
        if(matchInfo.getMatchStatus() == 1){
            //返回滚球
            return 0;
        }
        if(matchInfo.getOddsLive() == 1){
            //返回滚球
            return 0;
        }
        //如果赛事状态为未开赛，并且投注状态为0，根据当前时间是否已过赛事的开始时间，已过则为滚球
        if(matchInfo.getMatchStatus() == 0 && matchInfo.getOddsLive() == 0){
            return System.currentTimeMillis() > matchInfo.getBeginTime() ? 0 : 1;
        }
        return 1;
    }

    private void notifyWs(RcsMatchTradeMemo matchTradeMemo, List<Long> otherUserIds, Boolean readFlag) {
        try {
            log.info("::{}::准备赛事操盘备忘录WS推送数据", CommonUtil.getRequestId());
            if(CollectionUtils.isEmpty(otherUserIds)){
                log.info("::{}::无提醒操盘手查看备忘录数据！",CommonUtil.getRequestId());
                return;
            }
            List<MatchTradeMemoRemindDTO> remindDTOS = new ArrayList<>();
            otherUserIds.forEach(e -> {
                MatchTradeMemoRemindDTO remindDTO = new MatchTradeMemoRemindDTO();
                remindDTO.setTraderId(Long.toString(e));
                remindDTO.setMatchId(matchTradeMemo.getStandardMatchId());
                remindDTO.setMemoId(matchTradeMemo.getId());
                remindDTO.setReadFlag(readFlag);
                remindDTOS.add(remindDTO);
            });
            Request<List<MatchTradeMemoRemindDTO>> msg = new Request<List<MatchTradeMemoRemindDTO>>();
            msg.setData(remindDTOS);
            msg.setLinkId(UUID.randomUUID().toString().replace("-", "") + "_newMatchTradeMemoNotify_trade");
            producerSendMessageUtils.sendMessage("WS_REMIND_TRADE_READ_MEMO","new-memo-notify", "", msg);
            log.info("::{}::已发送消息到赛事操盘备忘录WS推送服务！",CommonUtil.getRequestId());
        } catch (Exception e) {
            log.error("::{}::赛事操盘未读备忘录扫描错误{}",CommonUtil.getRequestId(), e.getMessage(), e);
        }
    }
}
