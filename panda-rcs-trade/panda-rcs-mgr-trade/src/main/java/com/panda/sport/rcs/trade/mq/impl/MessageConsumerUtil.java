package com.panda.sport.rcs.trade.mq.impl;

import com.google.common.collect.Lists;
import com.panda.sport.rcs.mapper.RcsBroadCastMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.trade.wrapper.RcsBroadCastService;
import com.panda.sports.api.ISystemUserOrgAuthApi;
import com.panda.sports.api.vo.SysTraderVO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author :  myname 操盘消息通知
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.mgr.mq.impl
 * @Description :  TODO
 * @Date: 2020-10-18 14:45
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Component
@Slf4j
@Data
public class MessageConsumerUtil  {
    @Autowired
    private RcsBroadCastMapper rcsBroadCastMapper;
    @Autowired
    private RcsBroadCastService rcsBroadCastService;
    @Reference(check = false, lazy = true, retries = 1, timeout = 5000)
    private  ISystemUserOrgAuthApi systemUserOrgAuthApi;
    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;
    /***
     * 操盘部有权限的id
     **/
    public  HashMap<Integer,List<Integer>> hashMap=new HashMap<>();
    /**
     * 操盘部所有人
     */
    public  List<Integer> allTradeUserIdList=new ArrayList<>();
    /**
     * 风控中心有权限的人
     */
    public  List<Integer> riskUserIdList=new ArrayList<>();

    /**
     * 风控中心有权限的人
     */
    public  List<Integer> settlementUsers=new ArrayList<>();
    /**
     * 业务拉取的数据
     */
    List<Map<String, Object>> authUserByOperation=new ArrayList<>();
    /***
     * 最新拉取rpc时间
     **/
    private static  long time=0;

    /**
     * 初始化业务来的数据
     */
    public   void initData() {
        if (System.currentTimeMillis() < time) {
            return;
        }
        authUserByOperation= systemUserOrgAuthApi.getAuthUserByOperation();
        hashMap = getSportIds(authUserByOperation);
        riskUserIdList = systemUserOrgAuthApi.riskUser();
        List<SysTraderVO> sysTraderVOS = systemUserOrgAuthApi.traderUser();
        if (!CollectionUtils.isEmpty(sysTraderVOS)){
            allTradeUserIdList.clear();
            allTradeUserIdList.addAll(sysTraderVOS.stream().map(SysTraderVO :: getId).collect(Collectors.toSet()));
        }
        List<SysTraderVO> settlementTradeVos = systemUserOrgAuthApi.settlementUsers();
        if(!CollectionUtils.isEmpty(settlementTradeVos)){
            settlementUsers.clear();
            settlementUsers.addAll(settlementTradeVos.stream().map(SysTraderVO :: getId).collect(Collectors.toSet()));
        }
        time = System.currentTimeMillis() + 1000 * 60;
    }

    private static HashMap<Integer,List<Integer>>  getSportIds( List<Map<String, Object>> authUserByOperation){
        HashMap<Integer,List<Integer>> hashMap=new HashMap<>();
        if (!CollectionUtils.isEmpty(authUserByOperation)){
            for (Map<String, Object> map:authUserByOperation){
                List<Integer> ids = (List<Integer>) map.get("ids");
                String orgName = (String)map.get("orgName");
                Integer sportId = getSportId(orgName);
                List<Integer> list = hashMap.get(sportId);
                if (list==null){
                    list=new ArrayList<>();
                    list.addAll(ids);
                    hashMap.put(sportId,list);
                }
            }
        }
        return hashMap;
    }

    private static  Integer getSportId(String s){
        if (s.contains("美式足球")){
            return 6;
        }else if (s.contains("篮球")){
            return 2;
        } else if (s.contains("足球")){
            return 1;
        }else if (s.contains("棒球")){
            return 3;
        }else if (s.contains("冰球")){
            return 4;
        }else if (s.contains("网球")){
            return 5;
        }else if (s.contains("斯诺克")){
            return 7;
        }else if (s.contains("乒乓球")){
            return 8;
        }else if (s.contains("排球")){
            return 9;
        }else if (s.contains("羽毛球")){
            return 10;
        }else if (s.contains("英雄联盟")){
            return 100;
        }else if (s.contains("dota2")){
            return 101;
        }else if (s.contains("cs")){
            return 102;
        }else if (s.contains("王者荣耀")){
            return 103;
        } else if (s.contains("绝地求生")){
            return 104;
        }else if (s.contains("政治娱乐")){
            return 105;
        }
        return 0;
    }
}
