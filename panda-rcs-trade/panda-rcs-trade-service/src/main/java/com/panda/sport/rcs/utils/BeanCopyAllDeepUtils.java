package com.panda.sport.rcs.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 *	复制对象里的所有属性： 
 *		针对BeanCopyUtils.copyProperties拷贝对象里的数组问题
 * @author Z9-jordan
 *
 */
public class BeanCopyAllDeepUtils {

	/**
	 * 	先转json，再转对象，返回新的对象
	 * 
	 * @param object  源对象
	 * @param clazz	对象类型
	 * @return
	 */
	public static <T> T copyProperties(Object object, Class<T> clazz){
		return JSONObject.parseObject(JSON.toJSONString(object), clazz);
	}
}
