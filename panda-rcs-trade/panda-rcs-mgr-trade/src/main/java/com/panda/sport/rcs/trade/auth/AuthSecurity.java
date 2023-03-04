package com.panda.sport.rcs.trade.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.panda.sports.auth.permission.AuthSecurityInterceptor;

@Component
public class AuthSecurity {
	
	@Bean("authInterceptorBean")
	public HandlerInterceptor create() {
		return new AuthSecurityInterceptor();
	}

}
