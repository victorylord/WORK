package com.panda.sport.rcs.trade.wrapper.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.mapper.MarketCategorySetMapper;
import com.panda.sport.rcs.mapper.RcsCodeMapper;
import com.panda.sport.rcs.pojo.RcsCode;
import com.panda.sport.rcs.trade.wrapper.CategoryService;
import com.panda.sport.rcs.trade.wrapper.RcsCodeService;
import com.panda.sport.rcs.vo.CategoryConVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.panda.sport.rcs.constants.RedisKey.EXPRIY_TIME_7_DAYS;


/**
 * @ClassName CategoryServiceImpl
 * @Description: TODO
 * @Author Enzo
 * @Date
 **/
@Service
public class CategoryServiceImpl extends ServiceImpl<RcsCodeMapper, RcsCode> implements CategoryService {
    @Autowired
    private RcsCodeService rcsCodeService;
    @Autowired
    private MarketCategorySetMapper marketCategorySetMapper;
    @Autowired
    private RedisClient redisClient;

    private String CACHE_CATEGORY_CON = "CACHE_CATEGORY_CON_";

    private String CACHE_MAIN_CATEGORY_CON = "CACHE_MAIN_CATEGORY_CON_";

    @Override
    public Integer getCategoryCon(Long id) {
        List<CategoryConVo> categoryConVos = selectCategoryCon(1L);
        Integer result = 0;
        if (categoryConVos.size() > 0) {
            CategoryConVo categoryConVo = categoryConVos.stream().filter(line -> JsonFormatUtils.fromJsonArray(line.getCategoryIds(), Long.class).contains(id)).findFirst().orElse(null);
            if (null != categoryConVo) {
                result = categoryConVo.getId().intValue();
            }
        }
        return result;
    }

    @Override
    public List<CategoryConVo> selectCategoryCon(Long sportId) {
        List<CategoryConVo> result = new ArrayList<>();
        Object o = redisClient.get(CACHE_CATEGORY_CON + sportId);
        if (o != null) {
            result = JsonFormatUtils.fromJsonArray(JsonFormatUtils.toJson(o), CategoryConVo.class);
        } else {
            List<CategoryConVo> categoryConVos = marketCategorySetMapper.selectCategoryCons(sportId);
            categoryConVos.addAll(mainCategory(sportId));
            List<CategoryConVo> collect = categoryConVos.stream().filter(filter -> StringUtils.isNotBlank(filter.getCategoryIds())).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(collect)) {
                result = categoryConVos.stream().filter(filter -> StringUtils.isNotBlank(filter.getCategoryIds())).collect(Collectors.toList());
                redisClient.setExpiry(CACHE_CATEGORY_CON + sportId, JsonFormatUtils.toJson(categoryConVos), EXPRIY_TIME_7_DAYS);
            }
        }
        return result;
    }

    @Override
    public List<CategoryConVo> selectCategoryConById(Long id) {
        List<CategoryConVo> result = new ArrayList<>();
        List<CategoryConVo> rcsCodes = selectCategoryCon(1L);
        if (!CollectionUtils.isEmpty(rcsCodes)) {
            result = rcsCodes.stream().filter(filter -> JsonFormatUtils.fromJsonArray(filter.getCategoryIds(), Long.class).contains(id)).collect(Collectors.toList());
        }
        return result;
    }

    @Override
    public List<CategoryConVo> mainCategory(Long sportId) {
        List<CategoryConVo> categoryConVos = new ArrayList<>();
        Object o = redisClient.get(CACHE_MAIN_CATEGORY_CON + sportId);
        if (o != null) {
            categoryConVos = JsonFormatUtils.fromJsonArray(JsonFormatUtils.toJson(o), CategoryConVo.class);
        } else {
            List<RcsCode> rcsCodes = rcsCodeService.selectRcsCods("category_con_" + sportId);
            if (!CollectionUtils.isEmpty(rcsCodes)) {
                for (RcsCode model : rcsCodes) {
                    CategoryConVo categoryConVo = new CategoryConVo();
                    categoryConVo.setId(Long.parseLong(model.getChildKey()));
                    categoryConVo.setName(model.getRemark());
                    categoryConVo.setCategoryIds(model.getValue());
                    categoryConVo.setSportId(sportId);
                    categoryConVos.add(categoryConVo);
                }
                redisClient.setExpiry(CACHE_MAIN_CATEGORY_CON + sportId, JsonFormatUtils.toJson(categoryConVos), EXPRIY_TIME_7_DAYS);
            }
        }
        if(SportIdEnum.otherSports().contains(sportId)){
            Long categorySetId= 0L;
            List<Long> categorySetIds= new ArrayList<>();
            if(SportIdEnum.isTennis(sportId)){
                categorySetId = 50002L;
                categorySetIds=new ArrayList<>(Arrays.asList(50003L,50004L,50005L,50006L));
            }else if(SportIdEnum.isPingpong(sportId)){
                categorySetId = 80002L;
                categorySetIds=new ArrayList<>(Arrays.asList(80003L,80004L,80005L,80006L,80007L,80008L));
            }else if(SportIdEnum.isVolleyball(sportId)){
                categorySetId = 90002L;
                categorySetIds=new ArrayList<>(Arrays.asList(90003L,90004L,90005L,90006L,90007L,90008L));
            }else if(SportIdEnum.isSnooker(sportId)){
                categorySetId = 70002L;
                categorySetIds=new ArrayList<>(Arrays.asList(70003L,70004L,70005L,70006L,70007L,70008L,70009L,70010L,
                        70011L,70012L,70013L,70014L,70015L,70016L,70017L,70018L,70019L,70020L,
                        70021L,70022L,70023L,70024L,70025L,70026L,70027L,70028L,70029L,70030L,
                        70031L,70032L,70033L,70034L,70035L,70036L));
            }else if(SportIdEnum.BADMINTON.isYes(sportId)){
                categorySetId = 100002L;
                categorySetIds=new ArrayList<>(Arrays.asList(100003L,100004L));
            }
            // 棒球，新加三个玩法集合
            else if(SportIdEnum.BASEBALL.isYes(sportId)){
                categorySetId = 30003L;
                categorySetIds=new ArrayList<>(Arrays.asList(30004L,30005L));
            }
            // 冰球，新加2个玩法集合
            else if(SportIdEnum.ICE_HOCKEY.isYes(sportId)){
                categorySetId = 40002L;
                categorySetIds=new ArrayList<>(Arrays.asList(40003L,40004L));
            }
            
            if(!CollectionUtils.isEmpty(categoryConVos)){
                Long sid = categorySetId;
                CategoryConVo vo = categoryConVos.stream().filter(fi -> fi.getId().equals(sid)).findFirst().orElse(null);
                if(vo!=null){
                    for (Long id: categorySetIds) {
                        CategoryConVo conVo = BeanCopyUtils.copyProperties(vo, CategoryConVo.class);
                        conVo.setId(id);
                        categoryConVos.add(conVo);
                    }
                }
            }
        }
        return categoryConVos;
    }

    @Override
    public List<CategoryConVo> mainCategory(Long sportId,Long categorySetId) {
        List<CategoryConVo> categoryConVos = new ArrayList<>();
        Object o = redisClient.get(CACHE_MAIN_CATEGORY_CON + sportId+categorySetId);
        if (o != null) {
            categoryConVos = JsonFormatUtils.fromJsonArray(JsonFormatUtils.toJson(o), CategoryConVo.class);
        } else {
            List<RcsCode> rcsCodes = rcsCodeService.selectRcsCods("category_con_" + sportId, String.valueOf(categorySetId));
            if (!CollectionUtils.isEmpty(rcsCodes)) {
                for (RcsCode model : rcsCodes) {
                    CategoryConVo categoryConVo = new CategoryConVo();
                    categoryConVo.setId(Long.parseLong(model.getChildKey()));
                    categoryConVo.setName(model.getRemark());
                    categoryConVo.setCategoryIds(model.getValue());
                    categoryConVo.setSportId(sportId);
                    categoryConVos.add(categoryConVo);
                }
                redisClient.setExpiry(CACHE_MAIN_CATEGORY_CON + sportId+categorySetId, JsonFormatUtils.toJson(categoryConVos), EXPRIY_TIME_7_DAYS);
            }
        }

        return categoryConVos;
    }

    @Override
    public List<Long> categoryIds(Long sportId, Long categorySetId) {
        List<CategoryConVo> categoryConVos = selectCategoryCon(sportId);
        if(CollectionUtils.isEmpty(categoryConVos))return null;
        CategoryConVo categoryConVo = categoryConVos.stream().filter(filter -> categorySetId.equals(filter.getId())).findFirst().orElse(null);
        if(null==categoryConVo)return null;
        return categoryConVo.categoryIds();
    }

    @Override
    public List<Long> mainCategoryIds(Long sportId) {
        List<CategoryConVo> categoryConVos = mainCategory(sportId);
        List<Long> mainCategoryIds = new ArrayList<>();
        if(!CollectionUtils.isEmpty(categoryConVos)){
            categoryConVos.stream().forEach(categoryConVo->{
                mainCategoryIds.addAll(categoryConVo.categoryIds());
            });
        }
        return mainCategoryIds;
    }

    @Override
    public List<Long> mainCategorySetIds(Long sportId, Long categorySetId) {
        List<CategoryConVo> categoryConVos = mainCategory(sportId,categorySetId);
        List<Long> mainCategoryIds = new ArrayList<>();
        if(!CollectionUtils.isEmpty(categoryConVos)){
            categoryConVos.stream().forEach(categoryConVo->{
                mainCategoryIds.addAll(categoryConVo.categoryIds());
            });
        }
        return mainCategoryIds;
    }
}
