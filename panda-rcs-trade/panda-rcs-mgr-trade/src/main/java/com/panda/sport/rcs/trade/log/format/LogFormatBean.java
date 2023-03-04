package com.panda.sport.rcs.trade.log.format;

import lombok.Data;

@Data
public class LogFormatBean {
	
	/**
	 * 操作参数
	 */
	private String name;
	/**
	 * 操作前值
	 */
	private String oldVal;
	/**
	 * 操作后值
	 */
	private String newVal;
	/**
	 * 格式化值
	 */
	private String format;

	public LogFormatBean(String name, String oldVal, String newVal, String format) {
		super();
		this.name = name;
		this.oldVal = oldVal;
		this.newVal = newVal;
		this.format = format;
	}

	public LogFormatBean(String name, String oldVal, String newVal) {
		super();
		this.name = name;
		this.oldVal = oldVal;
		this.newVal = newVal;
	}

	public LogFormatBean() {
		super();
	}
	
}
