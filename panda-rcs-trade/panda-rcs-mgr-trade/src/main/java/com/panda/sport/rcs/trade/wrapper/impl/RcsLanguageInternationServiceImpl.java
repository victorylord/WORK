package com.panda.sport.rcs.trade.wrapper.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.sport.rcs.core.db.annotation.Master;
import com.panda.sport.rcs.mapper.RcsLanguageInternationMapper;
import com.panda.sport.rcs.mongo.I18nBean;
import com.panda.sport.rcs.pojo.LanguageInternation;
import com.panda.sport.rcs.pojo.RcsLanguageInternation;
import com.panda.sport.rcs.pojo.dto.PlayLanguageInternation;
import com.panda.sport.rcs.trade.wrapper.RcsLanguageInternationService;
import com.panda.sport.rcs.utils.CategoryParseUtils;
import com.panda.sport.rcs.vo.ConditionVo;
import com.panda.sport.rcs.vo.I18nItemVo;
import com.panda.sport.rcs.vo.TournamentBeanVo;
import com.panda.sport.rcs.vo.TournamentConditionVo;
import com.panda.sport.rcs.vo.TournamentResultVo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RcsLanguageInternationServiceImpl extends ServiceImpl<RcsLanguageInternationMapper, RcsLanguageInternation> implements RcsLanguageInternationService {
    
    @Override
    public int batchInsertOrUpdate(List<RcsLanguageInternation> list) {
        if (CollectionUtils.isEmpty(list)) {
            return 0;
        }
        return this.baseMapper.batchInsertOrUpdate(list);
    }

    @Override
    public List<RcsLanguageInternation> getLanguageInternationByCode(List<String> nameCodes) {
        if (CollectionUtils.isEmpty(nameCodes)) {
            return null;
        }
        QueryWrapper<RcsLanguageInternation> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(RcsLanguageInternation::getNameCode, nameCodes);
        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public I18nBean getPlayLanguage(Long sportId, Long playId) {
        String value = this.baseMapper.getPlayLanguage(sportId.intValue(), playId);
        if (StringUtils.isNotBlank(value)) {
            return JSON.parseObject(value, I18nBean.class);
        }
        return null;
    }

    @Override
    public I18nBean getPlayerLanguage(String nameCode) {
        String value = this.baseMapper.getPlayLanguageByNamecode(nameCode);
        if (StringUtils.isNotBlank(value)) {
            return JSON.parseObject(value, I18nBean.class);
        }
        return null;
    }
    
    @Override
    public String getPlayerLanguageStr(String nameCode) {
    	String value = this.baseMapper.getPlayLanguageByNamecode(nameCode);
    	if (StringUtils.isNotBlank(value)) {
    		JSONObject parseObject = JSON.parseObject(value);
    		ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
    		HttpServletRequest request = requestAttributes.getRequest();
    		return parseObject.get(request.getHeader("lang")).toString();
    	}
    	return null;
    }

	@Override
	public I18nBean getCategoryLanguage(Long categoryId, Long sportId) {
    	String value = this.baseMapper.getCategoryLanguage(categoryId, sportId);
    	if (StringUtils.isNotBlank(value)) {
            return JSON.parseObject(value, I18nBean.class);
        }
		return null;
	}
	
    @Override
    public List<PlayLanguageInternation> getByMultilingualism(Long sportId) {
        return this.baseMapper.getByMultilingualism(sportId);
    }

    @Override
    public List<PlayLanguageInternation> getAllRefMultilingualism(){
        return this.baseMapper.getByMultilingualism(null);
    }
    
    @Override
    public List<LanguageInternation> getPlayList() {
        return this.baseMapper.getStandardSportMarketCategoryList();
    }

    
    @Override
    public List<LanguageInternation> getPlayList(String matchStage) {
        return this.baseMapper.getStandardSportMarketCategoryList(matchStage);
    }
    
    @Override
    public List<TournamentResultVo> getTournamentList(Long sportId) {
        List<TournamentResultVo> tournamentResultVos = new ArrayList<>();
        if (sportId != null && sportId == 0) {
            //热门赛事暂定，现只作占位
            List<TournamentConditionVo> tournamentConditionVos = new ArrayList<>();
            List<TournamentConditionVo> tournamentList = this.baseMapper.getTournamentList(1L);
            if (tournamentList.size() > 0) {
                TournamentConditionVo tournamentConditionVo = this.baseMapper.getTournamentList(1L).get(0);
                tournamentConditionVos.add(tournamentConditionVo);
                TournamentConditionVo conditionVo = tournamentConditionVos.get(0);
                TournamentResultVo tournamentResultVo = new TournamentResultVo();
                tournamentResultVo.setSpell(conditionVo.getSpell());
                tournamentResultVo.setIntroduction(conditionVo.getIntroduction());
                tournamentResultVo.setRegionId(conditionVo.getRegionId());
                tournamentResultVo.setVisible(conditionVo.getVisible());
                tournamentResultVo.setTournamentConditionVos(tournamentConditionVos);
                tournamentResultVos.add(tournamentResultVo);
            }
        } else {
            List<TournamentConditionVo> tournamentList = this.baseMapper.getTournamentList(sportId);
            Map<String, List<TournamentConditionVo>> collect = tournamentList.stream().collect(Collectors.groupingBy(TournamentConditionVo::getSpell, LinkedHashMap::new, Collectors.toList()));
            for (Map.Entry<String, List<TournamentConditionVo>> map : collect.entrySet()) {
                TournamentResultVo tournamentResultVo = new TournamentResultVo();
                List<TournamentConditionVo> tournamentConditionVos = map.getValue();
                TournamentConditionVo conditionVo = tournamentConditionVos.get(0);
                tournamentResultVo.setSpell(conditionVo.getSpell());
                tournamentResultVo.setIntroduction(conditionVo.getIntroduction());
                tournamentResultVo.setRegionId(conditionVo.getRegionId());
                tournamentResultVo.setVisible(conditionVo.getVisible());
                tournamentResultVo.setTournamentConditionVos(tournamentConditionVos);
                tournamentResultVos.add(tournamentResultVo);
            }
        }

        return tournamentResultVos;
    }
    
    @Override
    public List<ConditionVo> getMarketCategoryList(List<Long> sportIds,List<Long> playSetIds,String lang) {
        return this.baseMapper.getMarketCategoryList(sportIds,playSetIds,lang);
    }
    
    @Override
    public TournamentBeanVo getLanguageInternationByTournamentId(Long tournamentId) {
        return this.baseMapper.getLanguageInternationByTournamentId(tournamentId);
    }

    @Override
    public LanguageInternation getLanguageInternationByCategoryId(Long categoryId) {
        return this.baseMapper.getLanguageInternationByCategoryId(categoryId);
    }
    
    @Override
    public Map<Long, Map<String, String>> getCachedNamesMapByCodes(List<Long> nameCodes) {
        Map<Long, Map<String, String>> mapMap = new HashMap<>();
        if (CollectionUtils.isEmpty(nameCodes)) {
            return null;
        }
        HashSet<Long> objects = new HashSet<>(nameCodes);
        List<LanguageInternation> languageNameCodes = this.baseMapper.getLanguageNameCodes(objects);
        if (!CollectionUtils.isEmpty(languageNameCodes)) {
            for (LanguageInternation languageInternation : languageNameCodes) {
                Map<String, String> map = mapMap.get(languageInternation.getNameCode());
                if (map == null) {
                    map = new HashMap<>();
                }
                map.put(languageInternation.getLanguageType(), languageInternation.getText());
                mapMap.put(languageInternation.getNameCode(), map);
            }
        }
        return mapMap;
    }

    /**
     * 根据nameCode从缓存获取所有语言集，首次不存在则从数据库加载
     *
     * @param nameCode
     * @return
     */
    @Override
    public List<I18nItemVo> getCachedNamesByCode(Long nameCode) {
        if (nameCode == null) {
            return null;
        }
        List<LanguageInternation> cachedLanguages = this.baseMapper.getByNameCodeDeprecated(Arrays.asList(nameCode));
        // 数据库也不存在则返回
        if (CollectionUtils.isEmpty(cachedLanguages)) {
            return Collections.emptyList();
        }
        
        List<I18nItemVo> resultList = Lists.newArrayListWithCapacity(cachedLanguages.size());
        for (LanguageInternation languageInternation : cachedLanguages) {
            I18nItemVo itemVo = new I18nItemVo();
            itemVo.setLanguageType(languageInternation.getLanguageType());
            itemVo.setText(languageInternation.getText());
            resultList.add(itemVo);
        }
        return resultList;
    }



    /**
     * 根据nameCode从缓存获取所有语言集
     * 获取顺序：本地缓存->redis->database
     * 返回格式为MAP
     *
     * @param nameCode
     * @return
     */
    @Override
    public Map<String, String> getCachedNamesMapByCode(Long nameCode) {
        if (nameCode == null) {
            return null;
        }
        List<I18nItemVo> i18nItemVoList = getCachedNamesByCode(nameCode);
        if (CollectionUtils.isEmpty(i18nItemVoList)) {
            return null;
        }
        Map<String, String> resultMap = Maps.newHashMapWithExpectedSize(5);
        for (I18nItemVo itemVo : i18nItemVoList) {
            resultMap.put(itemVo.getLanguageType(), itemVo.getText());
        }
        return resultMap;
    }

    @Override
    @Master
    public Map<String, List<I18nItemVo>> getCachedNamesByCode(List<Long> nameCode) {
        return getCachedNamesByCode(nameCode, true);
    }


    @Override
    public Map<String, List<I18nItemVo>> getCachedNamesByCode(List<Long> nameCode, boolean isParse) {
        //批量根据nameCode查询国际话
        Collection<LanguageInternation> cachedLanguages = null;
        Map<String, List<I18nItemVo>> resultMap = new HashMap<>(4);
        if (nameCode.size() > 0) {
            cachedLanguages = this.baseMapper.getByNameCodeDeprecated(nameCode);
        }
        if (cachedLanguages == null) {
            return resultMap;
        }
        //根据同样的nameCode进行分组
        for (LanguageInternation language : cachedLanguages) {
            List<I18nItemVo> resultList = new ArrayList<>(4);
            for (LanguageInternation languages : cachedLanguages) {
                if (language.getNameCode().longValue() == languages.getNameCode().longValue()) {
                    I18nItemVo itemVo = new I18nItemVo();
                    itemVo.setLanguageType(languages.getLanguageType());
                    if (isParse) {
                        itemVo.setText(CategoryParseUtils.parseName(languages.getText()));
                    } else {
                        itemVo.setText(languages.getText());
                    }
                    resultList.add(itemVo);
                }
            }
            resultMap.put(language.getNameCode().toString(), resultList);
        }
        return resultMap;
    }
}
