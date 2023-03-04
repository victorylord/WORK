package com.panda.sport.rcs.config;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.LocaleResolver;

import com.panda.sport.rcs.constants.I18iConstants;

/**
 * 	国际化配置
 * 
 * @author Z9-jordan
 *
 */
@Configuration
public class LocaleResolverConfig implements LocaleResolver {

    @Autowired
    private HttpServletRequest request;

    public Locale getLocal() {
        return resolveLocale(request);
    }

    /**
     * 	获取请求头的lang
     */
    @Override
    public Locale resolveLocale(HttpServletRequest httpServletRequest) {
    	if (httpServletRequest == null) {
    		return Locale.SIMPLIFIED_CHINESE;
    	}
        //获取请求中的语言参数
        String lang = httpServletRequest.getHeader(I18iConstants.HEADER_LANG);
        Locale locale = null;
	  	if (I18iConstants.LANG_EN.equals(lang)) {
	  		locale = Locale.US;
	  	} else {
	  		locale = Locale.SIMPLIFIED_CHINESE;
	  	}
        return locale;
    }

    @Override
    public void setLocale(@NonNull HttpServletRequest request, @Nullable HttpServletResponse httpServletResponse, @Nullable Locale locale) {

    }
}
