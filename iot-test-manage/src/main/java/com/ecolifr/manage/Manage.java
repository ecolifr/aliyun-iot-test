package com.ecolifr.manage;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.iot.api.Profile;
import com.aliyun.openservices.iot.api.message.MessageClientFactory;
import com.aliyun.openservices.iot.api.message.api.MessageClient;
import com.aliyun.openservices.iot.api.message.callback.MessageCallback;
import com.aliyun.openservices.iot.api.message.entity.Message;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.iot.model.v20180120.*;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import com.ecolifr.manage.configuration.AliyunProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Objects;

/**
 * @author Xie ZuoZhi
 * @date 2018/11/29 16:30
 * @description
 */
@Component
public class Manage {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AliyunProperties aliyunProperties;

    private final DefaultAcsClient client;

    public Manage(AliyunProperties aliyunProperties) {
        this.aliyunProperties = aliyunProperties;
        IClientProfile profile = DefaultProfile.getProfile(aliyunProperties.getRegionId(), aliyunProperties.getAccessKey(), aliyunProperties.getAccessSecret());
        //初始化SDK客户端
        client = new DefaultAcsClient(profile);
    }

    public void send() {
        try {
            DefaultProfile.addEndpoint(aliyunProperties.getEndpointName(), aliyunProperties.getRegionId(), aliyunProperties.getProduct(), aliyunProperties.getDomain());
        } catch (ClientException e) {
            logger.error("", e);
        }

        PubRequest request = new PubRequest();
        request.setProductKey(aliyunProperties.getProductKey());

        request.setMessageContent(Base64.encode("hello world".getBytes()));
        request.setTopicFullName("/" + aliyunProperties.getProductKey() + "/" + aliyunProperties.getDeviceName() + "/diy");
        //目前支持QoS0和QoS1
        request.setQos(0);
        try {
            PubResponse response = client.getAcsResponse(request);
            logger.info("是否成功：{},失败详情：{},消息id：{}", response.getSuccess(), response.getErrorMessage(), response.getMessageId());
        } catch (ClientException e) {
            logger.error("发送失败", e);
        }
    }

    @PostConstruct
    public void subscribe() {
        String endPoint = String.format("https://%s.iot-as-http2.%s.aliyuncs.com", aliyunProperties.getUid(), aliyunProperties.getRegionId());
        // 连接配置
        Profile profile = Profile.getAccessKeyProfile(endPoint, aliyunProperties.getRegionId(), aliyunProperties.getAccessKey(), aliyunProperties.getAccessSecret());
        // 构造客户端
        MessageClient client = MessageClientFactory.messageClient(profile);
        MessageCallback messageCallback = messageToken -> {
            Message m = messageToken.getMessage();
            logger.info("接收消息: {}", m);
            return MessageCallback.Action.CommitSuccess;
        };
        // 数据接收
        client.connect(messageCallback);
    }

    public void registerDevice(String deviceName) {
        RegisterDeviceRequest request = new RegisterDeviceRequest();
        request.setProductKey(aliyunProperties.getProductKey());
        if (!StringUtils.isEmpty(deviceName)) {
            // 若设备名为空，则不填入该参数，平台自动生成设备名
            request.setDeviceName(deviceName);
        }
        try {
            RegisterDeviceResponse response = client.getAcsResponse(request);
            if (Objects.isNull(response)) {
                logger.error("删除设备失败！");
            } else if (response.getSuccess()) {
                logger.info("创建设备成功！{}", JSONObject.toJSONString(response));
            } else {
                logger.error("创建设备失败！{}", JSONObject.toJSONString(response));
            }
        } catch (Exception e) {
            logger.error("执行失败：", e.getMessage());
        }
    }

    public void queryDevice(Integer pageSize, Integer currentPage) {
        QueryDeviceRequest request = new QueryDeviceRequest();
        request.setProductKey(aliyunProperties.getProductKey());
        request.setPageSize(pageSize);
        request.setCurrentPage(currentPage);
        try {
            QueryDeviceResponse response = client.getAcsResponse(request);
            if (Objects.isNull(response)) {
                logger.error("删除设备失败！");
            } else if (response.getSuccess()) {
                logger.info("查询设备成功！ {}", JSONObject.toJSONString(response));
            } else {
                logger.error("查询设备失败！{}", JSONObject.toJSONString(response));
            }
        } catch (Exception e) {
            logger.error("执行失败：", e.getMessage());
        }
    }

    public void deleteDevice(String deviceName) {
        DeleteDeviceRequest deleteDeviceRequest = new DeleteDeviceRequest();
        deleteDeviceRequest.setDeviceName(deviceName);
        deleteDeviceRequest.setProductKey(aliyunProperties.getProductKey());
        try {
            DeleteDeviceResponse response = client.getAcsResponse(deleteDeviceRequest);
            if (Objects.isNull(response)) {
                logger.error("删除设备失败！");
            } else if (response.getSuccess()) {
                logger.info("删除设备成功！ {}", JSONObject.toJSONString(response));
            } else {
                logger.error("删除设备失败！{}", JSONObject.toJSONString(response));
            }
        } catch (Exception e) {
            logger.error("执行失败：", e.getMessage());
        }
    }
}
