package com.panda.sport.rcs.utils;


import java.util.Locale;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Component;

import com.panda.sport.rcs.config.LocaleResolverConfig;
import com.panda.sport.rcs.constants.I18iConstants;

import lombok.extern.slf4j.Slf4j;

/**
 * 	国际化工具类
 * 
 * @author Z9-jordan
 *
 */
@Slf4j
@Component
public class I18nUtils {

    @Value("${spring.messages.basename}")
	private String basename;

	private static LocaleResolverConfig customLocaleResolver;

	private static String path;

	public I18nUtils(LocaleResolverConfig customLocaleResolver) {
		this.customLocaleResolver = customLocaleResolver;
	}

	@PostConstruct
	public void init() {
		setBasename(basename);
	}

	/**
	 *	 获取 国际化后内容信息
	 *
	 * @param code 国际化key
	 * @return 国际化后内容信息
	 */
	public static String getMessage(String code) {
		Locale locale = customLocaleResolver.getLocal();
		return getMessage(code, null, code, locale);
	}

	/**
	 * 	获取指定语言中的国际化信息，默认是中文
	 *
	 * @param code 国际化 key
	 * @param lang 语言参数
	 * @return 国际化后内容信息
	 */
	public static String getMessage(String code, String lang) {
		Locale locale;
		if (I18iConstants.LANG_EN.equals(lang)) {
			locale = Locale.US;
		} else {
			locale = Locale.SIMPLIFIED_CHINESE;
		}
		return getMessage(code, null, code, locale);
	}

	private static String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
		ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
		messageSource.setDefaultEncoding("utf-8");
		messageSource.setBasename(path);
		String content;
		try {
			content = messageSource.getMessage(code, args, locale);
		} catch (Exception e) {
			log.error("国际化参数获取失败===>{},{}", e.getMessage(), e);
			content = defaultMessage;
		}
		return content;

	}

	private static void setBasename(String basename) {
		I18nUtils.path = basename;
	}

	public static void setCustomLocaleResolver(LocaleResolverConfig resolver) {
		I18nUtils.customLocaleResolver = resolver;
	}

}
