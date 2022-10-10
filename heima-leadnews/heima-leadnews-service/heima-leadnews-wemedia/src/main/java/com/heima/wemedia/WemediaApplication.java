package com.heima.wemedia;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Created on 2022/9/9.
 *
 * @author Chen Gao
 */
@SpringBootApplication
@MapperScan("com.heima.wemedia.mapper")
@EnableFeignClients("com.heima.apis")
@EnableAsync
@EnableScheduling
@EnableTransactionManagement(proxyTargetClass = true)
public class WemediaApplication {

    public static void main(String[] args) {
        SpringApplication.run(WemediaApplication.class,args);
    }
}
