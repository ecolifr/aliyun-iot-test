package com.ecolifr.manage.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


/**
 * @author Xie ZuoZhi
 * @date 2018/12/12 14:08
 * @description
 */
@Data
@Configuration
public class AliyunProperties {
    @Value("${aliyun.access_key}")
    private String accessKey;
    @Value("${aliyun.access_secret}")
    private String accessSecret;
    @Value("${aliyun.endpoint_name}")
    private String endpointName;
    @Value("${aliyun.region_id}")
    private String regionId;
    @Value("${aliyun.product}")
    private String product;
    @Value("${aliyun.domain}")
    private String domain;
    @Value("${aliyun.product_key}")
    private String productKey;
    @Value("${aliyun.device_name}")
    private String deviceName;
    @Value("${aliyun.device_secret}")
    private String deviceSecret;

    @Value("${aliyun.uid}")
    private String uid;
}
