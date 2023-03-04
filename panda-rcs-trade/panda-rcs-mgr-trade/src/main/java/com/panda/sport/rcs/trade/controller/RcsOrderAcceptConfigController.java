package com.panda.sport.rcs.trade.controller;

import com.panda.sport.rcs.constants.MatchEventEnum;
import com.panda.sport.rcs.constants.RedisKeys;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.log.annotion.LogAnnotion;
import com.panda.sport.rcs.pojo.*;
import com.panda.sport.rcs.pojo.dto.tourTemplate.TournamentTemplateDto;
import com.panda.sport.rcs.trade.enums.MatchTypeEnum;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.*;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.IRcsTournamentTemplateService;
import com.panda.sport.rcs.vo.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.rcsOrderAcceptConfig
 * @Description :  联赛设置
 * @Date: 2020-02-01 12:25
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@RestController
@RequestMapping(value = "/rcsOrderAcceptConfigController")
@Slf4j
public class RcsOrderAcceptConfigController {
    @Autowired
    private RcsTournamentOrderAcceptConfigService rcsTournamentOrderAcceptConfigService;
    @Autowired
    private RcsTournamentOrderAcceptEventConfigService rcsTournamentOrderAcceptEventConfigService;
    @Autowired
    private RcsMatchOrderAcceptConfigService rcsMatchOrderAcceptConfigService;
    @Autowired
    private StandardMatchInfoService standardMatchInfoService;
    @Autowired
    private RcsMatchOrderAcceptEventConfigService rcsMatchOrderAcceptEventConfigService;
    @Autowired
    private RedisClient redisClient;

    @Autowired
    private IRcsTournamentTemplateService tournamentTemplateService;

    private static final Integer MIN_TIME = 20;
    private static final Integer ZERO = 0;
    private static final Integer MAX_TIME = 120;
    /**
     * @return com.panda.sport.rcs.vo.HttpResponse<com.panda.sport.rcs.pojo.RcsTournamentOrderAcceptConfig>
     * @Description //获取联赛滚球配置
     * @Param [TournamentId]
     * @Author kimi
     * @Date 2020/2/1
     **/
    @RequestMapping(value = "/getRcsTournamentOrderAcceptConfig", method = RequestMethod.GET)
    public HttpResponse<RcsTournamentOrderAcceptConfig> getRcsTournamentOrderAcceptConfig(Long tournamentId) {
        try {
            RcsTournamentOrderAcceptConfig rcsTournamentOrderAcceptConfig = rcsTournamentOrderAcceptConfigService.selectByTournamentId(tournamentId);
            if (rcsTournamentOrderAcceptConfig == null) {
                rcsTournamentOrderAcceptConfig = rcsTournamentOrderAcceptConfigService.init(tournamentId);
            }
            List<RcsTournamentOrderAcceptEventConfig> rcsTournamentOrderAcceptEventConfigs = rcsTournamentOrderAcceptEventConfigService.selectRcsTournamentOrderAcceptEventConfigs(tournamentId);
            for (RcsTournamentOrderAcceptEventConfig rcsTournamentOrderAcceptEventConfig : rcsTournamentOrderAcceptEventConfigs) {
                rcsTournamentOrderAcceptEventConfig.setEventCodeZH(MatchEventEnum.getNameByCode(rcsTournamentOrderAcceptEventConfig.getEventCode()));
            }
            rcsTournamentOrderAcceptConfig.setList(rcsTournamentOrderAcceptEventConfigs);
            return HttpResponse.success(rcsTournamentOrderAcceptConfig);
        } catch (Exception e) {
            log.error("::{}::风控服务器出问题{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse<com.panda.sport.rcs.pojo.RcsTournamentOrderAcceptConfig>
     * @Description //更新滚球联赛接单配置
     * @Param [rcsTournamentOrderAcceptConfig]
     * @Author kimi
     * @Date 2020/2/1
     **/
    @RequestMapping(value = "/updateRcsTournamentOrderAcceptConfig", method = RequestMethod.POST)
    @LogAnnotion(name = "更新滚球联赛接单配置", keys = {"tournamentId", "dataSource", "minWait", "maxWait", "mode", "halfTime", "eventCode", "eventCodeZH", "maxWait", "valid"},
        title = {"联赛id", "数据源1 SR 2 BC 3 BG", "最短等待时间", "最长等待时间", "接单模式 0 自动 1 手动", "中场休息 0 关闭 1 开启", "事件英文", "事件中文", "等待时间", "0 无效 1 有效"})
    public HttpResponse<RcsTournamentOrderAcceptConfig> updateRcsTournamentOrderAcceptConfig(@RequestBody RcsTournamentOrderAcceptConfig rcsTournamentOrderAcceptConfig) {
        try {
            //验证数据正确性
            Integer minWait = rcsTournamentOrderAcceptConfig.getMinWait();
            if (minWait < ZERO || minWait > MIN_TIME) {
                return HttpResponse.fail("最小等待时间设置超出范围，范围是" + ZERO + "-" + MIN_TIME);
            }
            Integer maxWait = rcsTournamentOrderAcceptConfig.getMaxWait();
            if (maxWait < ZERO || maxWait > MAX_TIME) {
                return HttpResponse.fail("最大等待时间设置超出范围，范围是" + ZERO + "-" + MAX_TIME);
            }
            List<RcsTournamentOrderAcceptEventConfig> list = rcsTournamentOrderAcceptConfig.getList();
            for (RcsTournamentOrderAcceptEventConfig rcsTournamentOrderAcceptEventConfig : list) {
                Integer maxWait1 = rcsTournamentOrderAcceptEventConfig.getMaxWait();
                if (maxWait1 <= ZERO || maxWait1 > MAX_TIME) {
                    return HttpResponse.fail("最大等待时间设置超出范围，范围是" + ZERO + "-" + MAX_TIME);
                }
            }
            rcsTournamentOrderAcceptConfigService.update(rcsTournamentOrderAcceptConfig);
            rcsTournamentOrderAcceptEventConfigService.updateRcsTournamentOrderAcceptEventConfigs(list);
            String eventConfigCacheKey = getTournamentEventConfigCacheKey(rcsTournamentOrderAcceptConfig.getTournamentId());
            String acceptConfigCacheKey = getTournamentAcceptConfigCacheKey(rcsTournamentOrderAcceptConfig.getTournamentId());
            //设置缓存
            redisClient.setExpiry(eventConfigCacheKey, JsonFormatUtils.toJson(list),Long.valueOf(60*60*2));
            redisClient.setExpiry(acceptConfigCacheKey, JsonFormatUtils.toJson(Arrays.asList(rcsTournamentOrderAcceptConfig)),Long.valueOf(60*60*2));
            return HttpResponse.success(rcsTournamentOrderAcceptConfig);
        } catch (Exception e) {
            log.error("::{}::风控服务器出问题{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse<com.panda.sport.rcs.pojo.RcsMatchOrderAcceptConfig>
     * @Description //获取赛事滚球接单配置
     * @Param [matchId]
     * @Author kimi
     * @Date 2020/2/1
     **/
    @RequestMapping(value = "/getRcsMatchOrderAcceptConfig", method = RequestMethod.GET)
    public HttpResponse<RcsMatchOrderAcceptConfig> getRcsMatchOrderAcceptConfig(Long matchId) {
        //先查赛事数据
        try {
            RcsMatchOrderAcceptConfig rcsMatchOrderAcceptConfig = rcsMatchOrderAcceptConfigService.selectRcsMatchOrderAcceptConfigById(matchId);
            if (rcsMatchOrderAcceptConfig == null) {
                rcsMatchOrderAcceptConfig = rcsMatchOrderAcceptConfigService.init(matchId);
//                //1：没有赛事数据 就去查联赛数据
//                StandardMatchInfo standardMatchInfo = standardMatchInfoService.selectById(matchId);
//                List<RcsMatchOrderAcceptConfig> list = null;//getTourTemplate(standardMatchInfo);
//                if(list == null || list.size() == 0) {
//                    //2:有联赛id和赛事
//                    if (standardMatchInfo != null && standardMatchInfo.getStandardTournamentId() != null) {
//                        RcsTournamentOrderAcceptConfig rcsTournamentOrderAcceptConfig = rcsTournamentOrderAcceptConfigService.selectByTournamentId(standardMatchInfo.getStandardTournamentId());
//                        //没有联赛配置数据
//                        if (rcsTournamentOrderAcceptConfig == null) {
//                            rcsMatchOrderAcceptConfig = rcsMatchOrderAcceptConfigService.init(matchId);
//                        } else {
//                            //把联赛的数据复制到赛事里面去
//                            rcsMatchOrderAcceptConfig = rcsMatchOrderAcceptConfigService.init(rcsTournamentOrderAcceptConfig, matchId);
//                        }
//                        log.warn("事件设置：赛事" + matchId + "没有找到配置模板");
//                        //把联赛的数据拿到赛事里面来
//                    } else {
//                        rcsMatchOrderAcceptConfig = rcsMatchOrderAcceptConfigService.init(matchId);
//                    }
//                }else{
//                    rcsMatchOrderAcceptConfig= list.get(0);
//                }
            }


            if(rcsMatchOrderAcceptConfig.getList() == null || rcsMatchOrderAcceptConfig.getList().size() == 0 ) {
                List<RcsMatchOrderAcceptEventConfig> rcsMatchOrderAcceptEventConfigList = rcsMatchOrderAcceptEventConfigService.selectRcsMatchOrderAcceptEventConfig(matchId);
                for (RcsMatchOrderAcceptEventConfig rcsMatchOrderAcceptEventConfig : rcsMatchOrderAcceptEventConfigList) {
                    rcsMatchOrderAcceptEventConfig.setEventCodeZH(MatchEventEnum.getNameByCode(rcsMatchOrderAcceptEventConfig.getEventCode()));
                }
                //没数据要
                rcsMatchOrderAcceptConfig.setList(rcsMatchOrderAcceptEventConfigList);
            }

            return HttpResponse.success(rcsMatchOrderAcceptConfig);
        } catch (Exception e) {
            log.error("::{}::风控服务器出问题{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse<com.panda.sport.rcs.pojo.RcsMatchOrderAcceptConfig>
     * @Description //更新赛事接单配置
     * @Param [rcsMatchOrderAcceptConfig]
     * @Author kimi
     * @Date 2020/2/1
     **/
    @RequestMapping(value = "/updateRcsMatchOrderAcceptConfig", method = RequestMethod.POST)
    @LogAnnotion(name = "更新赛事接单配置", keys = {"matchId", "dataSource", "minWait", "maxWait", "mode", "halfTime", "eventCode", "eventCodeZH", "maxWait", "valid"},
        title = {"赛事Id", "数据源 1 SR 2 BC 3 BG", "最短等待时间", "最长等待时间", "接单模式 0 自动 1 手动", "中场休息 0 关闭 1 开启", "事件英文", "事件中文", "等待时间", "0 无效 1 有效"})
    public HttpResponse<RcsMatchOrderAcceptConfig> updateRcsMatchOrderAcceptConfig(@RequestBody RcsMatchOrderAcceptConfig rcsMatchOrderAcceptConfig) {
        try {
            log.info("::{}::updateRcsMatchOrderAcceptConfig,init bean:{}",CommonUtil.getRequestId(),JsonFormatUtils.toJson(rcsMatchOrderAcceptConfig));
            //验证数据正确性
            Integer maxWait = rcsMatchOrderAcceptConfig.getMaxWait();
            if (maxWait <= ZERO || maxWait > MAX_TIME) {
                return HttpResponse.fail("最大等待时间设置超出范围，范围是" + ZERO + "-" + MAX_TIME);
            }
            Integer minWait = rcsMatchOrderAcceptConfig.getMinWait();
            if (minWait <= ZERO || minWait > MIN_TIME) {
                return HttpResponse.fail("最小等待时间设置超出范围，范围是" + ZERO + "-" + MIN_TIME);
            }
            List<RcsMatchOrderAcceptEventConfig> list = rcsMatchOrderAcceptConfig.getList();
            for (RcsMatchOrderAcceptEventConfig rcsMatchOrderAcceptEventConfig : list) {
                Integer maxWait1 = rcsMatchOrderAcceptEventConfig.getMaxWait();
                if (maxWait1 <= ZERO || maxWait1 > MAX_TIME) {
                    return HttpResponse.fail("最大等待时间设置超出范围，范围是" + ZERO + "-" + MAX_TIME);
                }
            }
            rcsMatchOrderAcceptConfigService.insertOrUpdate(rcsMatchOrderAcceptConfig);
            rcsMatchOrderAcceptEventConfigService.insertOrUpdate(rcsMatchOrderAcceptConfig.getList());

            String eventConfigCacheKey = getMatchEventConfigCacheKey(rcsMatchOrderAcceptConfig.getMatchId());
            String acceptConfigCacheKey = getMatchAcceptConfigCacheKey(rcsMatchOrderAcceptConfig.getMatchId());

            log.info("::{}::updateRcsMatchOrderAcceptConfig,eventConfigCacheKey bean:{}",CommonUtil.getRequestId(),JsonFormatUtils.toJson(rcsMatchOrderAcceptConfig.getList()));
            log.info("::{}::updateRcsMatchOrderAcceptConfig,acceptConfigCacheKey bean-list:{}",CommonUtil.getRequestId(),JsonFormatUtils.toJson(rcsMatchOrderAcceptConfig));
//            redisClient.setExpiry(eventConfigCacheKey, JsonFormatUtils.toJson(rcsMatchOrderAcceptConfig.getList()),Long.valueOf(60*60*2));
//            redisClient.setExpiry(acceptConfigCacheKey, JsonFormatUtils.toJson(Arrays.asList(rcsMatchOrderAcceptConfig)),Long.valueOf(60*60*2));

            redisClient.delete(eventConfigCacheKey);
            redisClient.delete(acceptConfigCacheKey);
            
            log.info("::{}::updateRcsMatchOrderAcceptConfig,eventConfigCacheKey bean:{}",CommonUtil.getRequestId(),redisClient.get(eventConfigCacheKey));
            log.info("::{}::updateRcsMatchOrderAcceptConfig,acceptConfigCacheKey bean:{}",CommonUtil.getRequestId(),redisClient.get(acceptConfigCacheKey));

            return HttpResponse.success(rcsMatchOrderAcceptConfig);
        } catch (Exception e) {
            log.error("::{}::风控服务器出问题{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }


    /**
     * 赛事事件配置
     * @param matchId
     * @return
     */
    private String getMatchEventConfigCacheKey(Long matchId){
        return String.format(RedisKeys.MATCH_EVENT_CONFIG,matchId);
    }

    /**
     * 赛事联赛配置
     * @param TournamentId
     * @return
     */
    private String getTournamentEventConfigCacheKey(Long TournamentId){
        return String.format(RedisKeys.TOURNAMENT_EVENT_CONFIG,TournamentId);
    }

    /**
     * 赛事事件配置
     * @param matchId
     * @return
     */
    private String getMatchAcceptConfigCacheKey(Long matchId){
        return String.format(RedisKeys.MATCH_ACCEPT_CONFIG,matchId);
    }

    /**
     * 赛事联赛配置
     * @param TournamentId
     * @return
     */
    private String getTournamentAcceptConfigCacheKey(Long TournamentId){
        return String.format(RedisKeys.TOURNAMENT_ACCEPT_CONFIG,TournamentId);
    }

    /**
     * @Description  获取联赛模板
     * @Param [matchInfo]
     * @Author  toney
     * @Date  17:30 2020/5/28
     * @return java.util.List<com.panda.sport.rcs.pojo.RcsMatchOrderAcceptConfig>
     **/
    private List<RcsMatchOrderAcceptConfig> getTourTemplate(StandardMatchInfo matchInfo){
    	List<RcsMatchOrderAcceptConfig> configs = new ArrayList<>();
        try {
            List<TournamentTemplateDto> templateAcceptEventList = tournamentTemplateService.query(matchInfo, MatchTypeEnum.LIVE.getId());
            templateAcceptEventList.forEach(bean->{
                RcsMatchOrderAcceptConfig config = new RcsMatchOrderAcceptConfig();

                config.setMaxWait(bean.getAcceptMaxTime());
                config.setMinWait(bean.getAcceptMinTime());
                config.setDataSource(bean.getOrderAcceptEventCode().toUpperCase());
                config.setHalfTime(0);
                config.setMode(0);
                config.setTournamentId(matchInfo.getStandardTournamentId());
                config.setMatchId(matchInfo.getId());

                List<RcsMatchOrderAcceptEventConfig> acceptEventConfigList = new ArrayList<>();
                bean.getAcceptEventList().forEach(event->{
                    RcsMatchOrderAcceptEventConfig acceptEventConfig =new RcsMatchOrderAcceptEventConfig();
                    acceptEventConfig.setEventCode(event.getEventCode());
                    acceptEventConfig.setMaxWait(event.getDelayTime());
                    acceptEventConfig.setEventCodeZH(event.getEventDesc());
                    acceptEventConfig.setMatchId(matchInfo.getId());
                    acceptEventConfig.setValid(event.getStatus() == 1 ? Boolean.TRUE : Boolean.FALSE);
                    acceptEventConfigList.add(acceptEventConfig);
                });
                config.setList(acceptEventConfigList);
                configs.add(config);
            });

            return configs;
        }catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return null;
    }

}
