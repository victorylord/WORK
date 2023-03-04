package com.panda.sport.rcs.trade.log;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.panda.sport.rcs.log.annotion.format.LogFormatAnnotion;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.trade.log.format.LogFormatBean;
import com.panda.sport.rcs.trade.log.format.LogFormatDynamicBean;
import com.panda.sport.rcs.trade.log.format.LogFormatPublicBean;
import com.panda.sport.rcs.trade.util.BeanUtils;
import com.panda.sport.rcs.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;

/**
 *  @author Kwon
 *  2022年10月25日14:59:07
 *  改造日志记录
 */
@Slf4j
public class LogContext {
    private ProducerSendMessageUtils producerSendMessageUtils = (ProducerSendMessageUtils)BeanUtils.getBean(ProducerSendMessageUtils.class);

    private List<Map<String, Object>> formatList = new ArrayList<Map<String, Object>>();

    public static LogContext getContext() {
        return new LogContext();
    }
    public void addFormatBean(LogFormatPublicBean publicBean , Object dynamicBean  , LogFormatBean beanList ) {
        addFormatBean(publicBean, dynamicBean, Arrays.asList(beanList));
    }
    public void addFormatBean(LogFormatPublicBean publicBean , Object dynamicBean  , List<LogFormatBean> beanList ) {
        if(publicBean == null || beanList == null || beanList.size() <= 0) return;

        Map<String, Object> map = JSONObject.parseObject(JSONObject.toJSONString(publicBean) , new TypeReference<Map<String, Object>>(){});
        Map<String, Object> dynamicMap = LogFormatDynamicBean.parseAnno(dynamicBean);
        map.put("dynamicBean", dynamicMap);

        for(LogFormatBean formatBean : beanList) {
            if(!StringUtils.isBlank(formatBean.getFormat())) {
                formatBean.setOldVal(String.format(formatBean.getFormat(), formatBean.getOldVal()));
                formatBean.setNewVal(String.format(formatBean.getFormat(), formatBean.getNewVal()));
            }

            Map<String, Object> temMap = JSONObject.parseObject(JSONObject.toJSONString(map) , new TypeReference<Map<String, Object>>(){});
            temMap.putAll(JSONObject.parseObject(JSONObject.toJSONString(formatBean) , new TypeReference<Map<String, Object>>(){}));
            formatList.add(temMap);
        }
        if(formatList.size() > 0){
            producerSendMessageUtils.sendMessage("RCS_LOG_FORMAT", null, publicBean.getUid(), formatList);
        }

    }
    public void addFormatBean(LogFormatPublicBean publicBean ,Object dynamicBean  , Object oldVal , Object newVal) {
        try {
            if(newVal == null ) {
                return ;
            }
            LogFormatAnnotion logformatAnno = newVal.getClass().getAnnotation(LogFormatAnnotion.class);
            if(logformatAnno == null) return;

            Map<String, Object> oldMap = new HashMap<String, Object>();
            if(oldVal != null ) {
                oldMap = JSONObject.parseObject(JSONObject.toJSONString(oldVal),new TypeReference<Map<String,Object>>(){});
            }

            Field[] fields = newVal.getClass().getDeclaredFields();
            for(Field field : fields) {
                LogFormatAnnotion fieldAnno = field.getAnnotation(LogFormatAnnotion.class);
                if(fieldAnno == null) {
                    continue;
                }

                field.setAccessible(true);
                String fileName = field.getName();

                String name = fieldAnno.name();
                String format = fieldAnno.format();

                if(StringUtils.isBlank(format)) format = "%s";

                String oldFieldVal = String.format(format, oldMap.get(fileName));
                Object newFieldObj = field.get(newVal);
                if(newFieldObj == null && fieldAnno.isIgnoreBlank()) {//忽略空值
                    continue;
                }

                String newFieldVal = field.get(newVal) == null ? "" : String.valueOf(field.get(newVal));

                if(oldMap.containsKey(fileName) && !oldFieldVal.equals(newFieldVal) ) {
                    addFormatBean(publicBean, dynamicBean, new LogFormatBean(name, oldFieldVal, newFieldVal, format));
                }
            }
        }catch (Exception e) {
            log.error("addFormatBean:{}",e.getMessage(),e);
        }
    }
}
