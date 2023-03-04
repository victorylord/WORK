package com.panda.sport.rcs.trade.log.format;

import com.panda.sport.rcs.common.DateUtils;
import lombok.Data;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Data
public class LogFormatPublicBean {
	
	/**
	 * 操作时间
	 */
	private String createTime;
	/**
	 * 操作用户
	 */
	private String uid;
	/**
	 * 操作类别（查询使用）
	 */
	private String logType;
	/**
	 * 操作描述（显示使用）
	 */
	private String logDesc;
	/**
	 * 操作关联id（赛事id）
	 */
	private String logId;

	public LogFormatPublicBean() {
		super();
		this.uid = queryUserIdNoException();
		this.createTime = DateUtils.parseDate(System.currentTimeMillis(), DateUtils.DATE_YYYY_MM_DD_HH_MM_SS);
	}
	
    /**
     * 得到用主户id 如果为空抛错
     */
    public String queryUserIdNoException() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String userId = request.getHeader("user-id");
            if (userId == null) userId = "";
            return userId;
        } catch (Exception e) {
        }
        return "";
    }

	public LogFormatPublicBean(String logType, String logDesc, String logId) {
		this();
		
		this.logType = logType;
		this.logDesc = logDesc;
		this.logId = logId;
	}
    
	
}
