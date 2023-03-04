package com.panda.sport.rcs.trade.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.panda.sport.rcs.pojo.LanguageInternation;
import com.panda.sport.rcs.pojo.dto.PlayLanguageInternation;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.RcsLanguageInternationService;
import com.panda.sport.rcs.vo.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author :  kimi
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.bootstrap.controller
 * @Description :  获取玩法列表
 */
@RestController
@RequestMapping(value = "playList")
@Slf4j
public class PlayController {
	
    @Autowired
    private RcsLanguageInternationService rcsLanguageInternationService;

    @RequestMapping(value = "/get", method = RequestMethod.GET)
    public HttpResponse<List<LanguageInternation>> getList() {
        List<LanguageInternation> playList;
        try {
            playList = rcsLanguageInternationService.getPlayList();
            return HttpResponse.success(playList);
        } catch (Exception e) {
            log.error("::{}::风控服务器出问题{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }

    @RequestMapping(value = "/getByMultilingualism", method = RequestMethod.GET)
    public HttpResponse<List<LanguageInternation>> getByMultilingualism(Long sportId, @RequestHeader(value = "lang",required = false) String lang) {
        try {
            HashMap<Integer, Map<String, String>> hashMap = new HashMap<>();
            List<PlayLanguageInternation> byMultilingualism = rcsLanguageInternationService.getByMultilingualism(sportId);
            if (!CollectionUtils.isEmpty(byMultilingualism)) {
                for (PlayLanguageInternation languageInternation : byMultilingualism) {
                    Map<String, String> map = hashMap.get(languageInternation.getPlayId());
                    if (map == null) {
                        map = new HashMap<>();
                        hashMap.put(languageInternation.getPlayId(), map);
                    }
                    if(StringUtil.isNotEmpty(languageInternation.getText())){
                        Map<String,Object> names=JSON.parseObject(languageInternation.getText());
                        map.put(lang, names.get(lang).toString());
                    }
                }
            }
            return HttpResponse.success(hashMap);
        } catch (Exception e) {
            log.error("::{}::风控服务器出问题{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }


    @RequestMapping(value = "/getAllRefMultilingualism", method = RequestMethod.GET)
    public HttpResponse<List<LanguageInternation>> getAllRefMultilingualism(Long sportId,@RequestHeader(value = "lang",required = false) String lang) {
        try {
            List<PlayLanguageInternation> byMultilingualisms = rcsLanguageInternationService.getByMultilingualism(sportId);
            Map<String, List<PlayLanguageInternation>> sportList = byMultilingualisms.stream().collect(Collectors.groupingBy(e -> String.valueOf(e.getSportId())));
            HashMap<String, Map> sportIdMap = new HashMap<>();
            for (String key : sportList.keySet()) {
                List<PlayLanguageInternation> playLanguageInternations = sportList.get(key);
                Map<String, Map> playMap = new HashMap<>();
                for (PlayLanguageInternation playLanguageInternation : playLanguageInternations) {
                    Map map = JSONObject.parseObject(playLanguageInternation.getText(), Map.class);
                    playMap.put(String.valueOf(playLanguageInternation.getPlayId()),map);
                }
                sportIdMap.put(key,playMap);
            }
            return HttpResponse.success(sportIdMap);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("::{}::风控服务器出问题{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }

    public Map<String,Object> getSportByCategory(List<PlayLanguageInternation> byMultilingualism,long sportId,String lang){
        Map categorymap=new HashMap();
        for(PlayLanguageInternation languageInternation:byMultilingualism){
            if(languageInternation.getSportId()==sportId){
                Map maps=new HashMap();

                for(PlayLanguageInternation languageInternationpo:byMultilingualism){
                    if(languageInternation.getCategoryId()==languageInternationpo.getCategoryId()){
                        if(StringUtil.isNotEmpty(languageInternation.getText())){
                            Map<String,Object> names=JSON.parseObject(languageInternation.getText());
                            maps.put(lang, names.get(lang).toString());
                        }
                    }
                }
                categorymap.put(languageInternation.getCategoryId(),maps);
            }


        }
        return categorymap;
    }

    /**
     * 玩法查询
     *
     * @param matchStage 赛事阶段  1:比分全场矩阵；2:比分上半场矩阵
     * @return
     */
    @RequestMapping(value = "/get2", method = RequestMethod.GET)
    public HttpResponse<List<LanguageInternation>> getList2(String matchStage) {
        List<LanguageInternation> playList;
        try {
            playList = rcsLanguageInternationService.getPlayList(matchStage);
            return HttpResponse.success(playList);
        } catch (Exception e) {
            log.error("::{}::风控服务器出问题{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }
}
