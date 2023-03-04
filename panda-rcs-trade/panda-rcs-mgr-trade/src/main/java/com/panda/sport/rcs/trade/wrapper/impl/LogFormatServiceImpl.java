package com.panda.sport.rcs.trade.wrapper.impl;

import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.trade.log.format.LogFormatBean;
import com.panda.sport.rcs.trade.log.format.LogFormatPublicBean;
import com.panda.sport.rcs.pojo.vo.OrderTakingVo;
import com.panda.sport.rcs.trade.enums.HandleStatusEnum;
import com.panda.sport.rcs.trade.enums.LogTypeEnum;
import com.panda.sport.rcs.trade.log.LogContext;
import com.panda.sport.rcs.trade.wrapper.LogFormatService;
import com.panda.sport.rcs.vo.OrderSecondConfigVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @ClassName LogFormatServiceImpl
 * @Description: TODO
 * @Author Enzo
 * @Date
 **/
@Service
public class LogFormatServiceImpl  implements LogFormatService {

    @Override
    public void pauseMatchOrderLog(OrderSecondConfigVo vo) {
        LogFormatPublicBean publicBean = new LogFormatPublicBean(LogTypeEnum.TIMELYBET.getCode()+"", switchOnOff(vo.getSecondStatus())+"暂停接拒", String.valueOf(vo.getMatchInfoId()));
        Map<String, Object> dynamicBean = new HashMap<String, Object>();
        if(StringUtils.isNotBlank(vo.getTrader()))dynamicBean.put("操盘手",vo.getTrader());
        LogContext.getContext().addFormatBean(publicBean, dynamicBean, new LogFormatBean("暂停接拒状态", "", switchOnOff(vo.getSecondStatus())));
    }

    @Override
    public void saveOrderSecondConfigLog(OrderSecondConfigVo vo) {
        int secondStatus = vo.getSecondStatus();
        LogFormatPublicBean publicBean = new LogFormatPublicBean(LogTypeEnum.TIMELYBET.getCode()+"", switchOnOff(secondStatus)+"一键秒接", String.valueOf(vo.getMatchInfoId()));
        Map<String, Object> dynamicBean = new HashMap<String, Object>();
        List<OrderSecondConfigVo> playSetList = vo.getPlaySetList();
        List<Integer> userLevels = vo.getUserLevels();
        if(!CollectionUtils.isEmpty(playSetList)){
            List<Long> playSetIds = playSetList.stream().filter(fi -> fi.getSecondStatus() == secondStatus).map(map -> map.getPlaySetId()).collect(Collectors.toList());
            dynamicBean.put("玩法集", JsonFormatUtils.toJson(playSetIds));
        }
        if(!CollectionUtils.isEmpty(userLevels))dynamicBean.put("用户等级", JsonFormatUtils.toJson(userLevels));
        if(!ObjectUtils.isEmpty(vo.getBetAmount()))dynamicBean.put("注单金额<=",vo.getBetAmount());

        LogContext.getContext().addFormatBean(publicBean, dynamicBean, new LogFormatBean("一键秒接状态", "", switchOnOff(vo.getSecondStatus())));
    }

    @Override
    public void orderTakingBatchLog(OrderTakingVo vo) {
        LogFormatPublicBean publicBean = new LogFormatPublicBean(LogTypeEnum.TIMELYBET.getCode()+"", HandleStatusEnum.codeValue(vo.getState()), vo.getMatchId()+"");
        Map<String, Object> dynamicBean = new HashMap<String, Object>();
        if(!CollectionUtils.isEmpty(vo.getIds()))dynamicBean.put("订单号",JsonFormatUtils.toJson(vo.getIds()));
        if(StringUtils.isNotBlank(vo.getUserName()))dynamicBean.put("操盘手",vo.getUserName());
        LogContext.getContext().addFormatBean(publicBean, dynamicBean, new LogFormatBean("接拒状态", "", HandleStatusEnum.codeValue(vo.getState())));
    }

    String switchOnOff(int status){
        String value = "";
        if(status==1){
            value="开启";
        }else if(status==0){
            value="关闭";
        }
        return value;
    }
}
