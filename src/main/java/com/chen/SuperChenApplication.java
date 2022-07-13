package com.chen;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@EnableCaching
@SpringBootApplication
@EnableTransactionManagement
@ServletComponentScan
public class  SuperChenApplication{
    public static void main(String[] args) {
        SpringApplication.run(SuperChenApplication.class,args);
        log.info("项目启动成功...");
    }
}
