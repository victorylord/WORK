package com.panda.sport.rcs.trade.util;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.panda.sport.rcs.utils.MarketAdditionUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NameExpressionValueUtils {
	
	/**
	 * 盘口转换，1.25 转到 1/1.5
	* @Title: getNumberParseToText 
	* @Description: TODO 
	* @param @return    设定文件 
	* @return String    返回类型 
	* @throws
	 */
	public static String getNumberParseToText(String add ) {
		try {
			if(!NumberUtils.isNumber(add)) return add;
			
			String result = "";
			if(Double.parseDouble(add) < 0) {
				result = result + "-";
			}
			
			Double handpic = Math.abs(Double.parseDouble(add));
			if(handpic / 0.25 % 2 == 1) {
				result = result + MarketAdditionUtils.formatAddition(new BigDecimal(handpic).subtract(new BigDecimal("0.25")).doubleValue());
				result = result + "/";
				result = result + MarketAdditionUtils.formatAddition(new BigDecimal(handpic).add(new BigDecimal("0.25")).doubleValue());
			}else {
				result = result + MarketAdditionUtils.formatAddition(handpic);
			}
			
			return result;
		}catch (Exception e) {
			log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
		}
		
		return add;
	}
	
	/**
	 * 盘口转换，1.25 转到 1/1.5
	* @Title: getNumberParseToText 
	* @Description: TODO 
	* @param @return    设定文件 
	* @return String    返回类型 
	* @throws
	 */
	public static String getNumberParseToText(Integer playId,String oddsType,String addition1) {
		try {
			String add = getNameExpressionValue(playId, oddsType, addition1);
			if(!NumberUtils.isNumber(add)) return add;
			
			String result = "";
			if(Double.parseDouble(add) < 0) {
				result = result + "-";
			}
			
			Double handpic = Math.abs(Double.parseDouble(add));
			if(handpic / 0.25 % 2 == 1) {
				result = result + MarketAdditionUtils.formatAddition(new BigDecimal(handpic).subtract(new BigDecimal("0.25")).doubleValue());
				result = result + "/";
				result = result + MarketAdditionUtils.formatAddition(new BigDecimal(handpic).add(new BigDecimal("0.25")).doubleValue());
			}else {
				result = result + MarketAdditionUtils.formatAddition(handpic);
			}
			
			return result;
		}catch (Exception e) {
			log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
		}
		
		return addition1;
	}
	
	/**
	* 将1.5/2 转到1.75 
	* @Title: getTextParseToNumber 
	* @Description: TODO 
	* @param @param add
	* @param @return    设定文件 
	* @return String    返回类型 
	* @throws
	 */
	public static String getTextParseToNumber(String add ) {
		try {
			if(StringUtils.isBlank(add)) {
				return add;
			}
			
			if(!add.contains("/")) {
				return add;
			}
			
			String result = "";
			if(add.contains("-")) {
				result = result + "-";
			}
			
			String[] strArrs = add.split("/");
			String addResult = new BigDecimal(String.valueOf(Math.abs(Double.parseDouble(strArrs[0])))).add(new BigDecimal(strArrs[1])).divide(new BigDecimal("2")).toPlainString();
			result = result + addResult;
			return result;
		}catch (Exception e) {
			log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
		}
		
		return add;
	}
	
	
	public static String getNameExpressionValue(Integer playId,String oddsType,String addition1) {
		if(addition1 == null ) return null;
		String nameExpressionValue = addition1;
		
		if(!NumberUtils.isNumber(addition1)) return addition1;
		
		try {
			if("X".equalsIgnoreCase(oddsType) ) {
				nameExpressionValue = new BigDecimal(addition1).multiply(new BigDecimal("-1")).toPlainString();
			}
			
			if("Odd".equals(oddsType)) {//奇数
				nameExpressionValue = addition1;
			}else if("Even".equals(oddsType)) {//偶数
				nameExpressionValue = addition1;
			}else if("1".equals(oddsType)) {//让球
				nameExpressionValue = addition1;
			}else if("2".equals(oddsType)) {//让球
				nameExpressionValue = new BigDecimal(addition1).multiply(new BigDecimal("-1")).toPlainString();
			}else if("Over".equals(oddsType)) {//大小
				nameExpressionValue = addition1;
			}else if("Under".equals(oddsType)) {//大小
				nameExpressionValue = addition1;
			}
			
			return nameExpressionValue;
		}finally {
			if(StringUtils.isBlank(nameExpressionValue)) return nameExpressionValue;
			if(new Double(nameExpressionValue) % 1 / 1 == 0) nameExpressionValue = nameExpressionValue + ".0";
		}
	}
	
	public static void main(String[] args) {
		System.out.println(getTextParseToNumber("0"));
	}

}

