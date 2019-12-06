package com.ecolifr.manage;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * @author Xie ZuoZhi
 * @date 2018/11/30 14:01
 * @description
 */
@SpringBootApplication
public class ManageApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(ManageApplication.class).registerShutdownHook(true).application().run(args);
    }
}
