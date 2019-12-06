package com.ecolifr.manage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Xie ZuoZhi
 * @date 2018/12/12 16:06
 * @description
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ManageApplication.class)
public class IotManageTest {
    @Autowired
    private Manage manage;

    @Test
    public void send() {
        manage.send();
    }

    @Test
    public void registerDevice() {
        manage.registerDevice("s33s");
    }
    @Test
    public void deleteDevice() {
        manage.deleteDevice("s33s");
    }

    @Test
    public void queryDevice() {
        manage.queryDevice(10, 1);
    }

}
