package com.panda.sport.rcs.trade.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.swagger.annotations.ApiOperation;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
@Profile({"test"})
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        List<Parameter> pars = new ArrayList<>();

        ParameterBuilder ticketPar1 = new ParameterBuilder();
        ParameterBuilder ticketPar2 = new ParameterBuilder();
        ParameterBuilder ticketPar3 = new ParameterBuilder();
        ParameterBuilder ticketPar4 = new ParameterBuilder();

        ticketPar1.name("request-id").description("请求随机数，每次请求都不一样")
                .modelRef(new ModelRef("string")).parameterType("header")
                .required(false).build();
        ticketPar2.name("lang").description("语言标识")
                .modelRef(new ModelRef("string")).parameterType("header")
                .required(false).build();
        ticketPar3.name("user-id").description("登陆人id")
                .modelRef(new ModelRef("string")).parameterType("header")
                .required(true).build();
        ticketPar4.name("app-id").description("app-id")
                .modelRef(new ModelRef("string")).parameterType("header")
                .required(true).build();

        pars.add(ticketPar1.build());
        pars.add(ticketPar2.build());
        pars.add(ticketPar3.build());
        pars.add(ticketPar4.build());

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.panda"))
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build()
                .globalOperationParameters(pars)
                .enable(true);
    }


    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("风控&操盘手动执行Api接口")
                .description("初始化数据接口调用")
                .version("1.0")
                .build();
    }
}

