package com.panda.sport.rcs.trade.log.format;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.panda.sport.rcs.log.annotion.format.LogFormatAnnotion;
import com.panda.sport.rcs.log.annotion.format.LogFormatDynamicAnnotion;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
public class LogFormatDynamicBean {
	
	private static Map<String, List<String>> dynamicParseMap = new HashMap<String, List<String>>();
	
	private LogFormatDynamicBean() {
		super();
	}
	
	public static Map<String, Object> parseAnno(Object obj){
		Map<String, Object> result = new HashMap<String, Object>();
		
		try {
			if(obj == null ) return new HashMap<String, Object>();
			
			LogFormatAnnotion anno = obj.getClass().getAnnotation(LogFormatAnnotion.class);
			if(anno == null) {
				return JSONObject.parseObject(JSONObject.toJSONString(obj),new TypeReference<Map<String,Object>>(){});
			}
			
			List<String> list = new ArrayList<String>();
			if(!dynamicParseMap.containsKey(obj.getClass().getName())) {
				dynamicParseMap.put(obj.getClass().getName(), list);
				
				for(Field field : obj.getClass().getDeclaredFields()) {
					if(field.getAnnotation(LogFormatDynamicAnnotion.class) == null ) continue;
					
					try {
						String name = field.getName();
						result.put(name, field.get(obj));
						list.add(name);
					}catch (Exception e) {
						log.error(e.getMessage(),e);
					}
				}
				
				return result;
			}
			
			Map<String, Object> dataMap = JSONObject.parseObject(JSONObject.toJSONString(obj),new TypeReference<Map<String,Object>>(){});
			for(String key : dynamicParseMap.get(obj.getClass().getName())) {
				if(!dataMap.containsKey(key)) {
					continue;
				}
				result.put(key, dataMap.get(key));
			}
			
		}catch (Exception e) {
			log.error(e.getMessage(),e);
		}
		return result;
	}
	
}
