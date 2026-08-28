package com.tf2autobot.dbmanager.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ApiTokenFilter apiTokenFilter;

    public WebConfig(ApiTokenFilter apiTokenFilter) {
        this.apiTokenFilter = apiTokenFilter;
    }

    @Bean
    public FilterRegistrationBean<ApiTokenFilter> apiTokenFilterRegistration() {
        FilterRegistrationBean<ApiTokenFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(apiTokenFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
}
