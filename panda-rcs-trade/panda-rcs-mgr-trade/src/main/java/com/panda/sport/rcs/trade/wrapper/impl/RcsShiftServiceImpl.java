package com.panda.sport.rcs.trade.wrapper.impl;


import com.alibaba.nacos.common.util.UuidUtils;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.RcsShiftMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsShift;
import com.panda.sport.rcs.pojo.dto.ShiftDto;
import com.panda.sport.rcs.pojo.vo.ShiftGroupVo;
import com.panda.sport.rcs.trade.enums.ShiftEnum;
import com.panda.sport.rcs.trade.enums.ShiftMarketTypeEnum;
import com.panda.sport.rcs.trade.wrapper.RcsShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RcsShiftServiceImpl extends ServiceImpl<RcsShiftMapper, RcsShift> implements RcsShiftService {

    @Resource
    private RcsShiftMapper rcsShiftMapper;

    @Autowired
    private ProducerSendMessageUtils sendMessage;

    private static String RCS_UPDATE_SHIFT = "RCS_UPDATE_SHIFT";


    @Override
    public Map shiftUserList(ShiftDto shiftDto) {
        Map<String, Object> objectObjectHashMap = new HashMap<>();
        List<String> users = new ArrayList<>();
        if(null!=shiftDto){
            users = shiftDto.getUsers();
        }
        List<RcsShift> list = rcsShiftMapper.noDesignateUserList(users);
        List<RcsShift> rcsShifts = rcsShiftMapper.shiftUserList(shiftDto,users);
        if (CollectionUtils.isNotEmpty(rcsShifts)) {
            Map<String, Map<String, Map<String, List<RcsShift>>>> collect = rcsShifts.stream().collect(Collectors.groupingBy(e -> String.valueOf(e.getSportId()), Collectors.groupingBy(RcsShift::getShift, Collectors.groupingBy(e -> String.valueOf(e.getMarketType())))));
            for (String s : collect.keySet()) {
                Map<String, Map<String, List<RcsShift>>> stringMapMap = collect.get(s);
                for (String s1 : stringMapMap.keySet()) {
                    Map<String, List<RcsShift>> stringListMap = stringMapMap.get(s1);
                    for (String s2 : stringListMap.keySet()) {
                        List<RcsShift> rcsShifts1 = stringListMap.get(s2);
                        for (RcsShift rcsShift : rcsShifts1) {
                            rcsShift.setSupers(s+"_"+s1+"_"+s2);
                            rcsShift.setActive(false);
                        }
                    }
                }
            }
            objectObjectHashMap.put("Designate",collect);
        }
        objectObjectHashMap.put("noDesignate", list);
        return objectObjectHashMap;
    }

    @Override
    public List<ShiftGroupVo> shiftUserGroupList(ShiftDto shiftDto, String lang) {
        String sportId = shiftDto.getSportId();
        if(!("1".equals(shiftDto.getSportId())||"2".equals(shiftDto.getSportId()))){
            shiftDto.setSportId("300");
        }
        //得到树
        Map<String,Map<String, Map<String, RcsShift>>> map = new HashMap<>();
        List<RcsShift> rcsShifts = rcsShiftMapper.shiftUserList(shiftDto,null);
        if (CollectionUtils.isNotEmpty(rcsShifts)) {
            Map<String, List<RcsShift>> shiftCollect = rcsShifts.stream().collect(Collectors.groupingBy(RcsShift::getShift));
            Map<String, List<RcsShift>> marketTypeCollect = rcsShifts.stream().collect(Collectors.groupingBy(e->String.valueOf(e.getMarketType())));
            for (String key1 : shiftCollect.keySet()) {
                for (String key2 : marketTypeCollect.keySet()) {
                    List<RcsShift> rcsShifts1 = marketTypeCollect.get(key2);
                    for (RcsShift rcsShift : rcsShifts1) {
                        rcsShift.setTitle(rcsShift.getUserCode());
                        if(key1.equals(rcsShift.getShift())){
                            Map<String, Map<String, RcsShift>> marketTypeMap = map.get(key1);
                            if(null==marketTypeMap){
                                marketTypeMap = new HashMap<>();
                                HashMap<String, RcsShift> userMap = new HashMap<>();
                                userMap.put(rcsShift.getUserCode(),rcsShift);
                                marketTypeMap.put(key2,userMap);
                                map.put(key1,marketTypeMap);
                            }else {
                                Map<String, RcsShift> userMap = marketTypeMap.get(key2);
                                if(null==userMap){
                                    userMap = new HashMap<>();
                                    userMap.put(rcsShift.getUserCode(),rcsShift);
                                    marketTypeMap.put(key2,userMap);
                                }else {
                                    userMap.put(rcsShift.getUserCode(),rcsShift);
                                }
                            }
                        }
                    }
                }
            }
        }
        //给前端结构
        ArrayList<ShiftGroupVo> list = new ArrayList<>();
        for (String s : map.keySet()) {
            if(s.equals(ShiftEnum.DEAFAULT_A.getNum())){continue;}
            Map<String, Map<String, RcsShift>> shiftMapB = map.get(s);
            List<ShiftGroupVo.ShiftGroupSubVo> marekTypeMapB = new ArrayList<>();
            ShiftGroupVo shiftGroupVo = new ShiftGroupVo();
            List rcsShiftsSynthesis = new ArrayList();
            if("300".equals(shiftDto.getSportId())){
                for (String s1 : shiftMapB.keySet()) {
                    if(String.valueOf(s1).equals(String.valueOf(ShiftMarketTypeEnum.DEAFAULT_A.getNum()))&&!"300".equals(shiftDto.getSportId())){continue;}
                    Map<String, RcsShift> shiftMapC = shiftMapB.get(s1);
                    for (String s2 : shiftMapC.keySet()) {
                        rcsShiftsSynthesis.add(shiftMapC.get(s2));
                    }
                }
                shiftGroupVo.setId(s);
                shiftGroupVo.setTitle(lang.equals("en")?ShiftEnum.getEnName(s):ShiftEnum.getName(s));
                shiftGroupVo.setChildren(rcsShiftsSynthesis);
            }else {
                for (String s1 : shiftMapB.keySet()) {
                    if(String.valueOf(s1).equals(String.valueOf(ShiftMarketTypeEnum.DEAFAULT_A.getNum()))&&!"300".equals(shiftDto.getSportId())){continue;}
                    Map<String, RcsShift> shiftMapC = shiftMapB.get(s1);
                    ShiftGroupVo.ShiftGroupSubVo shiftGroupSubVo = shiftGroupVo.new ShiftGroupSubVo();
                    shiftGroupSubVo.setId(s1);
                    shiftGroupSubVo.setTitle(lang.equals("en")?ShiftMarketTypeEnum.getEnName(Integer.valueOf(s1)):ShiftMarketTypeEnum.getName(Integer.valueOf(s1)));
                    List rcsShiftsC = new ArrayList();
                    for (String s2 : shiftMapC.keySet()) {
                        rcsShiftsC.add(shiftMapC.get(s2));
                    }
                    shiftGroupSubVo.setChildren(rcsShiftsC);
                    marekTypeMapB.add(shiftGroupSubVo);
                }
                shiftGroupVo.setId(s);
                shiftGroupVo.setTitle(lang.equals("en")?ShiftEnum.getEnName(s):ShiftEnum.getName(s));
                shiftGroupVo.setChildren(marekTypeMapB);

            }
            list.add(shiftGroupVo);
        }
        return list;
    }

    @Override
    public void updateShiftList(ShiftDto shiftDto) {
        List<RcsShift> shifts = shiftDto.getShifts();
        for (RcsShift shift : shifts) {
            if(null==shift.getSportId()){shift.setSportId(0);}
            if(null==shift.getShift()){shift.setShift("0");}
            if(null==shift.getMarketType()){shift.setMarketType(100);}
        }
        if(CollectionUtils.isEmpty(shifts)){return;}
        List<String> deleteShifts = new ArrayList<>();
        for (RcsShift shift : shifts) {
            if(0==shift.getSportId()&&"0".equals(shift.getShift())&&100==shift.getMarketType())
            deleteShifts.add(shift.getUserCode());
        }
        if (CollectionUtils.isNotEmpty(shifts)) {
            rcsShiftMapper.batchInsertOrUpdate(shifts);
        }
        if(!CollectionUtils.isEmpty(deleteShifts)){
            rcsShiftMapper.deleteByUserCode(deleteShifts);
        }
        shiftListSendToMQ();
    }

    /**
     * 排班全量数据发送到MQ
     */
    private void shiftListSendToMQ() {
        ArrayList<RcsShift> objects = new ArrayList<>();
        List<RcsShift> list = rcsShiftMapper.noDesignateUserList(null);
        List<RcsShift> rcsShifts = rcsShiftMapper.shiftUserList(null,null);
        objects.addAll(list);
        objects.addAll(rcsShifts);
        String linkId = UuidUtils.generateUuid();
        HashMap<Object, Object> map = new HashMap<>();
        map.put("time", System.currentTimeMillis());
        map.put("linkId", linkId);
        map.put("data", objects);
        sendMessage.sendMessage(RCS_UPDATE_SHIFT,null,linkId,map);

    }
}


