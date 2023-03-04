package com.panda.sport.rcs.trade.wrapper.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.panda.common.fmt.LogFormat;
import com.panda.sport.data.rcs.api.CategoryListService;
import com.panda.sport.data.rcs.api.Response;
import com.panda.sport.data.rcs.dto.CategoryDTO;
import com.panda.sport.data.rcs.dto.MarketCategoryCetBean;
import com.panda.sport.rcs.common.CategorySetMargin;
import com.panda.sport.rcs.constants.Placeholder;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.constants.RedisKeys;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.db.annotation.Master;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.Basketball;
import com.panda.sport.rcs.enums.CategoryTemplateEnum;
import com.panda.sport.rcs.enums.Football;
import com.panda.sport.rcs.enums.MarketCategoryEnum;
import com.panda.sport.rcs.enums.MarketKindEnum;
import com.panda.sport.rcs.enums.OddsTypeEnum;
import com.panda.sport.rcs.enums.SportTypeEnum;
import com.panda.sport.rcs.enums.TradeEnum;
import com.panda.sport.rcs.enums.TradeStatusEnum;
import com.panda.sport.rcs.enums.TradeTypeEnum;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.factory.BeanFactory;
import com.panda.sport.rcs.mapper.MarketCategorySetMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketCategoryMapper;
import com.panda.sport.rcs.mongo.CategoryCollection;
import com.panda.sport.rcs.mongo.CategorySetOrderNo;
import com.panda.sport.rcs.mongo.I18nBean;
import com.panda.sport.rcs.mongo.MarketCategory;
import com.panda.sport.rcs.mongo.MarketConfigMongo;
import com.panda.sport.rcs.mongo.MatchMarketLiveBean;
import com.panda.sport.rcs.mongo.MatchMarketOddsVo;
import com.panda.sport.rcs.mongo.MatchMarketVo;
import com.panda.sport.rcs.mongo.MatchTeamVo;
import com.panda.sport.rcs.mongo.TemplateTitle;
import com.panda.sport.rcs.pojo.RcsLanguageInternation;
import com.panda.sport.rcs.pojo.RcsMarketCategorySet;
import com.panda.sport.rcs.pojo.RcsMarketCategorySetMargin;
import com.panda.sport.rcs.pojo.RcsMarketCategorySetRelation;
import com.panda.sport.rcs.pojo.StandardSportMarketCategory;
import com.panda.sport.rcs.pojo.StandardSportMarketCategoryRefReqVo;
import com.panda.sport.rcs.pojo.statistics.RcsPredictBetOdds;
import com.panda.sport.rcs.trade.util.RandomNumber;
import com.panda.sport.rcs.trade.wrapper.CategoryService;
import com.panda.sport.rcs.trade.wrapper.MarketCategorySetService;
import com.panda.sport.rcs.trade.wrapper.MarketStatusService;
import com.panda.sport.rcs.trade.wrapper.MatchSetMongoService;
import com.panda.sport.rcs.trade.wrapper.RcsLanguageInternationService;
import com.panda.sport.rcs.trade.wrapper.RcsMarketCategorySetMarginService;
import com.panda.sport.rcs.trade.wrapper.RcsMarketCategorySetRelationService;
import com.panda.sport.rcs.trade.wrapper.RcsOddsConvertMappingService;
import com.panda.sport.rcs.trade.wrapper.RcsPredictBetOddsService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketCategoryService;
import com.panda.sport.rcs.utils.OddsConvertUtils;
import com.panda.sport.rcs.utils.PlayTemplateUtils;
import com.panda.sport.rcs.vo.CategoryTemplateVo;
import com.panda.sport.rcs.vo.I18nItemVo;
import com.panda.sport.rcs.vo.MarketCategoryQueryVO;
import com.panda.sport.rcs.vo.MarketCategorySetResVO;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.shaded.com.google.common.collect.Lists;
import org.apache.dubbo.config.annotation.Reference;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Felix
 */
@Slf4j
@Service
public class MarketCategorySetServiceImpl extends ServiceImpl<MarketCategorySetMapper, RcsMarketCategorySet> implements MarketCategorySetService {
    Logger logger = log;
    @Autowired
    MarketCategorySetMapper marketCategorySetMapper;

    @Reference(retries = 3, lazy = true, check = false)
    CategoryListService categoryListService;

    @Autowired
    RcsMarketCategorySetRelationService rcsMarketCategorySetRelationService;
    @Autowired
    RedisClient redisClient;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RcsLanguageInternationService rcsLanguageInternationService;

    @Autowired
    RcsMarketCategorySetMarginService rcsMarketCategorySetMarginService;
    //玩法集名称最大输入字符30
    private final static int NAME_MAX_LENGTH = 30;
    //备注长度不超过130个字符。
    private final static int REMARKS_MAX_LENGTH = 130;

    @Autowired
    private StandardSportMarketCategoryMapper standardSportMarketCategoryMapper;

    @Autowired
    private StandardSportMarketCategoryService standardSportMarketCategoryService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    MarketStatusService marketStatusService;

    @Autowired
    private MatchSetMongoService matchSetMongoService;

    @Autowired
    private RcsPredictBetOddsService predictBetOddsService;

    @Autowired
    private RcsOddsConvertMappingService mappingService;

    @Override
    public List<Long> getMatrixPlayIdList(Long sportId, Long matchStage) {
//        List<StandardSportMarketCategory> list = standardSportMarketCategoryMapper.queryCategoryList(String.valueOf(sportId));
//        list = getCategoryList(list);
//        return list.stream().filter(bean -> {
//            if (matchStage == 1) {
//                return bean.getTheirTime() != null && bean.getTheirTime() == 3;
//            }
//            if (matchStage == 2) {
//                return bean.getTheirTime() != null && (bean.getTheirTime() == 1 || bean.getTheirTime() == 2);
//            }
//            return false;
//        }).map(StandardSportMarketCategory::getId).collect(Collectors.toList());
        //玩法固定   和 矩阵一直
        if (matchStage == 1) {
            return Arrays.asList(1L, 4L, 2L, 28L, 12L, 7L, 135L, 68L, 14L, 10L, 11L, 8L, 9L, 136L, 15L, 3L, 104L, 6L, 5L, 27L, 13L, 101L, 102L, 109L, 110L,
                    16L, 103L, 31L, 148L, 107L, 108L, 141L, 223L, 149L, 77L, 91L, 78L, 92L, 81L, 79L, 82L, 80L, 83L, 93L, 84L, 94L, 85L, 95L, 86L, 96L, 340L, 344L, 345L, 346L, 347L, 348L, 349L, 350L, 351L, 352L, 353L, 360L, 354L, 355L, 356L, 357L, 358L, 363L, 364L, 365L, 366L, 361L, 362L);
        }
        if (matchStage == 2) {
            return Arrays.asList(17L, 19L, 18L, 20L, 341L, 87L, 97L, 23L, 42L, 43L, 70L, 69L, 30L, 29L, 21L, 22L, 90L, 100L, 24L, 105L);
        }
        return Arrays.asList(-1L);
    }

    /**
     * 玩法集列表
     *
     * @return
     */
    @Override
    @Master
    public List<RcsMarketCategorySet> findCategorySetList(RcsMarketCategorySet rcsMarketCategorySet) {
        List<RcsMarketCategorySet> categorySetList = marketCategorySetMapper.findCategorySetList(rcsMarketCategorySet);
        //根据编码，获取多语言
        List<String> nameCodes = categorySetList.stream().map(RcsMarketCategorySet::getNameCode).filter(nameCode -> nameCode != null).collect(Collectors.toList());
        List<RcsLanguageInternation> language = rcsLanguageInternationService.getLanguageInternationByCode(nameCodes);
        Map<String, String> languageMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(language)) {
            languageMap = language.stream().collect(Collectors.toMap(RcsLanguageInternation::getNameCode, o -> o.getText() == null ? "0" : o.getText()));
        }

        for (RcsMarketCategorySet model : categorySetList) {
            //玩法集ID
            Long id = model.getId();
            //根据玩法集ID，获取新增设置的分时margin
            List<RcsMarketCategorySetMargin> margin = rcsMarketCategorySetMarginService.findMargin(id);
            Collections.sort(margin, (a, b) -> b.getTimeFrame().compareTo(a.getTimeFrame()));
            if (!CollectionUtils.isEmpty(margin)) {
                LinkedHashMap<String, RcsMarketCategorySetMargin> marginMap = new LinkedHashMap<>();
                margin.forEach(mm -> {
                    marginMap.put(CategorySetMargin.getTimeFrame(mm.getTimeFrame()), mm);
                });
                //玩法集margin赋值
                model.setMargin(marginMap);
            }

            //设置多语言
            if (StringUtils.isNotBlank(model.getNameCode()) && languageMap.size() > 0) {
                String obj = languageMap.get(model.getNameCode());
                JSONObject jsonObject = JSONObject.parseObject(obj);
                model.setLanguage(jsonObject);
            } else {
                Map<String, String> a = Maps.newHashMap();
                a.put("zs", model.getName());
                model.setLanguage(a);
            }
        }
        return categorySetList;
    }

    @Override
    public List<StandardSportMarketCategoryRefReqVo> findMarketCategoryListForSoccer() {
        List<StandardSportMarketCategoryRefReqVo> marketCategoryListForSoccer = standardSportMarketCategoryMapper.findMarketCategoryListForSoccer();

        //所有玩法id根据矩阵需求分类
        Integer[] int1 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 27, 28, 31, 68, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 91, 92, 93, 94, 95, 96, 101, 102, 103, 104, 107, 108, 109, 110, 135, 136, 141, 148, 149, 223, 340,344, 345, 346, 347, 348, 349, 350, 351, 353, 360, 352, 354, 355, 356, 357, 358, 352, 363, 364, 365, 366, 361, 362};

        Integer[] int2 = {17, 18, 19, 20, 341, 21, 22, 23, 24, 29, 30, 42, 43, 69, 70, 87, 90, 97, 100, 105, 359};
        Integer[] int3 = {25, 26, 71, 72, 73, 74, 342, 75, 76, 88, 89, 98, 99, 106, 142, 143};
        Integer[] int4 = {111, 112, 113, 114, 115, 116, 117, 118, 225, 226, 227};
        Integer[] int5 = {119, 121, 122, 123, 124, 228, 229};
        Integer[] int6 = {126, 127, 128, 234, 236, 343};
        Integer[] int7 = {129, 130};
        Integer[] int8 = {132, 134, 238, 239, 240, 241};
        List<Integer> list1 = Arrays.asList(int1);
        List<Integer> list2 = Arrays.asList(int2);
        List<Integer> list3 = Arrays.asList(int3);
        List<Integer> list4 = Arrays.asList(int4);
        List<Integer> list5 = Arrays.asList(int5);
        List<Integer> list6 = Arrays.asList(int6);
        List<Integer> list7 = Arrays.asList(int7);
        List<Integer> list8 = Arrays.asList(int8);

        //获取玩法列表的nameCode,然后国际话处理
        List<Long> nameCodeList = new ArrayList<>();
        for (StandardSportMarketCategoryRefReqVo vo : marketCategoryListForSoccer) {
            nameCodeList.add(vo.getNameCode());

            //为各玩法ID进行矩阵分类
            if (list1.contains(vo.getCategoryId())) {
                vo.setType(1);
            }
            if (list2.contains(vo.getCategoryId())) {
                vo.setType(2);
            }
            if (list3.contains(vo.getCategoryId())) {
                vo.setType(3);
            }
            if (list4.contains(vo.getCategoryId())) {
                vo.setType(4);
            }
            if (list5.contains(vo.getCategoryId())) {
                vo.setType(5);
            }
            if (list6.contains(vo.getCategoryId())) {
                vo.setType(6);
            }
            if (list7.contains(vo.getCategoryId())) {
                vo.setType(7);
            }
            if (list8.contains(vo.getCategoryId())) {
                vo.setType(8);
            }
        }
        Map<String, List<I18nItemVo>> langMap = rcsLanguageInternationService.getCachedNamesByCode(nameCodeList, false);
        for (StandardSportMarketCategoryRefReqVo vo : marketCategoryListForSoccer) {
            List<I18nItemVo> langList = new ArrayList<>();
            if (!langMap.isEmpty() && vo.getNameCode() != null) {
                langList = langMap.get(vo.getNameCode().toString());
            }
            if (langList != null) {
                Map<String, Object> languageMap = langList.stream().collect(Collectors.toMap(I18nItemVo::getLanguageType, (b) -> b));
                vo.setLanguage(new TreeMap<>(languageMap).descendingMap());
                vo.setOrderNo(0);
            }
        }

        return marketCategoryListForSoccer;
    }

    /**
     * 业务模块需要风控提供查询展示型玩法集接口
     *
     * @param
     * @return
     */
    @Override
    @Master
    public List<RcsMarketCategorySet> findCategorySetSyncList(MarketCategoryCetBean marketCategoryCetBean) {
        QueryWrapper<RcsMarketCategorySet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type", 0);
        queryWrapper.ne("status", 3);
        queryWrapper.eq("sport_id", marketCategoryCetBean.getSportId());
        queryWrapper.orderByAsc("order_no");
        return this.list(queryWrapper);
    }

    /**
     * @return com.baomidou.mybatisplus.core.metadata.IPage
     * @Description //TODO
     * @Param [rcsMarketCategorySet, current, size]
     * @Author kimi rcsMarketCategorySet 查询条件   current 分页第几页  size]分页大小
     * @Date 2020/2/14
     **/
    @Override
    @Master
    public IPage findPageCategorySetList(RcsMarketCategorySet rcsMarketCategorySet, int current, int size) {
        Page<RcsMarketCategorySet> pageParam = new Page<>(current, size);
        IPage<RcsMarketCategorySet> pageResult = marketCategorySetMapper.findPageCategorySetList(pageParam, rcsMarketCategorySet);
        List<RcsMarketCategorySet> pageResultList = pageResult.getRecords();
        logger.info("查询玩法集结果:" + JSONObject.toJSONString(pageResult));
        //根据编码，获取多语言
        List<String> nameCodes = pageResultList.stream().map(RcsMarketCategorySet::getNameCode).filter(nameCode -> nameCode != null).collect(Collectors.toList());
        List<RcsLanguageInternation> language = rcsLanguageInternationService.getLanguageInternationByCode(nameCodes);
        Map<String, String> languageMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(language)) {
            languageMap = language.stream().collect(Collectors.toMap(RcsLanguageInternation::getNameCode, o -> o.getText() == null ? "0" : o.getText()));
        }

        //保存玩法集id
        List<Long> categorySetIds = new ArrayList<>();
        //玩法集首页玩法二级目录
        for (RcsMarketCategorySet setvo : pageResultList) {
            Integer setId = setvo.getId() != null ? setvo.getId().intValue() : 0;
            //获取玩法集下面的玩法
            List<StandardSportMarketCategory> categoryList = findMarketCategoryContent(setId);
            setvo.setCategoryList(categoryList);
            categorySetIds.add(setvo.getId());

            //设置多语言
            if (StringUtils.isNotBlank(setvo.getNameCode()) && languageMap.size() > 0) {
                String obj = languageMap.get(setvo.getNameCode());
                JSONObject jsonObject = JSONObject.parseObject(obj);
                setvo.setLanguage(jsonObject);
            } else {
                Map<String, String> a = Maps.newHashMap();
                a.put("zs", setvo.getName());
                setvo.setLanguage(a);
            }
        }

        //分时margin
        List<RcsMarketCategorySetMargin> findMargin = rcsMarketCategorySetMarginService.findMargin(categorySetIds);
        logger.info("查询Margin结果:" + JSONArray.toJSONString(findMargin));
        for (RcsMarketCategorySet setvo : pageResultList) {
            if (CollectionUtils.isEmpty(findMargin)) {
                break;
            }
            Collections.sort(findMargin, (a, b) -> b.getTimeFrame().compareTo(a.getTimeFrame()));
            LinkedHashMap<String, RcsMarketCategorySetMargin> marginMap = new LinkedHashMap<>();
            findMargin.forEach(margin -> {
                if (setvo.getId().longValue() == margin.getMarketCategorySetId().longValue()) {
                    marginMap.put(CategorySetMargin.getTimeFrame(margin.getTimeFrame()), margin);
                }
            });
            setvo.setMargin(marginMap);
        }
        //初始化风控型玩法数据
//        cacheWindControlTypeAll(false);
        return pageResult;
    }

    /**
     * 批量编辑玩法集
     *
     * @param rcsMarketCategorySetList
     */
    @Override
    public Response updateCategorySetList(List<RcsMarketCategorySet> rcsMarketCategorySetList) {
        logger.info("updateCategorySetList:start");
        for (RcsMarketCategorySet rcsMarketCategorySet : rcsMarketCategorySetList) {
            String name = rcsMarketCategorySet.getName();
            if (!StringUtil.isNullOrEmpty(name) && name.length() > 30) {
                logger.info(LogFormat.moduleLinkLogFmt("panda-rcs", "", "", "", ""));
                throw new RcsServiceException("输入超限");
            }
        }
        saveOrUpdateBatch(rcsMarketCategorySetList);
        logger.info("updateCategorySetList:end");
        return Response.success();
    }


    private List<StandardSportMarketCategory> getCategoryList(List<StandardSportMarketCategory> sources) {
        List<CategoryDTO> list = categoryListService.getMatrixCategoryList(0);
        List<StandardSportMarketCategory> dest = sources.stream().filter(source -> {
            for (CategoryDTO dto : list) {
                if (dto.getId().intValue() == source.getId().intValue()) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());
        return dest;
    }

    /**
     * 玩法列表
     *
     * @param standardSportMarketCategory
     * @return
     */
    @Override
    @Master
    public List<StandardSportMarketCategory> findStandardSportMarketCategoryList(StandardSportMarketCategory standardSportMarketCategory) {
        //取出所有风控类型的玩法
//        LinkedHashMap<String, Object> displayStyle = new LinkedHashMap<>();
//        if (redisClient.exist(RedisKeys.RCSCACHE_WIND_CONTROL_TYPE_CATEGORY)) {
//            displayStyle = (LinkedHashMap<String, Object>) redisClient.hGetAll(RedisKeys.RCSCACHE_WIND_CONTROL_TYPE_CATEGORY, Map.class);
//        }
        //默认足球
        if (standardSportMarketCategory.getSportId() == null) standardSportMarketCategory.setSportId(1l);

        List<StandardSportMarketCategory> categoryList = new ArrayList<>();
        //从数据库取玩法 并加入缓存
        List<StandardSportMarketCategory> marketCategoryList = standardSportMarketCategoryMapper.queryCategoryList(String.valueOf(standardSportMarketCategory.getSportId()));
        //矩阵玩法，不需要显示所有玩法，请别再注释这块代码

        if ("matrix".equalsIgnoreCase(standardSportMarketCategory.getType())) {
            marketCategoryList = getCategoryList(marketCategoryList);
        }

        Collections.sort(marketCategoryList, new Comparator<StandardSportMarketCategory>() {

            @Override
            public int compare(StandardSportMarketCategory o1, StandardSportMarketCategory o2) {
                if (o1.getOrderNo() == null) o1.setOrderNo(Integer.MAX_VALUE);
                if (o2.getOrderNo() == null) o2.setOrderNo(Integer.MAX_VALUE);
                return o1.getOrderNo().intValue() - o2.getOrderNo().intValue();
            }
        });
        //获取玩法列表的nameCode,然后国际话处理
        List<String> nameCodeList = new ArrayList<>();
        for (StandardSportMarketCategory vo : marketCategoryList) {
            nameCodeList.add(String.valueOf(vo.getNameCode()));
        }
        List<RcsLanguageInternation> rcsLanguageInternations = rcsLanguageInternationService.getLanguageInternationByCode(nameCodeList);
//        Map<String, List<I18nItemVo>> langMap = rcsLanguageInternationService.getLanguageInternationByCode(nameCodeList);
        for (StandardSportMarketCategory vo : marketCategoryList) {
//            String key = String.format("%s_%s", standardSportMarketCategory.getSportId(), vo.getId().toString());
//            if (!redisClient.exist(RedisKeys.RCSCACHE_MARKETODDS_MARKET_CATEGORY+ key)) {
//                redisClient.set(RedisKeys.RCSCACHE_MARKETODDS_MARKET_CATEGORY+ key, vo);
//            }
            List<I18nItemVo> langList = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(rcsLanguageInternations) && vo.getNameCode() != null) {
                List<RcsLanguageInternation> collect = rcsLanguageInternations.stream().filter(o -> o.getNameCode().equals(vo.getNameCode().toString())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(collect)) {
                    for (RcsLanguageInternation internation : collect) {
                        I18nItemVo i18nItemVo = new I18nItemVo();
                        i18nItemVo.setLanguageType(internation.getText());
                        langList.add(i18nItemVo);
                    }
                }
            }
            if (langList != null) {
                // 组装国际化
                vo.setLanguage(buildLanguage(langList));
                vo.setOrderNo(0);
                categoryList.add(vo);
            }
        }
        return categoryList;
    }


    private Map<String, Object> buildLanguage(List<I18nItemVo> langList) {
        String languageMap = langList.get(0).getLanguageType();
        JSONObject jsonObject = JSONObject.parseObject(languageMap);
        List<I18nItemVo> languageTypeList = new ArrayList<>();
        I18nItemVo enMap = new I18nItemVo();
        enMap.setLanguageType("en");
        enMap.setText(jsonObject.getString("en"));

        I18nItemVo zhMap = new I18nItemVo();
        zhMap.setLanguageType("zh");
        zhMap.setText(jsonObject.getString("zh"));

        I18nItemVo zsMap = new I18nItemVo();
        zsMap.setLanguageType("zs");
        zsMap.setText(jsonObject.getString("zs"));
        languageTypeList.add(enMap);
        languageTypeList.add(zsMap);
        languageTypeList.add(zhMap);
        Map<String, Object> languageTypeMap = languageTypeList.stream().collect(Collectors.toMap(I18nItemVo::getLanguageType, (b) -> b));
        NavigableMap<String, Object> stringObjectNavigableMap = new TreeMap<>(languageTypeMap).descendingMap();
        return stringObjectNavigableMap;
    }

    /**
     * 玩法内容
     *
     * @param id
     * @return
     */
    @Override
    @Master
    public List<StandardSportMarketCategory> findMarketCategoryContent(Integer id) {
        List<StandardSportMarketCategory> result = new ArrayList<>();
        Long categorySet = id != null ? id.longValue() : 0L;
        //根据玩法集id查询出玩法id
        logger.info("玩法集id：" + categorySet);
        List<Map<String, Object>> getCategoryId = findPandaMarketCategoryId(categorySet);
        logger.info("玩法集查询数据库中的玩法：" + JSONObject.toJSONString(getCategoryId));
        if (getCategoryId.size() < 1) {
            return result;
        }
//        //start
        List<Map<String, Object>> languageMap = marketCategorySetMapper.findLanguageByCategorySetId(categorySet);
        List<Map<String, Object>> languageMap2 = marketCategorySetMapper.findLanguageByCategorySetId2(categorySet);
//        //end

        //查询出redis里的玩法
        for (Map<String, Object> categoryId : getCategoryId) {
            if (categoryId == null) {
                log.warn("::{}::查找categoryId不存在:" + categoryId,id);
                continue;
            }
            /*String key =   String.format("%s_%s", String.valueOf(categoryId.get("sport_id")),categoryId.get("marketCategoryId").toString());
            if (!redisClient.hexists(RedisKeys.RCSCACHE_MARKETODDS_MARKET_CATEGORY, key)) {
                log.warn("查找缓存不存在:" +key);
                continue;
            }*/
            //StandardSportMarketCategory marketCategoryObj = (StandardSportMarketCategory) redisClient.hGetObj(RedisKeys.RCSCACHE_MARKETODDS_MARKET_CATEGORY, key, StandardSportMarketCategory.class);
            StandardSportMarketCategory marketCategoryObj = standardSportMarketCategoryService.getCachedMarketCategoryById(Integer.valueOf(categoryId.get("sport_id").toString()), Long.valueOf(categoryId.get("marketCategoryId").toString()));
            //国际化
            if (marketCategoryObj == null) {
                log.warn("::{}::查找国际化不存在:" + marketCategoryObj,id);
                continue;
            }
            marketCategoryObj.setOrderNo((Integer) categoryId.get("orderNo"));

            marketCategoryObj.setRelId(Long.parseLong(categoryId.get("relId").toString()));
            //start
            Map<String, Object> languages = new HashMap<>(1);
            languageMap.forEach(lang -> {
                if (String.valueOf(marketCategoryObj.getId()).equals(String.valueOf(lang.get("playid")))) {
                    JSONObject text;
                    if(!lang.containsKey("text") || ObjectUtil.isEmpty(lang.get("text"))){
                        JSONObject finalJsonObject = new JSONObject();
                        languageMap2.stream().forEach(m->{
                            if(lang.get("playid").equals(m.get("playid")) && lang.get("sport_id").equals(m.get("sport_id"))){
                                if(ObjectUtil.isNotNull(m.get("language_type")) && ObjectUtil.isNotNull(m.get("text"))){
                                    String language_type = m.get("language_type").toString();
                                    String language_text = m.get("text").toString();
                                    finalJsonObject.put(language_type,language_text);
                                }
                            }
                        });
                        text = finalJsonObject;
                    }else{
                        text = JSONObject.parseObject(lang.get("text").toString());
                    }
                    text.keySet().forEach(langType -> {
                        if(StringUtils.isNotBlank(langType)) {
                            HashMap<String, Object> data = new HashMap<String, Object>();
                            data.put("playid", lang.get("playid"));
                            data.put("remark", lang.get("remark"));
                            data.put("sport_id", lang.get("sport_id"));
                            data.put("text",  text.get(langType));
                            data.put("type",  langType);
                            languages.put( langType, data);
                        }
                    });
                }
            });
            //end
            marketCategoryObj.setLanguage(languages);
            result.add(marketCategoryObj);
        }
        result.sort((o1, o2) -> o1.getOrderNo().compareTo(o2.getOrderNo()));
        logger.info("::{}::根据玩法集查询玩法结果：" + JSONObject.toJSONString(result),id);
        return result;
    }

    @Override
    @Master
    public List<Map<String, Object>> findPandaMarketCategoryId(Long id) {
        return marketCategorySetMapper.findPandaMarketCategoryId(id);
    }

    /**
     * 新建玩法集 & 新增玩法内容
     *
     * @param operatingParam
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> addCategorySetAndCategory(Map<String, Object> operatingParam) {
        return manageCategorySet(operatingParam);
    }

    /**
     * 编辑玩法集 & 编辑玩法内容
     *
     * @param operatingParam
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateCategorySetAndCategory(Map<String, Object> operatingParam) {
        return manageCategorySet(operatingParam);
    }

    /**
     * 查询出所有风控类型的玩法id，所在的玩法集id
     *
     * @return
     */
    @Override
    @Master
    public void cacheWindControlTypeAll(boolean reCached) {
        redisClient.delete(RedisKeys.RCSCACHE_WIND_CONTROL_TYPE_CATEGORY);
        List<Map<String, Long>> windControlTypeControl = marketCategorySetMapper.findWindControlTypeAll();
        if (CollectionUtils.isEmpty(windControlTypeControl)) {
            log.warn("Fail to cache wind control category: Can not found any data from DB, return.");
            return;
        }
        for (Map<String, Long> map : windControlTypeControl) {
            // 已经缓存的且不需要更新缓存的跳过，其他情况缓存
            if (!reCached && redisClient.hexists(RedisKeys.RCSCACHE_WIND_CONTROL_TYPE_CATEGORY, map.get("marketCategoryId").toString())) {
                continue;
            }
            redisClient.hSetObj(RedisKeys.RCSCACHE_WIND_CONTROL_TYPE_CATEGORY, map.get("marketCategoryId").toString(), map);
        }
    }

    /*******1.0补丁版本*********/
    /**
     * 批量删除玩法集里的玩法
     *
     * @param id
     * @return
     */
    @Override
    public Map<String, Object> deleteCategorySetContent(List<Long> id) {
        Map<String, Object> resultMap = new HashMap<>(1);
        boolean result = rcsMarketCategorySetRelationService.deleteCategorySetContent(id);
        resultMap.put("result", result);
        return resultMap;
    }

    /**
     * 删除玩法集
     *
     * @param id
     * @return
     */
    @Override
    public Map<String, Object> deleteCategorySet(Long id) {
        Map<String, Object> resultMap = new HashMap<>(1);
        boolean result = false;
        //1.根据玩法集ID解除玩法集与玩法的关系
        boolean removRelation = marketCategorySetMapper.deleteCategorySetRelation(id);
        logger.info(LogFormat.moduleLinkLogFmt("panda-rcs", "", "deleteCategorySet", "删除玩法集", "成功解除玩法集:{" + id + "}关联的所有玩法的关系!"));
        //2.删除玩法集
        boolean removCategorySet = this.removeById(id);
        logger.info(LogFormat.moduleLinkLogFmt("panda-rcs", "", "deleteCategorySet", "删除玩法集", "成功删除玩法集:{" + id + "}"));
        if (removRelation || removCategorySet) {
            result = true;
            resultMap.put("result", result);
        }
        return resultMap;
    }

    @Override
    @Master
    public LinkedHashMap<String, RcsMarketCategorySetMargin> findMargin(MarketCategoryCetBean obj) {
        List<RcsMarketCategorySetMargin> margins = marketCategorySetMapper.findMarginByPlayId(obj.getPlayId(), obj.getSportId());
        LinkedHashMap<String, RcsMarketCategorySetMargin> resultMarginMap = new LinkedHashMap<>(6);
        Collections.sort(margins, (a, b) -> b.getTimeFrame().compareTo(a.getTimeFrame()));
        margins.forEach(margin -> {
            resultMarginMap.put(CategorySetMargin.getTimeFrame(margin.getTimeFrame()), margin);
        });
        return resultMarginMap;
    }


    /*******1.0补丁版本*********/

    /**
     * 处理玩法新增和修改
     * 规则：id不为空为更新数据，id为空创建数据
     *
     * @param operatingParam
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    Map<String, Object> manageCategorySet(Map<String, Object> operatingParam) {
        logger.info("manageCategorySet:start");
        Map<String, Object> resutMap = new HashMap<>(2);
        if (operatingParam.isEmpty()) {
            throw new RcsServiceException("保存数据不允许为空");
        }
        //解析object对象，取出玩法集和玩法数组
        RcsMarketCategorySet rcsMarketCategorySet = JSONArray.parseObject(JSONObject.toJSONString(operatingParam.get("marketCategorySet")), RcsMarketCategorySet.class);
        List<RcsMarketCategorySetRelation> relationList = JSONArray.parseArray(JSONObject.toJSONString(operatingParam.get("categoryList")), RcsMarketCategorySetRelation.class);
        verifyRelationList(relationList, rcsMarketCategorySet);
        //1.创建玩法集，返回新建数据的ID
        rcsMarketCategorySet.setCreateTime(System.currentTimeMillis());
        Long idExist = rcsMarketCategorySet.getId();
        Map<String, Object> checkReslut = checkCategorySetUpdateOrAdd(rcsMarketCategorySet);
        if (!checkReslut.isEmpty()) {
            throw new RcsServiceException();
        }
        //兼容历史数据，从多语言中取中文赋值玩法集名称
        if (ObjectUtil.isNotNull(rcsMarketCategorySet.getLanguage())) {
            Map map = rcsMarketCategorySet.getLanguage();
            if (ObjectUtil.isNotNull(map.get("zs"))) {
                rcsMarketCategorySet.setName(String.valueOf(map.get("zs")));
            }
        }

        //如果没有传入玩法集ID 新增操作
        if (idExist == null) {
            String nameCode = System.currentTimeMillis()+""+ RandomNumber.randomNum4Len1(2)+""+ RandomNumber.randomNum4Len2(2)+""+ RandomNumber.randomNum4Len1(1);
            rcsMarketCategorySet.setNameCode(nameCode);
            rcsMarketCategorySet.setCreateTime(System.currentTimeMillis());
            marketCategorySetMapper.addMarketCategorySet(rcsMarketCategorySet);
            //margin管理
            categorySetMarginManage(rcsMarketCategorySet);
            logger.info(LogFormat.moduleLinkLogFmt("panda-rcs", "", "addMarketCategorySet", "新增玩法集", "success"));
        } else {//如果有传入玩法集ID 更新操作
            //更新前数据校验
            if (rcsMarketCategorySet.getType() == 1 && !checkCategorySetData(rcsMarketCategorySet)) {
                throw new RcsServiceException("其它风控型玩法集下已存在该玩法集下的1到N个玩法");
            }
            //如果有带排序值的展示性玩法集，转风控型玩法集，初始化排序值为：1
            if (rcsMarketCategorySet.getType() == 1) {
                rcsMarketCategorySet.setOrderNo(1);
            }
            //兼容历史数据，编辑玩法集的时候生成编码
            if (StringUtils.isBlank(rcsMarketCategorySet.getNameCode())) {
                String nameCode = System.currentTimeMillis()+""+ RandomNumber.randomNum4Len1(2)+""+ RandomNumber.randomNum4Len2(2)+""+ RandomNumber.randomNum4Len1(1);
                rcsMarketCategorySet.setNameCode(nameCode);
            }
            rcsMarketCategorySet.setModifyTime(System.currentTimeMillis());
            marketCategorySetMapper.updateCategorySet(rcsMarketCategorySet);
            //margin管理
            categorySetMarginManage(rcsMarketCategorySet);
            logger.info(LogFormat.moduleLinkLogFmt("panda-rcs", "", "addMarketCategorySet", "更新玩法集", "success"));
        }
        //更新多语言
        if (ObjectUtil.isNotNull(rcsMarketCategorySet.getLanguage()) && ObjectUtil.isNotNull(rcsMarketCategorySet.getNameCode())) {
            RcsLanguageInternation rcsLanguageInternation = new RcsLanguageInternation();
            rcsLanguageInternation.setNameCode(rcsMarketCategorySet.getNameCode());
            rcsLanguageInternation.setText(JSONObject.toJSONString(rcsMarketCategorySet.getLanguage()));
            List<RcsLanguageInternation> list = Arrays.asList(rcsLanguageInternation);
            rcsLanguageInternationService.batchInsertOrUpdate(list);
        }
        Long categorySetId = rcsMarketCategorySet.getId();
        //2.玩法集增加玩法
        if (relationList.size() > 0) {
            //List<Long> idList = new ArrayList<>();
            for (RcsMarketCategorySetRelation relation : relationList) {
                //玩法集管理玩法 & 初始化创建时间
                relation.setMarketCategorySetId(categorySetId);
                relation.setCreateTime(System.currentTimeMillis());
                //idList.add(relation.getId());
            }
            //往玩法集里添加的玩法是否已经存在其它玩法集了
            rcsMarketCategorySetRelationService.addOrUpdateCategorySetCategory(relationList);
        }
        //返回新创建的玩法集，玩法集下的玩法
        //List<StandardSportMarketCategory> marketCategoryContent = findMarketCategoryContent(new Long(categorySetId).intValue());
        resutMap.put("Response", Response.success());
        logger.info("manageCategorySet:end");
        //更新风控型玩法 到缓存
//        Integer categorySetType = rcsMarketCategorySet.getType();
//        if (categorySetType == 1) {
//            updateWindControlTypeCache(categorySetId, marketCategoryContent);
//        }
        resutMap.put("rcsMarketCategorySet", rcsMarketCategorySet);

        //删除margain缓存
        if (relationList != null && relationList.size() > 0) {
            for (RcsMarketCategorySetRelation relation : relationList) {
                redisClient.hSet(RedisKeys.MARGAIN_CACHE_KEY, String.format("margain_%s_%s", rcsMarketCategorySet.getSportId(), relation.getMarketCategoryId()), "");
            }
        }

        return resutMap;
    }

    /**
     * 玩法集玩法设置赛种校验
     *
     * @param relationList
     * @param rcsMarketCategorySet
     * @return
     */
    private void verifyRelationList(List<RcsMarketCategorySetRelation> relationList, RcsMarketCategorySet rcsMarketCategorySet) {
        if (null == rcsMarketCategorySet.getSportId() || null == rcsMarketCategorySet.getType()) {
            throw new RcsServiceException("入参错误");
        }
        if (0 == rcsMarketCategorySet.getType()) {
            return;
        }
        if (CollectionUtils.isEmpty(relationList)) {
            return;
        }
        List<Long> ids = relationList.stream().map(RcsMarketCategorySetRelation::getMarketCategoryId).collect(Collectors.toList());
        int containPlayFromPlaySet = rcsMarketCategorySetRelationService.isContainPlayFromPlaySet(ids, rcsMarketCategorySet.getSportId(), rcsMarketCategorySet.getType(), rcsMarketCategorySet.getId());
        if (1 == containPlayFromPlaySet) {
            throw new RcsServiceException("玩法已有经玩集被占用");
        }
    }

    /**
     * 更新或新增玩法集时校验玩法集参数
     *
     * @param rcsMarketCategorySet
     * @return
     */
    private Map<String, Object> checkCategorySetUpdateOrAdd(RcsMarketCategorySet rcsMarketCategorySet) {
        Map<String, Object> resutMap = new HashMap<>(1);
        String categorySetName = rcsMarketCategorySet.getName();
        //玩法集名称
        if (StringUtil.isNullOrEmpty(categorySetName.trim())) {
            throw new RcsServiceException("玩法集名称不可以为空");
        }
        Map<String, RcsMarketCategorySetMargin> margin = rcsMarketCategorySet.getMargin();
        //玩法集名称

        //查询修改后的玩法集名称，在同类型玩法集名称中是否存在
        QueryWrapper<RcsMarketCategorySet> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(RcsMarketCategorySet::getName, rcsMarketCategorySet.getName());
        queryWrapper.lambda().eq(RcsMarketCategorySet::getSportId, rcsMarketCategorySet.getSportId());
        queryWrapper.lambda().eq(RcsMarketCategorySet::getType, rcsMarketCategorySet.getType());
        if (rcsMarketCategorySet.getId() != null) {
            //如果是新增操作，ID为空，ID不作为查询条件
            //如果是修改操作，ID作为查询条件
            queryWrapper.lambda().ne(RcsMarketCategorySet::getId, rcsMarketCategorySet.getId());
        }
        List<RcsMarketCategorySet> list = this.list(queryWrapper);
        if (list.size() > 0) {
            throw new RcsServiceException("同类型的玩法集名称不可以取相同的名称");
        }

        //玩法集类型
        if (rcsMarketCategorySet.getType() == null) {
            throw new RcsServiceException("请选择玩法集类型");
        }
        //玩法集名称最大输入字符30
        if (categorySetName.length() > NAME_MAX_LENGTH) {
            throw new RcsServiceException("玩法集名称输入超限");
        }
        //备注.长度不超过130个字符。
        if (!StringUtil.isNullOrEmpty(rcsMarketCategorySet.getRemark()) && rcsMarketCategorySet.getRemark().length() > REMARKS_MAX_LENGTH) {
            throw new RcsServiceException("备注输入超限");
        }
        return resutMap;
    }

    /**
     * 更新玩法集时 数据校验；防止已经存在风控型的玩法，再展示型玩法转换成风控型玩法
     *
     * @param rcsMarketCategorySet
     * @return
     */
    private boolean checkCategorySetData(RcsMarketCategorySet rcsMarketCategorySet) {
        //1.根据玩法集id 获取同样玩法的风控型玩法集Id
        List<Map<String, Long>> categorySetId = marketCategorySetMapper.findIsExistWindControlSet(rcsMarketCategorySet.getId().intValue(), rcsMarketCategorySet.getSportId().intValue());
        if (!CollectionUtils.isEmpty(categorySetId)) {
            //更新数据失败，其它风控型玩法集下已经存在 1或N个玩法
            return false;
        }
        return true;
    }

    //更新风控型玩法 到缓存
    private void updateWindControlTypeCache(Long categorySetId, List<StandardSportMarketCategory> list) {
        for (StandardSportMarketCategory category : list) {
            Map<String, Object> map = new HashMap<>(3);
            map.put("marketCategorySetId", categorySetId);
            map.put("type", category.getType());
            map.put("marketCategoryId", category.getId());
            redisClient.hSetObj(RedisKeys.RCSCACHE_WIND_CONTROL_TYPE_CATEGORY, map.get("marketCategoryId").toString(), map);
        }
    }


    private boolean type(RcsMarketCategorySet rcsMarketCategorySet) {
        if (rcsMarketCategorySet.getType() == 1) {
            Integer level = rcsMarketCategorySet.getTournamentLevel();
            Integer returnRate = rcsMarketCategorySet.getReturnRate();
            if (level == null || returnRate == null) {
                return false;
            }
            boolean levelNonEmpty = level != null || level != 0;
            boolean returnRateNonEmpty = returnRate != null || returnRate != 0;
            if (levelNonEmpty && returnRateNonEmpty) {
                return true;
            }
        }
        return false;
    }

    /**
     * 玩法集margin管理
     *
     * @return
     */
    private boolean categorySetMarginManage(RcsMarketCategorySet rcsMarketCategorySet) {
        Integer type = rcsMarketCategorySet.getType();
        //1.如果是展示型，不新增margin,不更新margin
        //2.查看是否有关联margin,然后删除margin
        if (type == 0) {
            return deleteCategorySetMargin(rcsMarketCategorySet);
        } else if (type == 1) {
            return addOrUpdateCategorySetMargin(rcsMarketCategorySet);
        }
        return false;
    }

    /**
     * 删除margin
     *
     * @param rcsMarketCategorySet
     * @return
     */
    private boolean deleteCategorySetMargin(RcsMarketCategorySet rcsMarketCategorySet) {
        Map<String, RcsMarketCategorySetMargin> margin = rcsMarketCategorySet.getMargin();
        if (null == margin) {
            return true;
        }
        List<Long> marginIds = new ArrayList<>();
        Iterator<Map.Entry<String, RcsMarketCategorySetMargin>> it = margin.entrySet().iterator();
        //获取所有的margin id
        while (it.hasNext()) {
            Map.Entry<String, RcsMarketCategorySetMargin> entry = it.next();
            RcsMarketCategorySetMargin marginVo = entry.getValue();
            if (null != marginVo.getId()) {
                marginIds.add(marginVo.getId());
            }
        }
        if (!marginIds.isEmpty()) {
            //删除margin
            return rcsMarketCategorySetMarginService.deleteMargin(marginIds);
        }
        if (marginIds.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * 新增 or 更新margin
     *
     * @param rcsMarketCategorySet
     * @return
     */
    private boolean addOrUpdateCategorySetMargin(RcsMarketCategorySet rcsMarketCategorySet) {
        Map<String, RcsMarketCategorySetMargin> margin = rcsMarketCategorySet.getMargin();
        if (null == margin) {
            return true;
        }
        Iterator<Map.Entry<String, RcsMarketCategorySetMargin>> ite = margin.entrySet().iterator();
        List<RcsMarketCategorySetMargin> addList = new ArrayList<>();
        List<RcsMarketCategorySetMargin> updateList = new ArrayList<>();
        while (ite.hasNext()) {
            Map.Entry<String, RcsMarketCategorySetMargin> entry = ite.next();
            RcsMarketCategorySetMargin marginVo = entry.getValue();
            //判断更新还是新增
            //1.id为空新增
            //2.id非空为更新
            if (null == marginVo.getId()) {
                marginVo.setMarketCategorySetId(rcsMarketCategorySet.getId());
                marginVo.setCreateTime(System.currentTimeMillis());
                addList.add(marginVo);
            } else {
                marginVo.setModifyTime(System.currentTimeMillis());
                updateList.add(marginVo);
            }
        }
        //新增margin
        if (addList.size() > 0) {
            return rcsMarketCategorySetMarginService.addMargin(addList);
        }
        //修改margin
        if (updateList.size() > 0) {
            return rcsMarketCategorySetMarginService.updateMargin(updateList);
        }
        return false;
    }

    @Override
    public List<MarketCategorySetResVO> list(Long sportId, Long matchId, Integer matchSnapshot) {
        List<RcsMarketCategorySet> list = this.list(new LambdaQueryWrapper<RcsMarketCategorySet>()
                .eq(RcsMarketCategorySet::getSportId, sportId)
                .eq(RcsMarketCategorySet::getType, 1)
                .eq(RcsMarketCategorySet::getStatus, 2)
                .orderByAsc(RcsMarketCategorySet::getDisplaySort));
        List<MarketCategorySetResVO> result = new ArrayList<>(list.size() + 1);
        MarketCategorySetResVO all = new MarketCategorySetResVO();
        all.setCategorySetId(0L);
        all.setSportId(sportId);
        all.setName("全部");
        all.setOrderNo(-1);
        Map<String, String> allNames = Maps.newHashMap();
        allNames.put("zs", "全部");
        allNames.put("en", "All");
        all.setNames(allNames);
        result.add(all);
        if (CollectionUtils.isEmpty(list)) {
            return result;
        }

        // 查询玩法集信息，获取玩法集操盘方式
        Query query = new Query().addCriteria(Criteria.where("matchId").is(matchId).and("sportId").is(sportId));
        List<CategoryCollection> categoryCollectionList = mongoTemplate.find(query, CategoryCollection.class);
        final Map<Long, CategoryCollection> categoryCollectionMap;
        if (!CollectionUtils.isEmpty(categoryCollectionList)) {
            categoryCollectionMap = categoryCollectionList.stream().collect(Collectors.toMap(CategoryCollection::getId, Function.identity()));
        } else {
            categoryCollectionMap = null;
        }

        List<String> nameCodeList = list.stream().map(RcsMarketCategorySet::getNameCode).collect(Collectors.toList());
        List<RcsLanguageInternation> internationalList = rcsLanguageInternationService.getLanguageInternationByCode(nameCodeList);

        Map<String, Map<String, String>> internationalMap;
        if (CollectionUtils.isEmpty(internationalList)) {
            internationalMap = Maps.newHashMap();
        } else {
            internationalMap = internationalList.stream().collect(Collectors.toMap(RcsLanguageInternation::getNameCode, bean -> JsonFormatUtils.fromJson(bean.getText(), Map.class)));
        }

        // 玩法集操盘方式
        boolean isNotEmpty = !CollectionUtils.isEmpty(categoryCollectionMap);
        list.forEach(item -> {
            Long categorySetId = item.getId();
            MarketCategorySetResVO resVO = new MarketCategorySetResVO(item);
            if (isNotEmpty && categoryCollectionMap.containsKey(categorySetId)) {
                resVO.setTradeType(categoryCollectionMap.get(categorySetId).getTradeType());
            } else {
                resVO.setTradeType(TradeTypeEnum.AUTO.getCode());
            }
            if (internationalMap.containsKey(item.getNameCode())) {
                resVO.setNames(internationalMap.get(item.getNameCode()));
            } else {
                Map<String, String> names = Maps.newHashMap();
                names.put("zs", item.getName());
                resVO.setNames(names);
            }
            result.add(resVO);
        });

        if (matchSnapshot == null || matchSnapshot != 1) {
            return result;
        }

        // 赛前十五分钟自动手动
        Map<Long, Integer> categorySetTradeTypeMap = matchSetMongoService.getCategorySetSnapshotTradeType(matchId);
        result.forEach(bean -> {
            Long categorySetId = bean.getCategorySetId();
            if (!CollectionUtils.isEmpty(categorySetTradeTypeMap) && categorySetTradeTypeMap.containsKey(categorySetId)) {
                bean.setTradeType(categorySetTradeTypeMap.get(categorySetId));
            }
        });
        return result;
    }

    @Override
    public CategoryCollection marketDetail(MarketCategoryQueryVO marketCategoryQueryVO) {
        final Long sportId = marketCategoryQueryVO.getSportId();
        final Long matchId = marketCategoryQueryVO.getMatchId();
        final Long categorySetId = marketCategoryQueryVO.getCategorySetId();
        List<Long> mainCategoryIds = categoryService.mainCategoryIds(sportId);
        //篮球需要添加几个特殊的跳水玩法
        if ("2".equals(String.valueOf(sportId))) {
            mainCategoryIds.addAll(Arrays.asList(198L, 199L, 87L, 88L, 97L, 98L, 145L, 146L, 41L));
        }
        MatchMarketLiveBean matchInfo = getMatchInfo(sportId, matchId);
        CategoryCollection categoryCollection = getCategoryCollection(sportId, matchId, categorySetId, matchInfo);
        categoryCollection.setOperateMatchStatus(TradeStatusEnum.OPEN.getStatus());
        if (matchInfo != null && matchInfo.getOperateMatchStatus() != null) {
            categoryCollection.setOperateMatchStatus(matchInfo.getOperateMatchStatus());
        }

        // 是否赛前15分钟
        Integer matchSnapshot = marketCategoryQueryVO.getMatchSnapshot();
        boolean isSnapshot = null != matchSnapshot && 1 == matchSnapshot;
        // 查询前十五分钟设置
        Map<Long, Integer> categoryTradeTypeMap = null;
//        Map<Long, Map<Integer, RcsMatchMarketConfig>> categoryMap = marketConfigService.queryConfigs(matchId);
        List<MarketConfigMongo> marketConfigMongos = new ArrayList<>();
        if (isSnapshot) {
            categoryTradeTypeMap = matchSetMongoService.getCategorySnapshotTradeType(matchId);
            marketConfigMongos = matchSetMongoService.queryMarketConfig(matchId);
        }

        List<MarketCategory> categoryList = categoryCollection.getMarketCategoryList();
        // 球队信息
        Map<String, I18nBean> teamMap = Maps.newHashMap();
        if (matchInfo != null && CollectionUtils.isNotEmpty(matchInfo.getTeamList())) {
            for (MatchTeamVo team : matchInfo.getTeamList()) {
                String position = team.getMatchPosition();
                Map<String, String> names = team.getNames();
                if (StringUtils.isNotBlank(position) && CollectionUtils.isNotEmpty(names)) {
                    teamMap.put(position.toLowerCase(), new I18nBean(names));
                }
            }
        }
        // 玩法模板信息，key=categoryId
        Map<Long, CategoryTemplateVo> categoryTemplateMap = standardSportMarketCategoryService.getCategoryTemplateByCache(sportId, categoryCollection.getCategoryIds());

        // 遍历玩法集合
        for (MarketCategory category : categoryList) {
            Long categoryId = category.getId();
            if (isSnapshot && !CollectionUtils.isEmpty(categoryTradeTypeMap) && categoryTradeTypeMap.containsKey(categoryId)) {
                category.setTradeType(categoryTradeTypeMap.get(categoryId));
            }
//            Map<Integer, RcsMatchMarketConfig> marketConfigMap = null;
//            if (!CollectionUtils.isEmpty(categoryMap)) {
//                marketConfigMap = categoryMap.get(category.getId());
//            }

            if (SportTypeEnum.FOOTBALL.getCode().equals(sportId.intValue()) && "Other".equals(category.getMarketType()))
                category.setMarketType(MarketKindEnum.Europe.getValue());
            if (SportTypeEnum.BASKETBALL.getCode().equals(sportId.intValue()) && "Other".equals(category.getMarketType()))
                category.setMarketType(MarketKindEnum.Malaysia.getValue());
            if (StringUtils.isBlank(category.getMarketType()))
                category.setMarketType(MarketKindEnum.Europe.getValue());
            category.setMain(mainCategoryIds.contains(categoryId));
            MarketKindEnum marketKind = MarketKindEnum.getMarketKindByValue(category.getMarketType());
            // 获取模板ID
            Integer templateId = getTemplateId(matchId, categoryId, categoryTemplateMap);
            category.setTemplateId(templateId);
            category.setIsIrregular(CategoryTemplateEnum.isIrregular(categoryId, templateId) ? 1 : 0);
            category.setShowFlag(PlayTemplateUtils.isShowMarketName(categoryId) ? 1 : 0);
//            BigDecimal margin = NumberUtils.getBigDecimal(category.getMargain());
            List<MatchMarketVo> marketList = category.getMatchMarketVoList();
            if (CollectionUtils.isEmpty(marketList)) {
                log.warn("::{}::玩法下无盘口信息：sportId={}，categoryId={}", matchId, sportId, categoryId);
                continue;
            }

            // 根据玩法id 赛种id 玩法国际化
            category.setNames(rcsLanguageInternationService.getCategoryLanguage(category.getId(), category.getSportId()));
            PlayTemplateUtils.handlePlayName(category, teamMap);

            // 取赛前15分钟状态
            Map<Integer, Integer> snapshotStatusMap = null;
            if (isSnapshot) {
                snapshotStatusMap = marketStatusService.getSnapshotMarketPlaceStatus(matchId, categoryId);
            }
            // 遍历盘口集合
            for (MatchMarketVo market : marketList) {
                if (isSnapshot) {
                    Integer placeNum = market.getPlaceNum();
                    if (!CollectionUtils.isEmpty(snapshotStatusMap) && snapshotStatusMap.containsKey(placeNum)) {
                        market.setStatus(snapshotStatusMap.get(placeNum));
                    }
                    // 获取配置赔率
                    matchSetMongoService.updateMarketConfig(marketConfigMongos, matchId, categoryId, market);
                }
                List<MatchMarketOddsVo> marketOddsList = market.getOddsFieldsList();
                if (CollectionUtils.isEmpty(marketOddsList)) {
                    log.warn("::{}::盘口下无投注项信息：sportId={}，matchId={}，categoryId={}", matchId,sportId,  categoryId);
                    continue;
                }

                // 遍历投注项集合
                for (MatchMarketOddsVo marketOdds : marketOddsList) {
                    // 赔率原始值
                    marketOdds.setFieldOddsOriginValue(Double.valueOf(marketOdds.getFieldOddsValue()).intValue());
                    // 赔率显示值
                    marketOdds.setFieldOddsValue(OddsConvertUtils.convertAndDefaultDisplay(MarketKindEnum.Europe, marketOdds.getFieldOddsOriginValue()));
                    if (MarketKindEnum.Malaysia.getValue().equals(category.getMarketType())) {
                        String myOdds = mappingService.getMyOdds(marketOdds.getFieldOddsValue());
                        if ("0".equals(myOdds))
                            myOdds = mappingService.getOddsValue(marketOdds.getFieldOddsValue(), marketKind);
                        marketOdds.setFieldOddsValue(myOdds);
                    }

                    PlayTemplateUtils.handleMarketOdds(categoryId, templateId, marketOdds, teamMap);
                }

                // 计算margin值
                market.setMarginValue(OddsConvertUtils.calMarginByOddsList(marketOddsList, marketKind));
                PlayTemplateUtils.handleMarketName(market, teamMap);
                // 篮球220L, 221L, 271L, 272L 四个玩法球员名做特殊处理
                if (Basketball.Secondary.PLAYER.getPlayIds().contains(market.getMarketCategoryId())) {
                    String playerLanguageStr = rcsLanguageInternationService.getPlayerLanguageStr(market.getAddition3());
                    market.setAddition3(playerLanguageStr);
                    market.setOddsName(playerLanguageStr);
                }

                // 根据sortNo排序
                List<MatchMarketOddsVo> sortedMarketOddsList = marketOddsList.stream().sorted(Comparator.comparingInt(MatchMarketOddsVo::getOrderOdds)).collect(Collectors.toList());
                market.setOddsFieldsList(sortedMarketOddsList);
            }
            List<MatchMarketVo> markets = marketList.stream().filter(filter -> Arrays.asList(0, 1).contains(filter.getThirdMarketSourceStatus()) ||
                            null != filter.getOddsFieldsList() || filter.getOddsFieldsList().size() > 0)
                    .sorted(Comparator.comparingInt(MatchMarketVo::getPlaceNum))
                    .collect(Collectors.toList());
            if (Basketball.Secondary.PLAYER.getPlayIds().contains(categoryId)) {
                markets = markets.stream().sorted((o1, o2) -> {
                    if (StringUtils.equals(o1.getAddition4(), o2.getAddition4())) {
                        return o1.getPlaceNum().compareTo(o2.getPlaceNum());
                    } else {
                        return o1.getAddition4().compareTo(o2.getAddition4());
                    }
                }).collect(Collectors.toList());
            }
            category.setMatchMarketVoList(markets);
            if (CategoryTemplateEnum.isGroupByColumn(templateId)) {
                // 单盘口按列分组
                groupByColumn(category, teamMap);
            } else if (CategoryTemplateEnum.isSingleGroupByColAndRow(categoryId, templateId)) {
                // 单盘口按列和行分组
                singleGroupByColAndRow(category, teamMap);
            } else if (CategoryTemplateEnum.isMultiGroupByColAndRow(categoryId, templateId)) {
                // 多盘口按列和行分组
                multiGroupByColAndRow(category, teamMap);
            }
            //判断次玩法
            List<MatchMarketVo> childMarketVoList = markets.stream().filter(fi -> StringUtils.isNotBlank(fi.getChildMarketCategoryId())
                    && (!String.valueOf(fi.getMarketCategoryId()).equals(fi.getChildMarketCategoryId()))).collect(Collectors.toList());
            category.setIsChildCategory(CollectionUtils.isNotEmpty(childMarketVoList));
        }
        Map<Long, Map<String, List<RcsPredictBetOdds>>> matchBetMap = predictBetOddsService.queryBetOdds(Arrays.asList(matchId), 1, 1);
        if (!CollectionUtils.isEmpty(matchBetMap)) {
            categoryCollection.setBetMap(matchBetMap.get(matchId));
        }
        return categoryCollection;
    }

    @Override
    public void multiGroupByColAndRow(MarketCategory category, Map<String, I18nBean> teamMap) {
        final Long categoryId = category.getId();

        // 列标题集合
        List<TemplateTitle> rowTitles = new ArrayList<>();
        // 列下标
        int rowIndex = 1;
        // 行下标
        int colIndex = 1;
        // 遍历盘口
        for (MatchMarketVo market : category.getMatchMarketVoList()) {
            List<MatchMarketOddsVo> marketOddsList = market.getOddsFieldsList();
            if (CollectionUtils.isEmpty(marketOddsList)) {
                continue;
            }

            // 行标题，一个盘口一个行标题
            TemplateTitle colTitle = new TemplateTitle().setIndex(colIndex);
            // 遍历投注项集合
            for (MatchMarketOddsVo marketOdds : marketOddsList) {
                if(categoryId == 344L) {
            		//344-多重波胆玩法，主队其他+客队其他不参与新增rowTitle
        			if(OddsTypeEnum.HOME_OTHER.equalsIgnoreCase(marketOdds.getOddsType())
        					||OddsTypeEnum.AWAY_OTHER.equalsIgnoreCase(marketOdds.getOddsType())
//            					||OddsTypeEnum.DRAW_OTHER.equalsIgnoreCase(marketOdds.getOddsType())
        					) {
        				continue;
        			}
            	}
                colTitle.setName(marketOdds.getNameExpressionValue());
                marketOdds.setColIndex(colIndex);

                String titleName = marketOdds.getTitleName();
                int isOther = isOther(categoryId, titleName);
                boolean rowResult = handleRowTitle(marketOdds, rowTitles, rowIndex, titleName, isOther);
                if (rowResult) {
                    rowIndex++;
                }
            }
            replaceTitle(colTitle, teamMap);
            market.setColTitle(colTitle);
            colIndex++;
        }
        replaceTitle(rowTitles, teamMap);
        category.setRowTitles(rowTitles);
    }

    @Override
    public void singleGroupByColAndRow(MarketCategory category, Map<String, I18nBean> teamMap) {
        final Long categoryId = category.getId();

        // 列标题集合
        List<TemplateTitle> rowTitles = new ArrayList<>();
        // 列下标
        int rowIndex = 1;
        // 遍历盘口
        for (MatchMarketVo market : category.getMatchMarketVoList()) {
            List<MatchMarketOddsVo> marketOddsList = market.getOddsFieldsList();
            if (CollectionUtils.isEmpty(marketOddsList)) {
                continue;
            }

            // 行标题集合
            List<TemplateTitle> colTitles = new ArrayList<>();
            // 行下标
            int colIndex = 1;
            // 遍历投注项
            for (MatchMarketOddsVo marketOdds : marketOddsList) {
                if(categoryId == 344L) {
            		//344-多重波胆玩法，主队其他+客队其他不参与新增rowTitle
        			if(OddsTypeEnum.HOME_OTHER.equalsIgnoreCase(marketOdds.getOddsType())
        					||OddsTypeEnum.AWAY_OTHER.equalsIgnoreCase(marketOdds.getOddsType())
//            					||OddsTypeEnum.DRAW_OTHER.equalsIgnoreCase(marketOdds.getOddsType())
        					) {
        				continue;
        			}
            	}
                String oddsType = marketOdds.getOddsType();
                String titleName = marketOdds.getTitleName();
                int isOther = isOther(categoryId, titleName);
                boolean rowResult = handleRowTitle(marketOdds, rowTitles, rowIndex, titleName, isOther);
                if (rowResult) {
                    rowIndex++;
                }

                String colTitleName = irregularTitle(oddsType);
                int isOther2 = isOther(categoryId, colTitleName);
                boolean colResult = handleColTitle(marketOdds, colTitles, colIndex, colTitleName, isOther2);
                if (colResult) {
                    colIndex++;
                }
            }
            // 根据rowIndex分组
            Map<Integer, List<MatchMarketOddsVo>> marketOddsGroupMap = groupByOdds(marketOddsList, categoryId);;
            market.setMarketOddsGroupMap(marketOddsGroupMap);
//            market.setOddsFieldsList(null);

            replaceTitle(colTitles, teamMap);
            market.setColTitles(colTitles);
        }
        replaceTitle(rowTitles, teamMap);
        category.setRowTitles(rowTitles);
    }

    @Override
    public void groupByColumn(MarketCategory category, Map<String, I18nBean> teamMap) {
        final Long categoryId = category.getId();

        // 列标题集合
        List<TemplateTitle> rowTitles = new ArrayList<>();
        // 列下标
        int rowIndex = 1;
        // 遍历盘口
        for (MatchMarketVo market : category.getMatchMarketVoList()) {
            List<MatchMarketOddsVo> marketOddsList = market.getOddsFieldsList();
            if (CollectionUtils.isEmpty(marketOddsList)) {
                continue;
            }

            // 进球球员类玩法
            if (RcsConstant.GOALSCORER.contains(categoryId)) {
                marketOddsList = marketOddsList.stream().sorted(Comparator.comparingInt(MatchMarketOddsVo::getGroupId)).collect(Collectors.toList());
            }

            // 遍历投注项
            for (MatchMarketOddsVo marketOdds : marketOddsList) {
                if(categoryId == 344L) {
            		//344-多重波胆玩法，主队其他+客队其他不参与新增rowTitle
        			if(OddsTypeEnum.AWAY_OTHER.equalsIgnoreCase(marketOdds.getOddsType())) {
        				marketOdds.setRowIndex(2);
        				continue;
        			}
        			if(OddsTypeEnum.HOME_OTHER.equalsIgnoreCase(marketOdds.getOddsType())) {
        				marketOdds.setRowIndex(1);
        				continue;
        			}
            	}
                String titleName = marketOdds.getTitleName();
                int isOther = isOther(categoryId, titleName);
                boolean rowResult = handleRowTitle(marketOdds, rowTitles, rowIndex, titleName, isOther);
                if (rowResult) {
                    rowIndex++;
                }
            }
            // 根据rowIndex分组
            Map<Integer, List<MatchMarketOddsVo>> marketOddsGroupMap = groupByOdds(marketOddsList, categoryId);
            market.setMarketOddsGroupMap(marketOddsGroupMap);
//            market.setOddsFieldsList(null);
        }
        replaceTitle(rowTitles, teamMap);
        category.setRowTitles(rowTitles);
    }
	//344-多重波胆玩法，不单纯按rowIndex进行分组，将主队其他+客队其他+平局其他放入0-1-2分组中，其他玩法正常处理
    private Map<Integer, List<MatchMarketOddsVo>> groupByOdds(List<MatchMarketOddsVo> marketOddsList, Long categoryId){
    	Map<Integer, List<MatchMarketOddsVo>> data = new HashMap<Integer, List<MatchMarketOddsVo>>();
    	if(categoryId == 344L) {
    		//344-多重波胆玩法，不单纯按rowIndex进行分组，将odds
    		for(MatchMarketOddsVo matchMarketOddsVo : marketOddsList) {
    			if(OddsTypeEnum.HOME_OTHER.equalsIgnoreCase(matchMarketOddsVo.getOddsType())) {
    				//将主队其他投注项放入到主队投注项分组
    				if(!data.containsKey(1)) {
    					ArrayList temp = new ArrayList<MatchMarketOddsVo>();
    					temp.add(matchMarketOddsVo);
    					data.put(1, temp);
    				}else {
    					data.get(1).add(matchMarketOddsVo);
    				}
    			}else if(OddsTypeEnum.AWAY_OTHER.equalsIgnoreCase(matchMarketOddsVo.getOddsType())) {
    				//将客队其他投注项放入到主队投注项分组
    				if(!data.containsKey(2)) {
    					ArrayList temp = new ArrayList<MatchMarketOddsVo>();
    					temp.add(matchMarketOddsVo);
    					data.put(2, temp);
    				}else {
    					data.get(2).add(matchMarketOddsVo);
    				}
    			}else if(OddsTypeEnum.DRAW_OTHER.equalsIgnoreCase(matchMarketOddsVo.getOddsType())) {
    				//将平局其他投注项放入到主队投注项分组
    				if(!data.containsKey(3)) {
    					ArrayList temp = new ArrayList<MatchMarketOddsVo>();
    					temp.add(matchMarketOddsVo);
    					data.put(3, temp);
    				}else {
    					data.get(3).add(matchMarketOddsVo);
    				}
    			}else {
    				//其他投注项按照其对应的rowIndex存放
    				if(!data.containsKey(matchMarketOddsVo.getRowIndex())) {
    					ArrayList temp = new ArrayList<MatchMarketOddsVo>();
    					temp.add(matchMarketOddsVo);
    					data.put(matchMarketOddsVo.getRowIndex(), temp);
    				}else {
    					data.get(matchMarketOddsVo.getRowIndex()).add(matchMarketOddsVo);
    				}
    			}
    		}
    	}else {
    		data = marketOddsList.stream().collect(Collectors.groupingBy(MatchMarketOddsVo::getRowIndex));
    	}
    	return data;
    }
    private boolean handleRowTitle(MatchMarketOddsVo marketOdds, List<TemplateTitle> rowTitles, int rowIndex, String rowTitleName, int isOther) {
        if (StringUtils.isBlank(rowTitleName)) {
            marketOdds.setRowIndex(99);
            return false;
        }
        TemplateTitle templateTitle = new TemplateTitle().setName(rowTitleName);
        TemplateTitle rowTitle = null;
        if(OddsTypeEnum.DRAW0.equalsIgnoreCase(marketOdds.getOddsType()) || OddsTypeEnum.DRAW1.equalsIgnoreCase(marketOdds.getOddsType())) {
        	rowTitle =  new TemplateTitle().setName(Placeholder.DRAW).setIndex(3).setIsOther(1);
        	marketOdds.setRowIndex(3);
    		if (OddsTypeEnum.DRAW0.equalsIgnoreCase(marketOdds.getOddsType())) {
    			rowTitles.add(rowTitle);
    			return true;
            }
            for (TemplateTitle title : rowTitles) {
                if (title.getNames() != null && StringUtils.equals(title.getNames().getZs(), templateTitle.getNames().getZs())) {
                    marketOdds.setRowIndex(rowTitle.getIndex());
                    return false;
                }
            }
        }else {
        	rowTitle = existTitle(rowTitles, templateTitle);
        }
        // 标题已存在
        if (rowTitle != null) {
            marketOdds.setRowIndex(rowTitle.getIndex());
            return false;
        }
        rowTitle = templateTitle.setIndex(rowIndex).setIsOther(isOther);
        marketOdds.setRowIndex(rowIndex);
        rowTitles.add(rowTitle);
        return true;
    }

    private boolean handleColTitle(MatchMarketOddsVo marketOdds, List<TemplateTitle> colTitles, int colIndex, String colTitleName, int isOther) {
        if (StringUtils.isBlank(colTitleName)) {
            return false;
        }
        TemplateTitle templateTitle = new TemplateTitle().setName(colTitleName);
        TemplateTitle colTitle = existTitle(colTitles, templateTitle);
        // 标题已存在
        if (colTitle != null) {
            marketOdds.setColIndex(colTitle.getIndex());
            return false;
        }
        colTitle = templateTitle.setIndex(colIndex).setIsOther(isOther);
        marketOdds.setColIndex(colIndex);
        colTitles.add(colTitle);
        return true;
    }

    private int isOther(Long categoryId, String titleName) {
        if (MarketCategoryEnum.isCorrectScore(categoryId)) {
        	if(Placeholder.OTHER.equalsIgnoreCase(titleName)) {
        		return 1;
        	}else if(Placeholder.DRAW_OTHER.equalsIgnoreCase(titleName)) {
        		return 1;
        	}else {
        		return 0;
        	}
        }
        if (MarketCategoryEnum.isWinningMargin(categoryId)) {
            return Placeholder.DRAW.equalsIgnoreCase(titleName) ||Placeholder.DRAW0.equalsIgnoreCase(titleName) ||Placeholder.DRAW1.equalsIgnoreCase(titleName) || Placeholder.OTHER.equalsIgnoreCase(titleName) ? 1 : 0;
        }
        if (MarketCategoryEnum.isGoalscorer(categoryId)) {
            return Placeholder.OTHER.equalsIgnoreCase(titleName) ? 1 : 0;
        }
        return 0;
    }

    private String irregularTitle(String oddsType) {
        if (OddsTypeEnum.WinningMargin.DRAW.getOddsType().equalsIgnoreCase(oddsType)) {
            return Placeholder.DRAW;
        }
        if (OddsTypeEnum.WinningMargin.DRAW0.getOddsType().equalsIgnoreCase(oddsType)) {
            return Placeholder.DRAW0;
        }
        if (OddsTypeEnum.WinningMargin.DRAW1.getOddsType().equalsIgnoreCase(oddsType)) {
            return Placeholder.DRAW1;
        }
        if (OddsTypeEnum.WinningMargin.OTHER.getOddsType().equalsIgnoreCase(oddsType)) {
            return Placeholder.OTHER;
        }
        if (StringUtils.containsIgnoreCase(oddsType, OddsTypeEnum.AND)) {
            String[] array = StringUtils.split(oddsType, OddsTypeEnum.AND);
            if (array != null && array.length == 2) {
                return array[1];
            }
        }
        return "";
    }

    /**
     * 标题列表中是否存在该标题，存在返回该标题，不存在返回null
     *
     * @param rowTitles
     * @param templateTitle
     * @return
     */
    private TemplateTitle existTitle(List<TemplateTitle> rowTitles, TemplateTitle templateTitle) {
        if (CollectionUtils.isEmpty(rowTitles)) {
            return null;
        }
        for (TemplateTitle title : rowTitles) {
            if (title.getNames() != null && StringUtils.equals(title.getNames().getZs(), templateTitle.getNames().getZs())) {
                return title;
            }
        }
        return null;
    }

    private CategoryCollection getCategoryCollection(final Long sportId, final Long matchId, final Long categorySetId, final MatchMarketLiveBean matchInfo) {
        CategoryCollection categoryCollection = new CategoryCollection();
        categoryCollection.setId(categorySetId);
        categoryCollection.setSportId(sportId);
        categoryCollection.setMatchId(matchId);
        List<Long> categoryIdsSet = new ArrayList<>();
        if (categorySetId != null && categorySetId > 0) {
            //足球三项盘
            if (categorySetId.equals(10021L)) {
                List<Long> ids = Football.CategorySet.getCategoryIdsBySetId(null);
                ids.addAll(categoryService.mainCategoryIds(SportTypeEnum.FOOTBALL.getCode().longValue()));
                categoryIdsSet = standardSportMarketCategoryMapper.queryCategoryIds(sportId, ids);
            } else {
//                Integer dataSource = rcsTradeConfigMapper.selectDataSourceByMatchIdAndPlayId(matchId.toString(), categorySetId.toString());
                categoryCollection.setTradeType(TradeEnum.AUTO.getCode());
                categoryIdsSet = rcsMarketCategorySetRelationService.getCategoryIdByCategorySetId(categorySetId);
            }

        } else {
            categoryIdsSet = standardSportMarketCategoryMapper.queryCategoryIds(sportId, null);
        }
        // 过滤占位符玩法
        categoryIdsSet = categoryIdsSet.stream().filter(playId -> !RcsConstant.isPlaceholderPlay(sportId, playId)).collect(Collectors.toList());
        List<MarketCategory> marketCategoryList = getMarketCategoryList(matchId, categoryIdsSet);
        // 过滤占位符玩法
        marketCategoryList = marketCategoryList.stream().filter(play -> !RcsConstant.isPlaceholderPlay(sportId, play.getId())).collect(Collectors.toList());
        categoryCollection.setCategoryIds(categoryIdsSet);
        categoryCollection.setMarketCategoryList(marketCategoryList);
        return categoryCollection;
    }

    private List<MarketCategory> getMarketCategoryList(final Long matchId, final List<Long> categoryIds) {
        Criteria criteria = Criteria.where("matchId").is(matchId.toString());
        if (CollectionUtils.isNotEmpty(categoryIds)) {
            criteria.and("id").in(categoryIds);
        }
        criteria.and("matchMarketVoList.oddsFieldsList.id").gt(0L);
        //数据源展示开、封状态
        criteria.and("matchMarketVoList.thirdMarketSourceStatus").in(Arrays.asList(0, 1));
        Query query = new Query().addCriteria(criteria);
        List<MarketCategory> result = mongoTemplate.find(query, MarketCategory.class);
        if (CollectionUtils.isEmpty(result)) {
            return result;
        }
        List<CategorySetOrderNo> orderNoList = rcsMarketCategorySetRelationService.getCategorySetAndCategoryOrderNo();
        result.forEach(bean -> orderNoList.forEach(order -> {
            if (bean.getId().equals(order.getMarketCategoryId())) {
                Integer displaySort = order.getDisplaySort();
                Integer orderNo = order.getOrderNo();
                bean.setDisplaySort(displaySort == null ? 0 : displaySort);
                bean.setOrderNo(orderNo == null ? 0 : orderNo);
            }
        }));
        // 先按玩法集排序，再按玩法排序
        return result.stream().sorted(Comparator.comparing(MarketCategory::getDisplaySort).thenComparing(MarketCategory::getOrderNo)).collect(Collectors.toList());
    }

    @Override
    public MatchMarketLiveBean getMatchInfo(final Long sportId, final Long matchId) {
        Query query = new Query().addCriteria(Criteria.where("matchId").is(matchId));
        return mongoTemplate.findOne(query, MatchMarketLiveBean.class);
    }

    private Integer getTemplateId(final Long matchId, final Long categoryId, final Map<Long, CategoryTemplateVo> categoryTemplateMap) {
        if (Basketball.Secondary.PLAYER.getPlayIds().contains(categoryId)) {
            return 5;
        }
        // 查询模板ID，默认为0
        CategoryTemplateVo template = categoryTemplateMap.get(categoryId);
        Integer templateId = template.getTemplateId();
        if (templateId == null) {
            log.warn("::{}::玩法未配置模板：categoryId={}", matchId, categoryId);
            templateId = 0;
        }
        return templateId;
    }

    private void replaceTitle(TemplateTitle title, Map<String, I18nBean> teamMap) {
        title.getNames().replaceTitle(teamMap);
    }

    private void replaceTitle(List<TemplateTitle> titles, Map<String, I18nBean> teamMap) {
        if (CollectionUtils.isEmpty(titles)) {
            return;
        }
        titles.forEach(title -> title.getNames().replaceTitle(teamMap));
    }

    @Override
    public String getPlaySetCodeByPlaySetId(Long playSetId) {
        RcsMarketCategorySet rcsMarketCategorySet = this.getById(playSetId);
        if (rcsMarketCategorySet == null) {
            return "";
        }
        return rcsMarketCategorySet.getPlaySetCode();
    }

    @Override
    public List<RcsMarketCategorySet> getPerformanceSet(Long sportId) {
        return marketCategorySetMapper.getPerformanceSet(sportId);
    }

}
