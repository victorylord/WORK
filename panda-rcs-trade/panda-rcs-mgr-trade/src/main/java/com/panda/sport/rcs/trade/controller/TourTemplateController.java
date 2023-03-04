package com.panda.sport.rcs.trade.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.enums.OperateLogEnum;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.log.annotion.LogAnnotion;
import com.panda.sport.rcs.log.annotion.OperateLog;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.StandardSportTournament;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.dto.tourTemplate.TemplateMenuListDto;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateJumpConfig;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateRef;
import com.panda.sport.rcs.trade.enums.MatchTypeEnum;
import com.panda.sport.rcs.trade.enums.TempTypeEnum;
import com.panda.sport.rcs.trade.param.*;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.tourTemplate.StandardSportTournamentListVo;
import com.panda.sport.rcs.trade.vo.tourTemplate.StandardSportTournamentVo;
import com.panda.sport.rcs.trade.vo.tourTemplate.TournamentTemplatePlayMargainRefVo;
import com.panda.sport.rcs.trade.vo.tourTemplate.TournamentTemplateVo;
import com.panda.sport.rcs.trade.wrapper.IAuthPermissionService;
import com.panda.sport.rcs.trade.wrapper.RcsLanguageInternationService;
import com.panda.sport.rcs.trade.wrapper.StandardSportTournamentService;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.IRcsMatchTemplateModifyService;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.IRcsTournamentTemplateJumpConfigService;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.IRcsTournamentTemplateRefService;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.IRcsTournamentTemplateService;
import com.panda.sport.rcs.utils.StringUtils;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.I18nItemVo;
import com.panda.sports.auth.permission.AuthRequiredPermission;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.dubbo.common.utils.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;


/**
 * 联赛设置等级模板和专用模板
 *
 * @author carver
 * @date 2020-10-03
 */
@RestController
@RequestMapping(value = "/tourTemplate")
@Slf4j
@Component
public class TourTemplateController {
    @Autowired
    private IRcsTournamentTemplateService tournamentTemplateService;
    @Autowired
    private StandardSportTournamentService standardSportTournamentService;
	@Autowired
    private RcsLanguageInternationService rcsLanguageInternationService;
    @Autowired
    private IRcsTournamentTemplateRefService templateRefService;
    @Autowired
    private IRcsMatchTemplateModifyService rcsMatchTemplateModifyService;
    @Autowired
    private IRcsTournamentTemplateJumpConfigService tournamentTemplateJumpConfigService;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    IAuthPermissionService iAuthPermissionService;
    /**
     * 初始化联赛数据
     *
     * @author carver
     * @date 2020-10-03
     */
    @GetMapping("/initTournament")
    public HttpResponse initTournament(Integer sportId, int num) {
        try {
            Assert.notNull(sportId, "赛种不能为空");
            tournamentTemplateService.initTournament(sportId, num);
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::初始化联赛数据:{}", CommonUtil.getRequestId(sportId), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::初始化联赛数据:{}", CommonUtil.getRequestId(sportId), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        }
    }

    /**
     * 联赛模板新增玩法
     *
     * @author carver
     * @date 2021-2-24
     */
    @PostMapping("/addTournamentTemplatePlay")
    public HttpResponse addTournamentTemplatePlay(@RequestBody TournamentTemplateUpdateParam param) {
        try {
            Assert.notNull(param.getSportId(), "赛种不能为空");
            if (CollectionUtils.isEmpty(param.getPlayMargainList())) {
                throw new IllegalArgumentException("玩法配置参数不能为空");
            }
            tournamentTemplateService.addTournamentTemplatePlay(param);
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::联赛模板新增玩法:{}", CommonUtil.getRequestId(param.getMatchId(),param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::联赛模板新增玩法:{}", CommonUtil.getRequestId(param.getMatchId(),param.getId()), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        }
    }

    /**
     * 联赛模板滚球接拒单新增事件
     *
     * @author carver
     * @date 2021-6-5
     */
    @PostMapping("/addTournamentTemplateLiveEvent")
    public HttpResponse addTournamentTemplateLiveEvent(@RequestBody TournamentTemplateAddEventParam param) {
        try {
            Assert.notNull(param.getSportId(), "赛种不能为空");
            if (CollectionUtils.isEmpty(param.getEventList())) {
                throw new IllegalArgumentException("接拒单配置参数不能为空");
            }
            tournamentTemplateService.addTournamentTemplateLiveEvent(param);
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::联赛模板滚球接拒单新增事件:{}", CommonUtil.getRequestId(param.getTypeVal(),param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::联赛模板滚球接拒单新增事件:{}", CommonUtil.getRequestId(param.getTypeVal(),param.getId()), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        }
    }

    /**
     * 获取联赛列表
     *
     * @author carver
     * @date 2020-10-03
     */
    @PostMapping("/getTournamentTemplateList")
    public HttpResponse<IPage<StandardSportTournamentListVo>> getTournamentTemplateList(@RequestBody TournamentTemplateListParam param) {
        try {
            log.info("::{}::获取联赛列表:{}",CommonUtil.getRequestId(), JSONObject.toJSONString(param));
            Assert.notNull(param.getSportId(), "赛种不能为空");
            Assert.notNull(param.getTournamentLevel(), "联赛等级不能为空");
            List<StandardSportTournamentListVo> resultList = new ArrayList<>();
            // 获取所有的联赛数据
            IPage<StandardSportTournament> page = new Page<>(param.getCurrentPage(), param.getPageSize());
            QueryWrapper<StandardSportTournament> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(StandardSportTournament::getSportId, param.getSportId());
            queryWrapper.lambda().eq(StandardSportTournament::getTournamentLevel, param.getTournamentLevel());
            queryWrapper.lambda().eq(StandardSportTournament::getHasRelation, NumberUtils.INTEGER_ONE.intValue());
            if (!ObjectUtils.isEmpty(param.getTournamentId())) {
                queryWrapper.lambda().eq(StandardSportTournament::getId, param.getTournamentId());
            }
            IPage<StandardSportTournament> list = standardSportTournamentService.page(page, queryWrapper);
            IPage<StandardSportTournamentListVo> resultPage = new Page<>(list.getCurrent(), list.getSize(), list.getTotal());
            List<StandardSportTournament> listRecords = page.getRecords();
            if (!CollectionUtils.isEmpty(listRecords)) {
                //根据NameCode一次性获取国际化语言
                List<Long> nameCodes = listRecords.stream().map(StandardSportTournament::getNameCode).collect(Collectors.toList());
                Map<String, List<I18nItemVo>> nameCodeMap = rcsLanguageInternationService.getCachedNamesByCode(nameCodes);
                //根据联赛id，获取所有关联得模板
                List<Long> tournamentIds = listRecords.stream().map(StandardSportTournament::getId).collect(Collectors.toList());
                QueryWrapper<RcsTournamentTemplateRef> templateRefQueryWrapper = new QueryWrapper<>();
                templateRefQueryWrapper.lambda().in(RcsTournamentTemplateRef::getTournamentId, tournamentIds);
                List<RcsTournamentTemplateRef> tournamentTemplateRefList = templateRefService.list(templateRefQueryWrapper);
                Map<Long, List<RcsTournamentTemplateRef>> tournamentTemplateRefMap = tournamentTemplateRefList.stream().collect(groupingBy(RcsTournamentTemplateRef::getTournamentId));
                //根据联赛id获取关联的早盘和滚球模板id
                Map<Long, List<RcsTournamentTemplate>> templateMap = null;
                if (!CollectionUtils.isEmpty(tournamentTemplateRefList)) {
                    List<Long> templateIds = Lists.newArrayList();
                    List<Long> preTemplateIds = tournamentTemplateRefList.stream().map(RcsTournamentTemplateRef::getTemplateId).collect(Collectors.toList());
                    List<Long> liveTemplateIds = tournamentTemplateRefList.stream().map(RcsTournamentTemplateRef::getLiveTemplateId).collect(Collectors.toList());
                    templateIds.addAll(preTemplateIds);
                    templateIds.addAll(liveTemplateIds);
                    QueryWrapper<RcsTournamentTemplate> rcsTournamentTemplateQueryWrapper = new QueryWrapper<>();
                    rcsTournamentTemplateQueryWrapper.lambda().in(RcsTournamentTemplate::getId, templateIds);
                    List<RcsTournamentTemplate> templateList = tournamentTemplateService.list(rcsTournamentTemplateQueryWrapper);
                    templateMap = templateList.stream().collect(groupingBy(RcsTournamentTemplate::getId));
                }
                //根据联赛等级获取早盘和滚球模板
                QueryWrapper<RcsTournamentTemplate> preTournamentTemplateQueryWrapper = new QueryWrapper<>();
                preTournamentTemplateQueryWrapper.lambda().eq(RcsTournamentTemplate::getSportId, param.getSportId())
                        .eq(RcsTournamentTemplate::getType, TempTypeEnum.LEVEL.getId())
                        .eq(RcsTournamentTemplate::getTypeVal, param.getTournamentLevel());
                List<RcsTournamentTemplate> rcsTournamentTemplate = tournamentTemplateService.list(preTournamentTemplateQueryWrapper);
                List<RcsTournamentTemplate> preTemplateMenuList = rcsTournamentTemplate.stream().filter(e -> e.getMatchType() == 1).collect(Collectors.toList());
                List<RcsTournamentTemplate> liveTemplateMenuList = rcsTournamentTemplate.stream().filter(e -> e.getMatchType() == 0).collect(Collectors.toList());

                for (StandardSportTournament sportTournament : listRecords) {
                    StandardSportTournamentListVo standardSportTournamentVo = new StandardSportTournamentListVo();
                    standardSportTournamentVo.setId(sportTournament.getId());
                    standardSportTournamentVo.setFatherTournamentId(sportTournament.getFatherTournamentId());
                    standardSportTournamentVo.setTargetProfitRate(sportTournament.getTargetProfitRate());
                    standardSportTournamentVo.setMtsOddsChangeValue(sportTournament.getMtsOddsChangeValue());
                    standardSportTournamentVo.setOddsChangeStatus(sportTournament.getOddsChangeStatus());
                    if (!CollectionUtils.isEmpty(nameCodeMap.get(String.valueOf(sportTournament.getNameCode())))) {
                        standardSportTournamentVo.setLanguageCodeList(nameCodeMap.get(sportTournament.getNameCode().toString()));
                    }
                    // 获取联赛已关联的模板
                    List<RcsTournamentTemplateRef> refList = tournamentTemplateRefMap.get(sportTournament.getId());
                    if (!CollectionUtils.isEmpty(refList)) {
                        RcsTournamentTemplateRef tournamentTemplateRef = refList.get(0);
                        if (tournamentTemplateRef != null) {
                            // 设置是否热门
                            standardSportTournamentVo.setIsPopular(tournamentTemplateRef.getIsPopular());
                            standardSportTournamentVo.setOrderDelayTime(tournamentTemplateRef.getOrderDelayTime());
                            if (templateMap.size() > 0) {
                                // 关联已设置的等级或专用模板
                                List<RcsTournamentTemplate> templateList = templateMap.get(tournamentTemplateRef.getTemplateId());
                                if (!CollectionUtils.isEmpty(templateList)) {
                                    standardSportTournamentVo.setTemplate(templateList.get(0));
                                }
                                List<RcsTournamentTemplate> liveTemplateList = templateMap.get(tournamentTemplateRef.getLiveTemplateId());
                                if (!CollectionUtils.isEmpty(liveTemplateList)) {
                                    standardSportTournamentVo.setLiveTemplate(liveTemplateList.get(0));
                                }
                            }
                        }
                    } else {
                        // 设置是否热门  1:是 0:否
                        standardSportTournamentVo.setIsPopular(NumberUtils.INTEGER_ZERO.intValue());
                        //综合球类接单延迟时间,默认5秒
                        standardSportTournamentVo.setOrderDelayTime(5);
                    }
                    // 默认关联早盘等级模板
                    if (standardSportTournamentVo.getTemplate() == null && !CollectionUtils.isEmpty(preTemplateMenuList)) {
                        RcsTournamentTemplate preRcsTournamentTemplate = preTemplateMenuList.get(0);
                        TemplateMenuListDto preTemplateMenuListDto = BeanCopyUtils.copyProperties(preRcsTournamentTemplate, TemplateMenuListDto.class);
                        standardSportTournamentVo.setTemplate(preTemplateMenuListDto);
                    }
                    // 默认关联滚球等级模板
                    if (standardSportTournamentVo.getLiveTemplate() == null && !CollectionUtils.isEmpty(liveTemplateMenuList)) {
                        RcsTournamentTemplate liveRcsTournamentTemplate = liveTemplateMenuList.get(0);
                        TemplateMenuListDto liveTemplateMenuListDto = BeanCopyUtils.copyProperties(liveRcsTournamentTemplate, TemplateMenuListDto.class);
                        standardSportTournamentVo.setLiveTemplate(liveTemplateMenuListDto);
                    }
                    resultList.add(standardSportTournamentVo);
                }
            }
            resultPage.setRecords(resultList);
            return HttpResponse.success(resultPage);
        } catch (IllegalArgumentException ex) {
            log.error("::{}::获取联赛列表:{}", CommonUtil.getRequestId(param.getTournamentId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::获取联赛列表:{}", CommonUtil.getRequestId(param.getTournamentId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        }
    }

    /**
     * 获取联赛列表
     *
     * @author carver
     * @date 2020-10-03
     */
    @PostMapping("/getTemplateByTournamentId")
    public HttpResponse<StandardSportTournamentListVo> getTemplateByTournamentId(@RequestBody TournamentTemplateListParam param) {
        try {
            log.info("::{}::获取联赛列表:{}",CommonUtil.getRequestId(), JSONObject.toJSONString(param));
            Assert.notNull(param.getSportId(), "赛种id不能为空");
            Assert.notNull(param.getTournamentId(), "联赛id不能为空");
            Assert.notNull(param.getTournamentLevel(), "联赛等级不能为空");
            //获取早盘等级模板和联赛专用模板
            List<TemplateMenuListDto> preList = getMenuList(param, MatchTypeEnum.EARLY);
            //获取滚球等级模板和联赛专用模板
            List<TemplateMenuListDto> liveList = getMenuList(param, MatchTypeEnum.LIVE);
            StandardSportTournamentListVo vo = new StandardSportTournamentListVo();
            vo.setId(param.getTournamentId());
            vo.setMenuList(preList);
            vo.setLiveMenuList(liveList);
            //设置父联赛数据
            if (StringUtils.isNotBlank(param.getFatherTournamentId())) {
                StandardSportTournament father = standardSportTournamentService.getById(param.getFatherTournamentId());
                if (!ObjectUtils.isEmpty(father)) {
                    vo.setFatherTournamentId(param.getFatherTournamentId());
                    vo.setFatherTournamentLevel(father.getTournamentLevel());
                    QueryWrapper<StandardSportTournament> wrapper = new QueryWrapper<>();
                    wrapper.lambda().eq(StandardSportTournament::getFatherTournamentId, param.getFatherTournamentId());
                    List<StandardSportTournament> fatherChild = standardSportTournamentService.list(wrapper);
                    //根据NameCode获取国际化语言
                    List<Long> nameCodes = fatherChild.stream().map(StandardSportTournament::getNameCode).collect(Collectors.toList());
                    nameCodes.add(father.getNameCode());
                    Map<String, List<I18nItemVo>> nameCodeMap = rcsLanguageInternationService.getCachedNamesByCode(nameCodes);
                    vo.setLanguageCodeList(nameCodeMap.get(String.valueOf(father.getNameCode())));
                    List<StandardSportTournamentListVo.ChildTournament> childTournaments = new ArrayList<>();
                    for (StandardSportTournament child : fatherChild) {
                        StandardSportTournamentListVo.ChildTournament childTournament = new StandardSportTournamentListVo.ChildTournament();
                        childTournament.setId(child.getId());
                        childTournament.setTournamentLevel(child.getTournamentLevel());
                        childTournament.setLanguageCodeList(nameCodeMap.get(String.valueOf(child.getNameCode())));
                        childTournaments.add(childTournament);
                    }
                    vo.setChildTournament(childTournaments);
                }
            }
            return HttpResponse.success(vo);
        } catch (IllegalArgumentException ex) {
            log.error("::{}::获取联赛列表:{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::获取联赛列表:{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        }
    }

    private List<TemplateMenuListDto> getMenuList(TournamentTemplateListParam param, MatchTypeEnum matchTypeEnum) {
        Map<String, Object> liveMap = new HashMap<>(4);
        liveMap.put("sportId", param.getSportId());
        liveMap.put("matchType", matchTypeEnum.getId());
        liveMap.put("tournamentLevel", param.getTournamentLevel());
        liveMap.put("tournamentId", param.getTournamentId());
        List<TemplateMenuListDto> list = tournamentTemplateService.menuList(liveMap);
        return list;
    }

    /**
     * @param param:
     * @Description: 根据输入条件，筛选联赛
     * @Author carver
     * @Date 2020/10/20 20:55
     * @return: com.panda.sport.rcs.vo.HttpResponse
     **/
    @PostMapping("/findTournamentByName")
    public HttpResponse findTournamentByName(@RequestBody TournamentTemplateListParam param) {
        if (StringUtils.isBlank(param.getTournamentName()) || ObjectUtils.isEmpty(param.getSportId())) {
            return HttpResponse.success();
        }
        try {
            List<StandardSportTournament> list = standardSportTournamentService.queryTournamentByName(param.getSportId(), param.getTournamentName());
            if (CollectionUtils.isEmpty(list)) {
                return HttpResponse.success();
            }
            List<StandardSportTournamentVo> rtnList = BeanCopyUtils.copyPropertiesList(list, StandardSportTournamentVo.class);
            return HttpResponse.success(rtnList);
        } catch (Exception e) {
            log.error("::{}::findTournamentByName:{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        }
    }

    /**
     * 变更联赛等级
     *
     * @author carver
     * @date 2020-10-03
     */
    @PostMapping("/updateTournamentLevel")
    @LogAnnotion(name = "更新联赛等级", keys = {"id", "level", "userName"}, title = {"id", "等级", "用户名"})
    @OperateLog(operateType = OperateLogEnum.TOURNAMENT_CONFIG)
    public HttpResponse updateTournamentLevel(@RequestBody UpdateTournamentLevelParam param) {
        try {
            log.info("::{}::变更联赛等级:，操盘手:{}{}",CommonUtil.getRequestId(), JSONObject.toJSONString(param), TradeUserUtils.getUserIdNoException());
            Assert.notNull(param.getId(), "联赛id不能为空");
            Assert.notNull(param.getLevel(), "联赛等级不能为空");
            Assert.notNull(param.getIsPopular(), "是否热门不能为空");
            boolean b = iAuthPermissionService.checkAuthOpearate( "Risk:Tour:Level:Save");
            if (!b) {
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            tournamentTemplateService.updateTournamentLevel(param);
        } catch (IllegalArgumentException ex) {
            log.error("::{}::更新联赛等级:{}", CommonUtil.getRequestId(param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::更新联赛等级:{}", CommonUtil.getRequestId(param.getId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        }
        return HttpResponse.success();
    }

    /**
     * 更新所属联赛模板
     *
     * @author carver
     * @date 2020-10-03
     */
    @PostMapping("/updateTemplateRelation")
    @LogAnnotion(name = "更新所属联赛模板", keys = {"tournamentId", "templateId", "matchType", "userName"}, title = {"联赛id", "模板id", "1:为早盘  2：为滚球", "用户名"})
    @OperateLog(operateType = OperateLogEnum.TEMPLATE_SELECTION)
    public HttpResponse updateTournamentTemplate(@RequestBody UpdateTournamentTemplateParam param) {
        try {
            log.info("::{}::更新所属联赛模板:{}，操盘手:{}",CommonUtil.getRequestId(param.getId()), JSONObject.toJSONString(param),TradeUserUtils.getUserIdNoException());
            Assert.notNull(param.getId(), "联赛id不能为空");
            Assert.notNull(param.getPreLemplateId(), "早盘模板id不能为空");
            Assert.notNull(param.getLiveLemplateId(), "滚球模板id不能为空");
            boolean b = iAuthPermissionService.checkAuthOpearate( "Risk:Tour:Temp:Manage:Save");
            if (!b) {
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            tournamentTemplateService.updateTournamentTemplateRelationConfig(param);
        } catch (IllegalArgumentException ex) {
            log.error("::{}::更新所属联赛模板:{}", CommonUtil.getRequestId(param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::更新所属联赛模板:{}", CommonUtil.getRequestId(param.getId()), e.getMessage(), e);
            return HttpResponse.error(HttpResponse.FAIL, e.getMessage());
        }
        return HttpResponse.success();
    }

    /**
     * 根据条件获取模板信息
     *
     * @author carver
     * @date 2020-10-03
     */
    @PostMapping("/getTournamentTemplateDetail")
    public HttpResponse<TournamentTemplateVo> getTournamentTemplateDetail(@RequestBody TournamentTemplateParam param, @RequestHeader("lang") String lang) {
        try {
            log.info("::{}::根据条件获取模板信息:{}，操盘手:{}",CommonUtil.getRequestId(), JSONObject.toJSONString(param),TradeUserUtils.getUserIdNoException());
            Assert.notNull(param.getSportId(), "运动类型不能为空");
            Assert.notNull(param.getMatchType(), "盘口类型不能为空");
            Assert.notNull(param.getType(), "类型不能为空");
            Assert.notNull(param.getTypeVal(), "类型值不能为空");
            Assert.notNull(lang, "国际化lang不能为空");
            TournamentTemplateVo vo = tournamentTemplateService.queryTournamentTemplateAndPlay(param, lang);
            return HttpResponse.success(vo);
        } catch (IllegalArgumentException ex) {
            log.error("::{}::根据条件获取模板信息:{}", CommonUtil.getRequestId(param.getTypeVal(),param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::根据条件获取模板信息:{}", CommonUtil.getRequestId(param.getTypeVal(),param.getId()), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        }
    }

    /**
     * 获取分时margin节点信息
     *
     * @author carver
     * @date 2020-10-03
     */
    @PostMapping("/getTournamentTemplatePlayMargin")
    public HttpResponse<TournamentTemplatePlayMargainRefVo> getTournamentTemplatePlayMargin(@RequestBody TournamentTemplatePlayMargainRefParam param) {
        try {
            log.info("::{}::获取分时margin节点信息:{}，操盘手:{}",CommonUtil.getRequestId(), JSONObject.toJSONString(param),TradeUserUtils.getUserIdNoException());
            Assert.notNull(param.getMargainId(), "玩法主键id不能为空");
            Assert.notNull(param.getTimeVal(), "时间节点不能为空");
            Assert.notNull(param.getMatchType(), "盘口类型不能为空");
            TournamentTemplatePlayMargainRefVo vo = tournamentTemplateService.queryTournamentTemplatePlayMargin(param);
            return HttpResponse.success(vo);
        } catch (IllegalArgumentException ex) {
            log.error("::{}::获取分时margin节点信息:{}", CommonUtil.getRequestId(param.getMargainId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception e) {
            log.error("::{}::获取分时margin节点信息:{}", CommonUtil.getRequestId(param.getMargainId()), e.getMessage(), e);
            return HttpResponse.fail(e.getMessage());
        }
    }

    /**
     * 更新联赛模板信息
     *
     * @author carver
     * @date 2020-10-03
     */
    @PostMapping("/updateTemplate")
    @LogAnnotion(name = "更新", keys = {"templateEventList", "playMargainList", "acceptEventList", "userName"}, title = {"事件配置", "玩法margain配置", "事件模板", "用户名"})
    @OperateLog(operateType = OperateLogEnum.TEMPLATE_UPDATE)
    public HttpResponse update(@RequestBody TournamentTemplateUpdateParam param) {
        if (param.getId() == null) {
            return HttpResponse.error(HttpResponse.FAIL, "模板id不能为空");
        }

        try {
            log.info("::{}::更新联赛模板信息:{}，操盘手:{}",CommonUtil.getRequestId(param.getTypeVal(),param.getId()), JSONObject.toJSONString(param),TradeUserUtils.getUserIdNoException());
            boolean b = iAuthPermissionService.checkAuthOpearate( "Risk:Tour:Temp:Save");
            if (!b) {
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            tournamentTemplateService.updateTemplate(param);
            return HttpResponse.success();
        } catch (Exception ex) {
            log.error("::{}::更新联赛模板信息:{}", CommonUtil.getRequestId(param.getTypeVal(),param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(500, ex.getMessage());
        }
    }

    /**
     * 更新分时margin数据
     *
     * @author carver
     * @date 2020-10-03
     */
    @PostMapping("/updateMargainRef")
    @OperateLog(operateType = OperateLogEnum.TEMPLATE_UPDATE)
    public HttpResponse modifyMargainRef(@RequestBody TournamentTemplatePlayMargainRefParam param) {
        if (param.getMargainId() == null) {
            return HttpResponse.error(HttpResponse.FAIL, "margainId不能为空");
        }
        if (param.getTimeVal() == null) {
            return HttpResponse.error(HttpResponse.FAIL, "timeVal不能为空");
        }
        if(SportIdEnum.isFootball(param.getSportId())&& TradeConstant.TEMPLATE_BUSI_PLAY.contains(param.getPlayId()) && Double.valueOf(param.getMargain()) < 200){
            return HttpResponse.error(HttpResponse.FAIL, "margin不能低于200");
        }
        try {
            log.info("::{}::更新分时margin数据:{}，操盘手:{}",CommonUtil.getRequestId(param.getMargainId()), JSONObject.toJSONString(param),TradeUserUtils.getUserIdNoException());
            boolean b = iAuthPermissionService.checkAuthOpearate( "Risk:Tour:Temp:Save");
            if (!b) {
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            rcsMatchTemplateModifyService.modifyMargainRef(param, NumberUtils.INTEGER_ONE);
        } catch (Exception ex) {
            log.error("::{}::更新分时margin数据:{}", CommonUtil.getRequestId(param.getMargainId()), ex.getMessage(), ex);
            return HttpResponse.error(HttpResponse.FAIL, ex.getMessage());
        }
        return HttpResponse.success();
    }


    /**
     * 更新联赛模板信息
     *
     * @author carver
     * @date 2020-10-03
     */
    @PostMapping("/saveSpecialTemplate")
    public HttpResponse saveSpecialTemplate(@RequestBody TournamentTemplateUpdateParam param) {
        try {
            log.info("::{}::专用联赛模板信息:{}，操盘手:{}",CommonUtil.getRequestId(), JSONObject.toJSONString(param),TradeUserUtils.getUserIdNoException());
            Assert.notNull(param.getSportId(), "运动类型不能为空");
            Assert.notNull(param.getMatchType(), "盘口类型不能为空");
            Assert.notNull(param.getTypeVal(), "类型值不能为空");
            Assert.notNull(param.getType(), "类型不能为空");
            boolean b = iAuthPermissionService.checkAuthOpearate( "Risk:Tour:Temp:Save");
            if (!b) {
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            RcsTournamentTemplate template = tournamentTemplateService.saveSpecialTemplate(param);
            return HttpResponse.success(template);
        } catch (IllegalArgumentException ex) {
            log.error("::{}::更新联赛模板信息:{}", CommonUtil.getRequestId(param.getTypeVal(),param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::更新联赛模板信息:{}", CommonUtil.getRequestId(param.getTypeVal(),param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(500, ex.getMessage());
        }
    }

    /**
     * 获取早盘或者滚球联赛专用模板信息
     *
     * @author carver
     * @date 2020-10-03
     */
    @PostMapping("/getSpecialTemplateDetail")
    public HttpResponse getSpecialTemplateDetail(@RequestBody TournamentTemplateParam param) {
        try {
            log.info("::{}::获取联赛专用模板信息:{}，操盘手:{}",CommonUtil.getRequestId(), JSONObject.toJSONString(param),TradeUserUtils.getUserIdNoException());
            Assert.notNull(param.getSportId(), "运动类型不能为空");
            Assert.notNull(param.getMatchType(), "盘口类型不能为空");
            List<RcsTournamentTemplate> vo = tournamentTemplateService.getSpecialTemplateDetail(param);
            return HttpResponse.success(vo);
        } catch (Exception ex) {
            log.error("::{}::获取早盘或者滚球联赛专用模板信息:{}", CommonUtil.getRequestId(param.getTypeVal(),param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(500, ex.getMessage());
        }
    }

    /**
     * 删除专用模板,先确认是否有联赛关联
     *
     * @author carver
     * @date 2020-10-03
     */
    @PostMapping("/confirmRemoveSpecialTemplate")
    public HttpResponse confirmRemoveSpecialTemplate(@RequestBody TournamentTemplateParam param) {
        try {
            log.info("::{}::删除专用模板-先确认是否有联赛关联-入参:{}，操盘手:{}",CommonUtil.getRequestId(param.getTypeVal(),param.getId()), JSONObject.toJSONString(param),TradeUserUtils.getUserIdNoException());
            boolean b = iAuthPermissionService.checkAuthOpearate( "Risk:Tour:Special:Delete");
            if (!b) {
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            Assert.notNull(param.getSportId(), "运动类型不能为空");
            Assert.notNull(param.getMatchType(), "盘口类型不能为空");
            Assert.notNull(param.getId(), "模板id不能为空");

            // 根据参数条件，判断是否存在模板数据
            QueryWrapper<RcsTournamentTemplate> tempWrapper = new QueryWrapper<>();
            tempWrapper.lambda().eq(RcsTournamentTemplate::getSportId, param.getSportId())
                    .eq(RcsTournamentTemplate::getMatchType, param.getMatchType())
                    .eq(RcsTournamentTemplate::getType, TempTypeEnum.TOUR.getId())
                    .eq(RcsTournamentTemplate::getId, param.getId());
            RcsTournamentTemplate template = tournamentTemplateService.getOne(tempWrapper);
            if (!ObjectUtils.isEmpty(template)) {
                QueryWrapper<RcsTournamentTemplateRef> ref = new QueryWrapper<>();
                if (param.getMatchType().equals(NumberUtils.INTEGER_ONE)) {
                    ref.lambda().eq(RcsTournamentTemplateRef::getTemplateId, param.getId());
                } else {
                    ref.lambda().eq(RcsTournamentTemplateRef::getLiveTemplateId, param.getId());
                }
                List<RcsTournamentTemplateRef> refList = templateRefService.list(ref);
                if (CollectionUtils.isEmpty(refList)) {
                    return HttpResponse.success();
                }
                List<Long> tournamentIds = refList.stream().map(RcsTournamentTemplateRef::getTournamentId).collect(Collectors.toList());
                QueryWrapper<StandardSportTournament> tourWrapper = new QueryWrapper<>();
                tourWrapper.lambda().eq(StandardSportTournament::getSportId, param.getSportId())
                        .eq(StandardSportTournament::getHasRelation, NumberUtils.INTEGER_ONE.intValue())
                        .in(StandardSportTournament::getId, tournamentIds);
                List<StandardSportTournament> list = standardSportTournamentService.list(tourWrapper);
                if (CollectionUtils.isEmpty(list)) {
                    return HttpResponse.error(HttpResponse.FAIL, "关联的模板未找到标准联赛，联赛id:" + JSONObject.toJSONString(tournamentIds));
                }
                //根据NameCode一次性获取国际化语言
                List<Long> nameCodes = list.stream().map(StandardSportTournament::getNameCode).collect(Collectors.toList());
                Map<String, List<I18nItemVo>> nameCodeMap = rcsLanguageInternationService.getCachedNamesByCode(nameCodes);
                return HttpResponse.success(nameCodeMap.values());
            } else {
                return HttpResponse.error(HttpResponse.FAIL, "专用模板数据不存在！");
            }
        } catch (IllegalArgumentException ex) {
            log.error("::{}::删除专用模板,先确认是否有联赛关联:{}", CommonUtil.getRequestId(param.getTypeVal(),param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::删除专用模板,先确认是否有联赛关联:{}", CommonUtil.getRequestId(param.getTypeVal(),param.getId()), ex.getMessage(), ex);
            return HttpResponse.fail(ex.getMessage());
        }
    }


    /**
     * 删除专用模板
     *
     * @author carver
     * @date 2020-10-03
     */
    @PostMapping("/removeSpecialTemplate")
    @LogAnnotion(name = "删除专用模板", keys = {"sportId", "matchType", "templateName", "id"}, title = {"塞种", "盘口类型", "模板名称", "模板id"})
    @OperateLog(operateType = OperateLogEnum.TEMPLATE_DELETE)
    public HttpResponse removeSpecialTemplate(@RequestBody TournamentTemplateParam param) {
        try {
            log.info("::{}::删除专用模板-入参:{}，操盘手:{}",CommonUtil.getRequestId(param.getId()), JSONObject.toJSONString(param),TradeUserUtils.getUserIdNoException());
            Assert.notNull(param.getSportId(), "运动类型不能为空");
            Assert.notNull(param.getMatchType(), "盘口类型不能为空");
            Assert.notNull(param.getTemplateName(), "模板名称");
            Assert.notNull(param.getId(), "模板id不能为空");
            RcsTournamentTemplate template = tournamentTemplateService.getById(param.getId());
            if (!ObjectUtils.isEmpty(template)) {
                tournamentTemplateService.removeSpecialTemplate(template);
            } else {
                return HttpResponse.error(HttpResponse.FAIL, "专用模板数据不存在！");
            }
        } catch (IllegalArgumentException ex) {
            log.error("::{}::删除专用模板:{}", CommonUtil.getRequestId(param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::删除专用模板:{}", CommonUtil.getRequestId(param.getId()), ex.getMessage(), ex);
            return HttpResponse.fail(ex.getMessage());
        }
        return HttpResponse.success();
    }

    /**
     * 更新玩法赔率源设置
     *
     * @author carver
     * @date 2021-02-09
     */
    @PostMapping("/updatePlayOddsConfig")
    @LogAnnotion(name = "更新玩法赔率源设置", keys = {"templateId", "playOddsConfigs"}, title = {"模板id", "playOddsConfigs"})
    @OperateLog
    public HttpResponse updatePlayOddsConfig(@RequestBody RcsTournamentTemplatePlayOddsConfigParam param) {
        try {
            log.info("::{}::更新玩法赔率源设置:{}，操盘手:{}",CommonUtil.getRequestId(), JSONObject.toJSONString(param),TradeUserUtils.getUserIdNoException());
            boolean b = iAuthPermissionService.checkAuthOpearate( "Risk:Tour:Odds:Save");
            if (!b) {
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            Assert.notNull(param.getTemplateId(), "模板id不能为空");
            if (CollectionUtils.isEmpty(param.getPlayOddsConfigs())) {
                throw new IllegalArgumentException("玩法赔率源设置不能为空");
            }
            tournamentTemplateService.updatePlayOddsConfig(param);
        } catch (IllegalArgumentException ex) {
            log.error("::{}::更新玩法赔率源设置:{}", CommonUtil.getRequestId(param.getMatchId(),param.getTemplateId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::更新玩法赔率源设置:{}", CommonUtil.getRequestId(param.getMatchId(),param.getTemplateId()), ex.getMessage(), ex);
            return HttpResponse.error(HttpResponse.FAIL, ex.getMessage());
        }
        return HttpResponse.success();
    }

    /**
     * 复制父联赛专用模板
     *
     * @author carver
     * @date 2021-2-21
     */
    @PostMapping("/copyFatherSpecialTemplate")
    public HttpResponse copyFatherSpecialTemplate(@RequestBody TournamentTemplateParam param) {
        try {
            log.info("::{}::复制父联赛专用模板-入参:{}，操盘手:{}",CommonUtil.getRequestId(param.getId()), JSONObject.toJSONString(param),TradeUserUtils.getUserIdNoException());
            Assert.notNull(param.getId(), "联赛模板id不能为空");
            Assert.notNull(param.getFatherTournamentId(), "父联赛模板id不能为空");
            tournamentTemplateService.copyFatherSpecialTemplate(param);
        } catch (IllegalArgumentException ex) {
            log.error("::{}::复制父联赛专用模板:{}", CommonUtil.getRequestId(param.getId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::复制父联赛专用模板:{}", CommonUtil.getRequestId(param.getId()), ex.getMessage(), ex);
            return HttpResponse.fail(ex.getMessage());
        }
        return HttpResponse.success();
    }


    /**
     * 赛事模板同步联赛模板新增玩法到开售列表
     *
     * @author carver
     * @date 2021-4-29
     */
    @PostMapping("/matchSyncTourTempPlay")
    public HttpResponse matchSyncTourTempPlay(@RequestBody TournamentTemplateParam param) {
        try {
            log.info("::{}::赛事模板同步联赛模板新增玩法到开售列表-入参:{}，操盘手:{}",CommonUtil.getRequestId(param.getTypeVal()), JSONObject.toJSONString(param),TradeUserUtils.getUserIdNoException());
            Assert.notNull(param.getSportId(), "赛种不能为空");
            Assert.notNull(param.getTypeVal(), "赛事id不能为空");
            Assert.notNull(param.getMatchType(), "盘口类型不能为空");
            tournamentTemplateService.matchSyncTourTempPlay(param);
        } catch (IllegalArgumentException ex) {
            log.error("::{}::赛事模板同步联赛模板新增玩法到开售列表:{}", CommonUtil.getRequestId(param.getTypeVal()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::赛事模板同步联赛模板新增玩法到开售列表:{}", CommonUtil.getRequestId(param.getTypeVal()), ex.getMessage(), ex);
            return HttpResponse.fail(ex.getMessage());
        }
        return HttpResponse.success();
    }

    /**
     * 获取综合操盘联赛模板跳分设置
     *
     * @author carver
     * @date 2021-9-29
     */
    @PostMapping("/getTournamentTemplateJumpConfig")
    public HttpResponse getTournamentTemplateJumpConfig(@RequestBody TournamentTemplateJumpConfigParam param) {
        try {
            log.info("::{}::获取综合操盘联赛模板跳分设置:{}，操盘手:{}",CommonUtil.getRequestId(), JSONObject.toJSONString(param),TradeUserUtils.getUserIdNoException());
            Assert.notNull(param.getSportId(), "运动类型不能为空");
            Assert.notNull(param.getMatchType(), "盘口类型不能为空");
            Assert.notNull(param.getTournamentId(), "联赛id不能为空");
            QueryWrapper<RcsTournamentTemplateJumpConfig> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(RcsTournamentTemplateJumpConfig::getSportId, param.getSportId())
                    .eq(RcsTournamentTemplateJumpConfig::getMatchType, param.getMatchType())
                    .eq(RcsTournamentTemplateJumpConfig::getTournamentId, param.getTournamentId());
            RcsTournamentTemplateJumpConfig config = tournamentTemplateJumpConfigService.getOne(wrapper);
            return HttpResponse.success(config);
        } catch (IllegalArgumentException ex) {
            log.error("::{}::获取综合操盘联赛模板跳分设置:{}", CommonUtil.getRequestId(param.getTournamentId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::获取综合操盘联赛模板跳分设置:{}", CommonUtil.getRequestId(param.getTournamentId()), ex.getMessage(), ex);
            return HttpResponse.error(500, ex.getMessage());
        }
    }

    /**
     * 保存综合操盘联赛模板跳分设置
     *
     * @author carver
     * @date 2021-9-29
     */
    @PostMapping("/saveTournamentTemplateJumpConfig")
    @LogAnnotion(name = "联赛跳分设置", keys = {"tournamentId", "matchType", "maxSingleBetAmount"}, title = {"联赛id", "操盘类型", "最大投注最大赔付"})
    public HttpResponse saveTournamentTemplateJumpConfig(@RequestBody TournamentTemplateJumpConfigParam param) {
        try {
            log.info("::{}::综合操盘联赛模板跳分设置-入参:{}，操盘手:{}",CommonUtil.getRequestId(param.getTournamentId()), JSONObject.toJSONString(param),TradeUserUtils.getUserIdNoException());
            Assert.notNull(param.getSportId(), "运动类型不能为空");
            Assert.notNull(param.getMatchType(), "盘口类型不能为空");
            Assert.notNull(param.getTournamentId(), "联赛id不能为空");
            tournamentTemplateJumpConfigService.saveTournamentTemplateJumpConfig(param);
        } catch (IllegalArgumentException ex) {
            log.error("::{}::联赛跳分设置:{}", CommonUtil.getRequestId(param.getTournamentId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::联赛跳分设置:{}", CommonUtil.getRequestId(param.getTournamentId()), ex.getMessage(), ex);
            return HttpResponse.fail(ex.getMessage());
        }
        return HttpResponse.success();
    }

    /**
     * 清空赛事水差
     *
     * @author carver
     * @date 2021-9-29
     */
    @PostMapping("/clearMatchWater")
    @LogAnnotion(name = "清空赛事水差", keys = {"matchId"}, title = {"赛事管理id"})
    public HttpResponse clearMatchWater(@RequestBody RcsMatchMarketConfig config) {
        log.info("::{}::清空赛事水差-入参:{}，操盘手:{}",CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), JSONObject.toJSONString(config),TradeUserUtils.getUserIdNoException());
        try {
            Assert.notNull(config.getMatchId(), "赛事管理id不能为空");
            tournamentTemplateJumpConfigService.clearMatchWater(config);
        } catch (IllegalArgumentException ex) {
            log.error("::{}::清空赛事水差:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::清空赛事水差:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), ex.getMessage(), ex);
            return HttpResponse.fail("服务异常");
        }
        return HttpResponse.success();
    }

    /**
     * 综合操盘初始化联赛跳分机制数据
     *
     * @author carver
     * @date 2020-10-03
     */
    @GetMapping("/initTournamentJump")
    public HttpResponse initTournamentJump(Long sportId, Long tournamentId) {
        try {
            tournamentTemplateJumpConfigService.initTournamentJump(sportId, tournamentId);
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::综合操盘初始化联赛跳分机制数据:{}", CommonUtil.getRequestId(tournamentId), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::综合操盘初始化联赛跳分机制数据:{}", CommonUtil.getRequestId(tournamentId), ex.getMessage(), ex);
            return HttpResponse.fail(ex.getMessage());
        }
    }

    /**
     * 特殊抽水数据初始化
     *
     * @author kir
     * @date 2022-02-05
     */
    @GetMapping("/initTournamentSpecialOddsInterval")
    public HttpResponse initTournamentSpecialOddsInterval() {
        try {
            tournamentTemplateJumpConfigService.initTournamentSpecialOddsInterval();
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::特殊抽水数据初始化:{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::特殊抽水数据初始化:{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.fail(ex.getMessage());
        }
    }
    /**
     * 特殊抽水数据初始化，高赔：单注保底投注限额
     *
     * @author forever
     * @date 2022-06-29
     */
    @GetMapping("/initTournamentSpecialBettingIntervalHigh")
    public HttpResponse initTournamentSpecialBettingIntervalHigh() {
        try {
            tournamentTemplateJumpConfigService.initTournamentSpecialBettingIntervalHigh();
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::特殊抽水数据初始化，高赔：单注保底投注限额:{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::特殊抽水数据初始化，高赔：单注保底投注限额:{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.fail(ex.getMessage());
        }
    }

    /**
     * 初始化非自己操盘赛种的赔率变动范围数据
     *
     * @author kir
     * @date 2022-03-05
     */
    @GetMapping("/initMTSOddsChangeValue")
    public HttpResponse initMTSOddsChangeValue() {
        try {
            tournamentTemplateJumpConfigService.initMTSOddsChangeValue();
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::初始化非自己操盘赛种的赔率变动范围数据:{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::初始化非自己操盘赛种的赔率变动范围数据:{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.fail(ex.getMessage());
        }
    }

    /**
     * 赔率接拒变动范围初始化
     *
     * @author kir
     * @date 2022-03-04
     */
    @GetMapping("/initOddsChangeValue")
    public HttpResponse initOddsChangeValue() {
        try {
            tournamentTemplateJumpConfigService.initOddsChangeValue();
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::赔率接拒变动范围初始化:{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::赔率接拒变动范围初始化:{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.fail(ex.getMessage());
        }
    }

    /**
     * 获取联赛操作所有日志
     *
     * @author carver
     * @date 2020-10-03
     */
//    @PostMapping("/getTournamentTemplateLogs")
//    public HttpResponse<List<LogVo>> getTournamentTemplateLogs(@RequestBody UpdateTournamentTemplateParam param) {
//        try {
//            List<LogVo> standardSportTournamentLog = tournamentTemplateService.getStandardSportTournamentLog(param);
//            return HttpResponse.success(standardSportTournamentLog);
//        } catch (Exception ex) {
//            log.error("::{}::{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
//            return HttpResponse.fail("日志获取失败:" + ex.getMessage());
//        }
//    }

    /**
     * 增加百家陪权重配置
     *
     * @author kir
     * @date 2022-02-05
     */
    @PostMapping("/addBaiJiaPaiWeight")
    public HttpResponse addBaiJiaPaiWeight(@RequestBody JSONObject jsonObject) {
        try {
            tournamentTemplateJumpConfigService.addBaiJiaPaiWeight(jsonObject);
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::增加百家陪权重配置:{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::增加百家陪权重配置:{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.fail(ex.getMessage());
        }
    }

    /**
     * 增加数据源配置
     *
     * @return
     */
    @GetMapping("/addDataSourceCode")
    public HttpResponse addDataSourceCode() {
        try {
            tournamentTemplateJumpConfigService.addDataSourceCode();
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::增加数据源配置:{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::增加数据源配置:{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.fail(ex.getMessage());
        }
    }


    /**
     * 增加数据源配置通用方法往后加
     * @param key
     * @param val
     * @return
     */
    @PostMapping("/addDataSourceCodeCommon")
    public HttpResponse addDataSourceCodeCommon(@RequestParam("key") String key, @RequestParam("val")Integer val) {
        try {
            tournamentTemplateJumpConfigService.addDataSourceCodeCommon(key, val);
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::增加数据源配置通用方法往后加:{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::增加数据源配置通用方法往后加:{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.fail(ex.getMessage());
        }
    }

    /**
     * 增加玩法集配置数据
     *
     * @return
     */
    @GetMapping("/addTemplateAcceptConfigAutoChange")
    public HttpResponse addTemplateAcceptConfigAutoChange() {
        try {
            tournamentTemplateJumpConfigService.addTemplateAcceptConfigAutoChange();
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            log.error("::{}::增加玩法集配置数据:{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::增加玩法集配置数据:{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.fail(ex.getMessage());
        }
    }


    /**
     * 修改模板配置参数
     */
    @PostMapping("/updateTemplateAoConfigData")
    public HttpResponse updateTemplateAoConfigData() {
        try {
            tournamentTemplateJumpConfigService.updateTemplateAoConfigData();
            return HttpResponse.success();
        } catch (IllegalArgumentException ex) {
            return HttpResponse.error(201, ex.getMessage());
        } catch (Exception ex) {
            log.error("::{}::修改模板配置参数:{}", CommonUtil.getRequestId(), ex.getMessage(), ex);
            return HttpResponse.fail(ex.getMessage());
        }
    }
}
