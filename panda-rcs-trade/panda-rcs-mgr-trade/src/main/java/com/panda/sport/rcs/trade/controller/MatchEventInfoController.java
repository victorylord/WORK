package com.panda.sport.rcs.trade.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.panda.sport.rcs.constants.MatchEventEnum;
import com.panda.sport.rcs.mapper.MatchEventInfoMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mapper.SystemItemDictMapper;
import com.panda.sport.rcs.pojo.MatchEventInfo;
import com.panda.sport.rcs.pojo.MatchStatisticsInfoDetail;
import com.panda.sport.rcs.pojo.dto.CheckMatchEndDTO;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.SystemItemDict;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.MatchStatisticsInfoDetailVO;
import com.panda.sport.rcs.trade.wrapper.MatchEventInfoService;
import com.panda.sport.rcs.trade.wrapper.MatchStatisticsInfoDetailService;
import com.panda.sport.rcs.vo.CustomizedEventBeanVo;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.MatchEventInfoDTO;
import com.panda.sport.rcs.vo.statistics.MatchEventVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.common.utils.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description //TODO
 * @Param
 * @Author kimi
 * @Date 2020/7/21
 * @return
 **/
@RestController
@RequestMapping(value = "/matchEventInfo")
@Slf4j
public class MatchEventInfoController {

    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    private MatchEventInfoService matchEventInfoService;
    @Autowired
    private MatchStatisticsInfoDetailService matchStatisticsInfoDetailService;
    @Autowired
    private MatchEventInfoMapper matchEventInfoMapper;
    @Autowired
    SystemItemDictMapper systemItemDictMapper;

    private static List<String> unFilterEvent = Arrays.asList("kick_off_team",
            "goal","canceled_goal","possible_goal",
            "corner","canceled_corner","possible_corner",
            "red_card","yellow_card","canceled_red_card","canceled_yellow_card","possible_red_card","possible_yellow_card",
            "video_assistant_referee","possible_video_assistant_referee");

    private static String canceled="取消";
    @RequestMapping(value = "/getList", method = RequestMethod.POST)
    public HttpResponse<MatchEventVo> getList(@RequestBody MatchEventInfoDTO matchEventInfoDTO) {
        try {
            MatchEventVo matchEventVo = new MatchEventVo();
            List<CustomizedEventBeanVo> customizedEventBeanVoList = matchEventInfoService.selectMatchEventInfoByMatchId(matchEventInfoDTO.getMatchId(), matchEventInfoDTO.getDataSource(),
                matchEventInfoDTO.getEventTime(),matchEventInfoDTO.getEventTypes(),matchEventInfoDTO.getSort(),matchEventInfoDTO.getLimit(),null);
            if (!CollectionUtils.isEmpty(customizedEventBeanVoList)) {
                List<MatchEventInfo> matchEventInfos = matchEventInfoMapper.selectMatchEventInfoSocre(matchEventInfoDTO.getMatchId(), matchEventInfoDTO.getDataSource(),
                    customizedEventBeanVoList.get(0).getSportId().intValue());
                for (CustomizedEventBeanVo customizedEventBeanVo : customizedEventBeanVoList) {
                    if (MatchEventEnum.Goal.getCode().equals(customizedEventBeanVo.getEventCode())) {
                        customizedEventBeanVo.setScore(customizedEventBeanVo.getT1() + ":" + customizedEventBeanVo.getT2());
                    }
//                    if (customizedEventBeanVo.getScore() == null || "null:null".equals(customizedEventBeanVo.getScore())) {
//                        customizedEventBeanVo.setScore("0:0");
//                    }
                    if (customizedEventBeanVo.getCanceled() != null && customizedEventBeanVo.getCanceled() == 1) {
                        customizedEventBeanVo.setEventName(canceled + customizedEventBeanVo.getEventName());
                    }
                    if (customizedEventBeanVo.getEventCode().equals("time_start")){
                        if (customizedEventBeanVo.getExtraInfo()!=null && customizedEventBeanVo.getExtraInfo().equals("1")){
                            customizedEventBeanVo.setEventName("时钟计时开始");
                        }else {
                            customizedEventBeanVo.setEventName("时钟计时停止");
                        }
                    }
                    customizedEventBeanVo.setSetScore("0:0");
                    customizedEventBeanVo.setCurrentScore("0:0");
                    if (!CollectionUtils.isEmpty(matchEventInfos)) {
                        for (MatchEventInfo matchEventInfo : matchEventInfos) {
                            if (matchEventInfo.getEventTime() <= customizedEventBeanVo.getEventTime()) {
                                customizedEventBeanVo.setScore(matchEventInfo.getT1() + ":" + matchEventInfo.getT2());
                                customizedEventBeanVo.setSetScore(matchEventInfo.getFirstT1() + ":" + matchEventInfo.getFirstT2());
                                if(3!=customizedEventBeanVo.getSportId().intValue()){
                                    customizedEventBeanVo.setCurrentScore(matchEventInfo.getSecondT1() + ":" + matchEventInfo.getSecondT2());
                                }else if((matchEventInfo.getMatchPeriodId().equals(customizedEventBeanVo.getMatchPeriodId()))) {
                                    customizedEventBeanVo.setCurrentScore(matchEventInfo.getSecondT1() + ":" + matchEventInfo.getSecondT2());
                                }
                                break;
                            }
                        }
                        if (customizedEventBeanVo.getEventTime() < matchEventInfos.get(matchEventInfos.size() - 1).getEventTime()) {
                            customizedEventBeanVo.setScore("0:0");
                        }
                    }
                    if (StringUtils.isBlank(customizedEventBeanVo.getScore()) ||customizedEventBeanVo.getScore().contains("null")) {
                        customizedEventBeanVo.setScore("0:0");
                    }
                    if (StringUtils.isBlank(customizedEventBeanVo.getSetScore()) ||customizedEventBeanVo.getSetScore().contains("null")) {
                        customizedEventBeanVo.setSetScore("0:0");
                    }
                    if ("match_status".equals(customizedEventBeanVo.getEventCode())) {
                        LambdaQueryWrapper<SystemItemDict> query = new QueryWrapper<SystemItemDict>().lambda();
                        query.eq(SystemItemDict::getParentTypeId, 8);
                        query.eq(SystemItemDict::getValue, customizedEventBeanVo.getExtraInfo());
                        query.eq(SystemItemDict::getAddition1, customizedEventBeanVo.getSportId());
                        query.select(SystemItemDict::getDescription);
                        List<SystemItemDict> list = systemItemDictMapper.selectList(query);
                        if (list != null && list.size() > 0) {
                            customizedEventBeanVo.setEventName(list.get(0).getDescription());
                        }
                    }
                }
                List<CustomizedEventBeanVo> collect = customizedEventBeanVoList.stream().sorted((a, b) -> b.getEventTime().compareTo(a.getEventTime())).collect(Collectors.toList());
                matchEventVo.setCustomizedEventBeanVoList(collect);
            }
            return HttpResponse.success(matchEventVo);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("服务器出错");
        }
    }


    @RequestMapping(value = "/getMatchStatisticsInfoDetailList", method = RequestMethod.POST)
    public HttpResponse<List<MatchStatisticsInfoDetailVO>> getMatchStatisticsInfoDetailList(@RequestBody MatchEventInfoDTO matchEventInfoDTO) {
        try {
            List<MatchStatisticsInfoDetail> matchStatisticsInfoDetails = matchStatisticsInfoDetailService.slectListByMatchId(matchEventInfoDTO.getMatchId());
            MatchStatisticsInfoDetailVO matchStatisticsInfoDetailVO = new MatchStatisticsInfoDetailVO();
            matchStatisticsInfoDetailVO.setTime(System.currentTimeMillis());
            if(!CollectionUtils.isEmpty(matchStatisticsInfoDetails)){
                matchStatisticsInfoDetailVO.setStatisticsList(matchStatisticsInfoDetails);
                matchStatisticsInfoDetailVO.setStandardMatchId(matchStatisticsInfoDetails.get(0).getStandardMatchId());
            }
            return HttpResponse.success(matchStatisticsInfoDetailVO);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("赛事比分详情出错");
        }
    }

    /**
     * 异常结束赛事结束审核
     * @param dto
     * @return
     */
    @PostMapping("/checkErrorMatchEnd")
    public HttpResponse checkMatchEnd(@RequestBody CheckMatchEndDTO dto) {
        Assert.notNull(dto.getMatchId(), "赛事id不能为空");
        Assert.notNull(dto.getIsEnd(), "是否结束标记不能为空");
        try {
            matchEventInfoService.checkErrorMatchEnd(dto);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("审核失败,内部错误");
        }
        return HttpResponse.success();
    }

    @RequestMapping(value = "/getCFList", method = RequestMethod.POST)
    public HttpResponse<MatchEventVo> getCFList(@RequestBody MatchEventInfoDTO matchEventInfoDTO) {
        try {
            MatchEventVo matchEventVo = new MatchEventVo();
            List<CustomizedEventBeanVo> customizedEventBeanVoListTotal = matchEventInfoService.selectMatchEventInfoByMatchId(matchEventInfoDTO.getMatchId(), null,
                    matchEventInfoDTO.getEventTime(),matchEventInfoDTO.getEventTypes(),matchEventInfoDTO.getSort(),matchEventInfoDTO.getLimit(),unFilterEvent);
            StandardMatchInfo standardMatchInfo = standardMatchInfoMapper.selectById(matchEventInfoDTO.getMatchId());
            Map<String, List<CustomizedEventBeanVo>> map = customizedEventBeanVoListTotal.stream().collect(Collectors.groupingBy(bean -> bean.getDataSourceCode()));
            for (String key : map.keySet()) {
                List<CustomizedEventBeanVo> customizedEventBeanVoList = map.get(key);
                CustomizedEventBeanVo customizedEventBeanVo1 = new CustomizedEventBeanVo();
                customizedEventBeanVo1.setEventTime(standardMatchInfo.getBeginTime());
                customizedEventBeanVo1.setEventName("开赛时间");
                customizedEventBeanVo1.setEventCode("startTime");
                customizedEventBeanVo1.setSportId(standardMatchInfo.getSportId());
                customizedEventBeanVoList.add(0,customizedEventBeanVo1);
                if (!CollectionUtils.isEmpty(customizedEventBeanVoList)) {
                    List<MatchEventInfo> matchEventInfos = matchEventInfoMapper.selectMatchEventInfoSocre(matchEventInfoDTO.getMatchId(), matchEventInfoDTO.getDataSource(),
                            customizedEventBeanVoList.get(0).getSportId().intValue());
                    for (CustomizedEventBeanVo customizedEventBeanVo : customizedEventBeanVoList) {
                        if (MatchEventEnum.Goal.getCode().equals(customizedEventBeanVo.getEventCode())) {
                            customizedEventBeanVo.setScore(customizedEventBeanVo.getT1() + ":" + customizedEventBeanVo.getT2());
                        }
//                    if (customizedEventBeanVo.getScore() == null || "null:null".equals(customizedEventBeanVo.getScore())) {
//                        customizedEventBeanVo.setScore("0:0");
//                    }
                        if (customizedEventBeanVo.getCanceled() != null && customizedEventBeanVo.getCanceled() == 1) {
                            customizedEventBeanVo.setEventName(canceled + customizedEventBeanVo.getEventName());
                        }
                        if (customizedEventBeanVo.getEventCode().equals("time_start")){
                            if (customizedEventBeanVo.getExtraInfo()!=null && customizedEventBeanVo.getExtraInfo().equals("1")){
                                customizedEventBeanVo.setEventName("时钟计时开始");
                            }else {
                                customizedEventBeanVo.setEventName("时钟计时停止");
                            }
                        }
                        if (!CollectionUtils.isEmpty(matchEventInfos)) {
                            for (MatchEventInfo matchEventInfo : matchEventInfos) {
                                if (matchEventInfo.getEventTime() <= customizedEventBeanVo.getEventTime()) {
                                    customizedEventBeanVo.setScore(matchEventInfo.getT1() + ":" + matchEventInfo.getT2());
                                    customizedEventBeanVo.setSetScore(matchEventInfo.getFirstT1() + ":" + matchEventInfo.getFirstT2());
                                    customizedEventBeanVo.setCurrentScore(matchEventInfo.getSecondT1() + ":" + matchEventInfo.getSecondT2());
                                    break;
                                }
                            }
                            if (customizedEventBeanVo.getEventTime() < matchEventInfos.get(matchEventInfos.size() - 1).getEventTime()) {
                                customizedEventBeanVo.setScore("0:0");
                            }
                        }
                        if (StringUtils.isBlank(customizedEventBeanVo.getScore()) ||customizedEventBeanVo.getScore().contains("null")) {
                            customizedEventBeanVo.setScore("0:0");
                        }
                        if ("match_status".equals(customizedEventBeanVo.getEventCode())) {
                            LambdaQueryWrapper<SystemItemDict> query = new QueryWrapper<SystemItemDict>().lambda();
                            query.eq(SystemItemDict::getParentTypeId, 8);
                            query.eq(SystemItemDict::getValue, customizedEventBeanVo.getExtraInfo());
                            query.eq(SystemItemDict::getAddition1, customizedEventBeanVo.getSportId());
                            query.select(SystemItemDict::getDescription);
                            List<SystemItemDict> list = systemItemDictMapper.selectList(query);
                            if (list != null && list.size() > 0) {
                                customizedEventBeanVo.setEventName(list.get(0).getDescription());
                            }
                        }
                    }
                }
                matchEventVo.setCustomizedEventBeanVoListMap(map);
            }
            return HttpResponse.success(matchEventVo);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("服务器出错");
        }
    }
}
