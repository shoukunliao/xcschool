package com.xuecheng;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@EnableSwagger2Doc
@SpringBootApplication
@MapperScan({"com.xuecheng.content.mapper"})
public class XuechengPlusContentApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(XuechengPlusContentApiApplication.class, args);
    }

}
