package com.cqf.office;

import com.cqf.api.config.DefaultFeignConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.cqf.office.mapper")
@EnableFeignClients(basePackages="com.cqf.api.client",defaultConfiguration = DefaultFeignConfig.class)
public class OfficeServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OfficeServiceApplication.class, args);
    }

}
