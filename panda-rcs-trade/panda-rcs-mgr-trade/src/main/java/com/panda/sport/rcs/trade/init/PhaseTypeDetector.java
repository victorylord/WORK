package com.panda.sport.rcs.trade.init;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.pojo.RcsCode;
import com.panda.sport.rcs.trade.wrapper.RcsCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author :  Administrator
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.init
 * @Description :  TODO
 * @Date: 2020-02-07 11:06
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */

@Component
@Slf4j
public class PhaseTypeDetector {
    @Autowired
    private RcsCodeService rcsCodeService;

    public static List<RcsCode> phases = new ArrayList<>();

    public static List<RcsCode> categoryPhsase = new ArrayList<>();

    public static List<Long> haifOthers = new ArrayList<>();

    public static List<Long> zeroFifteen = new ArrayList<>();

    @PostConstruct
    public void initPhaseType() {
        phases = rcsCodeService.selectRcsCods("play_phase");
        categoryPhsase = rcsCodeService.selectRcsCods("category_phsase");
        haifOthers = JsonFormatUtils.fromJsonArray(rcsCodeService.getValue("other_category", "2"), Long.class);
        zeroFifteen= JsonFormatUtils.fromJsonArray(rcsCodeService.getValue("other_category", "4"), Long.class);
    }

    public static Integer getPhaseType(Long categoryId) {
        Integer result = 0;
        RcsCode rcsCode = phases.stream().filter(line -> String.valueOf(categoryId).equals(line.getChildKey())).findFirst().orElse(null);
        if (null != rcsCode) {
            result = Integer.parseInt(rcsCode.getRemark());
        }
        return result;
    }

    public static Integer getCategoryPhaseType(Long categoryId) {
        Integer result = 0;
        RcsCode rcsCode = categoryPhsase.stream().filter(line -> JsonFormatUtils.fromJsonArray(line.getValue(), Long.class).contains(categoryId)).findFirst().orElse(null);
        if (null != rcsCode) {
            result = Integer.parseInt(rcsCode.getChildKey());
        }
        return result;
    }

    public static Integer getColumnType(Long categoryId) {
        Integer result = 0;
        RcsCode rcsCode = phases.stream().filter(line -> String.valueOf(categoryId).equals(line.getChildKey())).findFirst().orElse(null);
        if (null != rcsCode) {
            result = Integer.parseInt(rcsCode.getValue());
        }
        return result;
    }


    public static List<Long> getCategoriesByLevel(Integer level) {
        List<Long> result = new ArrayList<>();
        for (RcsCode rcsCode : categoryPhsase) {
            if (String.valueOf(level).equals(rcsCode.getChildKey())) {
                List<Long> longs = JsonFormatUtils.fromJsonArray(rcsCode.getValue(), Long.class);
                result = longs;
            }
        }
        return result;
    }

    public static Integer getRollType(Long categoryId) {
        Integer result = 0;
        for (RcsCode rcsCode : categoryPhsase) {
            List<Long> longs = JsonFormatUtils.fromJsonArray(rcsCode.getValue(), Long.class);
            if (longs.contains(categoryId)) {
                result = longs.indexOf(categoryId) + 1;
                return result;
            }
        }
        return null;
    }
    
    
    public static List<Integer> getAllPlayByPhase(Integer phase) {
        for (RcsCode rcsCode : categoryPhsase) {
        	if(String.valueOf(phase).equals(rcsCode.getChildKey())) {
        		return JsonFormatUtils.fromJsonArray(rcsCode.getValue(), Integer.class);
        	}
        }
        return null;
    }
    
    public static List<Integer> getAllPlayIds(){
    	List<Integer> allList = new ArrayList<Integer>();
    	for (RcsCode rcsCode : categoryPhsase) {
    		allList.addAll(JSONObject.parseArray(rcsCode.getValue(),Integer.class));
        }
        return allList;
    }

    public static List<Integer> getAllCategories() {
        List<Integer> collect = phases.stream().map(map -> Integer.parseInt(map.getChildKey())).collect(Collectors.toList());
        if (null != collect && collect.size() > 0) {
            return collect;
        }
        return null;
    }

    public static List<Long> getHalfCategories() {
        List<Long> collect = phases.stream().filter(filter -> Arrays.asList(2L, 4L).contains(Long.parseLong(filter.getRemark()))).
                map(map -> Long.parseLong(map.getChildKey())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)) {
            return Lists.newArrayList();
        }
        collect.addAll(haifOthers);
        return collect;
    }

    public static List<Integer> getRemoveHalfCategories() {
        List<Integer> collect = phases.stream().filter(filter -> Arrays.asList(1, 3).contains(Integer.parseInt(filter.getRemark()))).
                map(map -> Integer.parseInt(map.getChildKey())).collect(Collectors.toList());
        if (null != collect && collect.size() > 0) {
            return collect;
        }

        return null;
    }

}
