package com.panda.sport.rcs.pojo.test;

import com.panda.sport.rcs.log.annotion.format.LogFormatAnnotion;

import lombok.Data;

@Data
@LogFormatAnnotion
public class RcsLogFormatTest {
	
	private String id ;
	
	@LogFormatAnnotion(name = "名字" )
	private String name ; 
	
	@LogFormatAnnotion(name = "身份Id" ,format = "Id Num : %s" )
	private String idcard ;
	
	@LogFormatAnnotion(name = "年龄" )
	private String age;
	
	@LogFormatAnnotion(name = "性别" ,isIgnoreBlank = false )
	private String sex;
	
	private String height;

	public RcsLogFormatTest(String id, String name, String idcard, String age, String sex) {
		super();
		this.id = id;
		this.name = name;
		this.idcard = idcard;
		this.age = age;
		this.sex = sex;
	}
	
	public RcsLogFormatTest() {
		super();
	}
}
