package com.ttcode.govern.center;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author : xingsongtan@qq.com
 * @date : 22:56 2019/7/21
 */
@EnableEurekaServer
@SpringBootApplication
public class CenterApplication {
    public static void main(String[] args) {
        SpringApplication.run(CenterApplication.class, args);
    }

}
