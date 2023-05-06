/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@ConditionalOnExpression("'${spring.profiles.active}'!='online'")
public class SwaggerConfig {

    @Value("${swagger-ui.host}")
    private String host;

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2).groupName("game")
                .apiInfo(apiInfo())
                .host(host)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.soft.game.controller"))
                .paths(PathSelectors.regex("/.*"))
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("game")
                .description("游戏")
                .version("1.0")
                .contact(new Contact("simon", "", ""))
                // .termsOfServiceUrl("localhost:8080")
                .build();
    }
}