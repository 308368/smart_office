package com.cqf.api.config;


import feign.Logger;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class DefaultFeignConfig {
    @Bean
    public Logger.Level feignLoggerLevel(){
        return Logger.Level.NONE;
    }
    @Bean
    public RequestInterceptor userInfoRequestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                //获取当前的请求对象
                ServletRequestAttributes attributes =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                System.out.println("=== Feign Interceptor Debug ===");
                System.out.println("attributes: " + attributes);
                if (attributes != null){
                    HttpServletRequest request = attributes.getRequest();
                    String header = request.getHeader("Authorization");
                    if (header != null){
                        requestTemplate.header("Authorization", header);
                    }
                }

            }
        };
    }

}
