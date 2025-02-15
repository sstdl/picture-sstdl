package com.sstdl.picturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.sstdl.picturebackend.mapper")
@EnableAspectJAutoProxy(exposeProxy = true) // 可使用代理对象
public class PictureBackendSstdlApplication {

    public static void main(String[] args) {
        SpringApplication.run(PictureBackendSstdlApplication.class, args);
    }

}
