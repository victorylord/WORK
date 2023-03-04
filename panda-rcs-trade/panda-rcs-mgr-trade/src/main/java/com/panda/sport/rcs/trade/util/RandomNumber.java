package com.panda.sport.rcs.trade.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RandomNumber {

	private static int LENGTH = 6;
	
	/**
	 * 随机数方法1：返回整数从1到指定长度的数值
	 * 例如:length = 4,则返回0001-9999之间的四位字符串
	 * 	   length = 2,则返回01-99之间的两位字符串
	 * @param length
	 * @return
	 */
	public static String randomNum4Len1(int length) {
		if(length < 1) {
			length = LENGTH;
		}
		String randomNum = randomNum(length);
		if(randomNum.length() < length) {
			return leftAddZeroForNum(randomNum, length);
		}
		return randomNum;
	}
	
	/**
	 * 随机数方法2：返回整数从指定长度的最小值到指定长度的最大值
	 * 例如:length = 4,则返回1000-9999之间的四位字符串
	 * 	   length = 2,则返回10-99之间的两位字符串
	 * @param length
	 * @return
	 */
	public static String randomNum4Len2(int length) {
		if(length < 1) {
			length = LENGTH;
		}
		String randomNum = randomNum(length);
		if(randomNum.length() < length) {
			return String.valueOf(Long.parseLong(randomNum) + Long.parseLong("1" + leftAddZeroForNum("", length - 1)));
		}
		return randomNum;
	}
	
	/**
	 * 生成随机数
	 * @param length 长度
	 * @return 返回随机生成的数字
	 * 例如:
	 *	length = 6,则   0 < 返回值 < 999999，之间的整数
	 *  length = 4,则   0 < 返回值 < 9999，之间的整数
	 */
	private static String randomNum(int length) {
		if(length < 1) {
			length = LENGTH;
		}
		long nultiNum = Long.parseLong("1" + leftAddZeroForNum("", length));
		return String.valueOf((long)(Math.random() * nultiNum));
	}
	
	/**
	 * 左补零,如果为null或者则为""或者不是一个数字，则默认返回长度位length个0
	 * 		反之在数字的左边补上零返回
	 * @param length
	 */
	private static String leftAddZeroForNum(String num, int length) {
		if(length < 1) {
			length = LENGTH;
		}
		if(null == num || "".equals(num) || !isNumeric(num)) {
			num = "0";
		}
		return String.format("%0"+length+"d", Long.parseLong(num));
	}
	
	/**
	 * 判断字符串是否为数字(正数为true，负数为false),小数也可以
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str) {
		//正负数都可以
		//Pattern pattern = Pattern.compile("-[0-9]+\\.?[0-9]*");
		//只能是正数
		Pattern pattern = Pattern.compile("[0-9]+\\.?[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}

}