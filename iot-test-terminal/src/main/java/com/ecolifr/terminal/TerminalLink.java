package com.ecolifr.terminal;

import com.aliyun.alink.dm.api.DeviceInfo;
import com.aliyun.alink.dm.api.InitResult;
import com.aliyun.alink.linkkit.api.*;
import com.aliyun.alink.linksdk.channel.core.persistent.mqtt.MqttNet;
import com.aliyun.alink.linksdk.cmp.core.base.AMessage;
import com.aliyun.alink.linksdk.cmp.core.base.ConnectState;
import com.aliyun.alink.linksdk.cmp.core.listener.IConnectNotifyListener;
import com.aliyun.alink.linksdk.tmp.device.payload.ValueWrapper;
import com.aliyun.alink.linksdk.tools.AError;
import com.aliyun.alink.linksdk.tools.ALog;
import com.ecolifr.terminal.configuration.AliyunProperties;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static com.aliyun.alink.linksdk.tmp.utils.TmpConstant.TAG;

/**
 * @author Xie ZuoZhi
 * @date 2018/11/29 15:00
 * @description
 */
@Component
public class TerminalLink {
    private final Logger logger = LoggerFactory.getLogger(getClass());


    private final AliyunProperties aliyunProperties;

    public TerminalLink(AliyunProperties aliyunProperties) {
        this.aliyunProperties = aliyunProperties;
    }

    @PostConstruct
    public void link() {
        final String topicDiy = "/" + aliyunProperties.getProductKey() + "/" + aliyunProperties.getDeviceName() + "/diy";

        LinkKitInitParams params = new LinkKitInitParams();
        /**
         * 设置 Mqtt 初始化参数
         */
        IoTMqttClientConfig config = new IoTMqttClientConfig();
        config.productKey = aliyunProperties.getProductKey();
        config.deviceName = aliyunProperties.getDeviceName();
        config.deviceSecret = aliyunProperties.getDeviceSecret();
        params.mqttClientConfig = config;
        /**
         * 设置初始化三元组信息，用户传入
         */
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.productKey = aliyunProperties.getProductKey();
        deviceInfo.deviceName = aliyunProperties.getDeviceName();
        deviceInfo.deviceSecret = aliyunProperties.getDeviceSecret();
        params.deviceInfo = deviceInfo;
        /**
         * 设置设备当前的初始状态值，属性需要和云端创建的物模型属性一致
         * 如果这里什么属性都不填，物模型就没有当前设备相关属性的初始值。
         * 用户调用物模型上报接口之后，物模型会有相关数据缓存。
         */
        Map<String, ValueWrapper> propertyValues = new HashMap<String, ValueWrapper>();
        // 示例
        // propertyValues.put("LightSwitch", new ValueWrapper.BooleanValueWrapper(0));
        params.propertyValues = propertyValues;
        ILinkKit linkKit = LinkKit.getInstance();


        IConnectNotifyListener notifyListener = new IConnectNotifyListener() {
            @Override
            public void onNotify(String connectId, String topic, AMessage aMessage) {
                logger.info("云端下行数据回调：{},id：{},主题：{}", aMessage, connectId, topic);
            }

            @Override
            public boolean shouldHandle(String connectId, String topic) {
                // 选择是否不处理某个 topic 的下行数据
                // 如果不处理某个topic，则onNotify不会收到对应topic的下行数据
                //TODO 根基实际情况设置
                logger.info("shouldHandle,id：{},主题：{}", connectId, topic);
                return true;
            }

            @Override
            public void onConnectStateChange(String connectId, ConnectState connectState) {
                // 对应连接类型的连接状态变化回调，具体连接状态参考 SDK ConnectState
                logger.info("对应连接类型的连接状态变化回调,id：{},状态：{}", connectId, connectState);
                try {
                    subscribe(topicDiy);
                } catch (MqttException e) {
                    logger.info("订阅消息失败", topicDiy, e);
                }
            }
        };

        linkKit.registerOnNotifyListener(notifyListener);
        linkKit.init(params, new ILinkKitConnectListener() {
            @Override
            public void onError(AError aError) {
                ALog.e(TAG, "Init Error error=" + aError);
            }

            @Override
            public void onInitDone(InitResult initResult) {
                ALog.i(TAG, "onInitDone result=" + initResult);
            }
        });

    }


    /**
     * 取消订阅
     */
    public void unsubscribe(String topic) throws MqttException {
        MqttNet.getInstance().getClient().unsubscribe(topic);
    }

    /**
     * 订阅
     */
    public void subscribe(String topic) throws MqttException {
        MqttNet.getInstance().getClient().subscribe(topic, 0, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                // 注册资源成功
                logger.info("注册资源成功{}----{}", topic, iMqttToken);
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                logger.error("注册失败{}----{}", iMqttToken, throwable);
            }
        }, (s, mqttMessage) -> {
            logger.info("收到消息：topic【{}】,消息：{}", s, mqttMessage);
            MqttNet.getInstance().getClient().publish("/" + aliyunProperties.getProductKey() + "/" + aliyunProperties.getDeviceName() + "/rec", mqttMessage);
        });
    }

}
