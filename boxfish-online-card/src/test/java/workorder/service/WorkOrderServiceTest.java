package workorder.service;

//import com.boxfishedu.workorder.ServiceApplication;

import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.web.param.SelectedTime;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(ServiceApplication.class)
@WebIntegrationTest("server.port:8080")
public class WorkOrderServiceTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UrlConf urlConf;

    private MockMvc mockMvc;

    @Autowired
    private ServiceJpaRepository serviceJpaRepository;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    @Transactional
    public void testGetTeachersByOrder() throws Exception {
        Service service=serviceJpaRepository.findByIdForUpdate(22L);
        service.setAmount(service.getAmount() - 1);
        service.setUpdateTime(new Date());
        Thread.sleep(60*1000);
        serviceJpaRepository.save(service);
    }

    public void testEnsureCourseTimes(){
        TimeSlotParam timeSlotParam=new TimeSlotParam();
        SelectedTime selectedTime=new SelectedTime();
    }
}