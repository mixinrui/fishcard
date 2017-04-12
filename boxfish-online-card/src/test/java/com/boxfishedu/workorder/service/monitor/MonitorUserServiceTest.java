package com.boxfishedu.workorder.service.monitor;

import com.boxfishedu.workorder.ServiceApplication;
import com.boxfishedu.workorder.entity.mysql.MonitorUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by ansel on 2017/4/11.
 */
@SpringApplicationConfiguration(ServiceApplication.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class MonitorUserServiceTest {
    @Test
    public void getTeacherAppRelease() throws Exception {
        monitorUserService.getTeacherAppRelease(Arrays.asList(2484405, 11212682));
    }

    @Autowired
    private MonitorUserService monitorUserService;



}