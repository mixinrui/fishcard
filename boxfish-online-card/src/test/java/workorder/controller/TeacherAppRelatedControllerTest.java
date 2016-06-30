package workorder.controller;

import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.base.ReturnCode;
import com.boxfishedu.workorder.common.config.UrlConf;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

/**
 * Created by zijun.jiao on 16/6/10.
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(ServiceApplication.class)
@WebIntegrationTest("server.port:8080")
public class TeacherAppRelatedControllerTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UrlConf urlConf;

    private MockMvc mockMvc;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Before
    public void initCaptity() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }



    /**
     * 测试
     * 教师端获取一个月的课程表
     * @return 返回带课程标记的课程规划表
     */
    public void evaluateForStudentTest() throws Exception{
        logger.info("*********************evaluateForStudentTest**********");
        StringBuilder stringBuilder = new StringBuilder(urlConf.getFishcard_service()).
                append("/service/teacher").
                append(978112).
                append("/schedule/month").
                append("?test=true").
                append("&userId=").
                append(112121);
        logger.info("测试接口正常结果:");

        JsonResultModel jsonResultModel = restTemplate.getForObject(stringBuilder.toString(),JsonResultModel.class);
        Assert.isTrue(jsonResultModel.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.notNull(jsonResultModel.getData());

    }


    @Test
    public void courseScheduleListTest() throws Exception{
        logger.info("*********************evaluateForStudentTest**********");
        StringBuilder stringBuilder = new StringBuilder(urlConf.getFishcard_service()).
                append("/service/teacher").
                append(1296443).
                append("/schedule/day").
                append("?test=true").
                append("&userId=").
                append(112121).
                append("&date=").
                append("2016-02-03");
        logger.info("测试接口正常结果:");

        JsonResultModel jsonResultModel = restTemplate.getForObject(stringBuilder.toString(),JsonResultModel.class);
        Assert.isTrue(jsonResultModel.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.notNull(jsonResultModel.getData());
    }

    @Test
    public void courseScheduleListAssignTest() throws Exception{
        logger.info("*********************courseScheduleListAssignTest**********");
        StringBuilder stringBuilder = new StringBuilder(urlConf.getFishcard_service()).
                append("/service/teacher").
                append(1296443).
                append("/schedule_assigned/day").
                append("?test=true").
                append("&userId=").
                append(112121).
                append("&date=").
                append("2016-02-03");
        logger.info("测试接口正常结果:");

        JsonResultModel jsonResultModel = restTemplate.getForObject(stringBuilder.toString(),JsonResultModel.class);
        Assert.isTrue(jsonResultModel.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.notNull(jsonResultModel.getData());
    }


    @Test
    public void getDayTimeSlotsTemplateTest() throws Exception{
        logger.info("*********************getDayTimeSlotsTemplateTest**********");
        StringBuilder stringBuilder = new StringBuilder(urlConf.getFishcard_service()).
                append("/service/teacher").
                append(1296443).
                append("/timeSlots/template").
                append("?test=true").
                append("&userId=").
                append(112121).
                append("&date=").
                append("2016-02-03");
        logger.info("测试接口正常结果:");

        JsonResultModel jsonResultModel = restTemplate.getForObject(stringBuilder.toString(),JsonResultModel.class);
        Assert.isTrue(jsonResultModel.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.notNull(jsonResultModel.getData());
    }

    @Test
    public void getInternationalDayTimeSlotsTemplateTest() throws Exception{
        logger.info("*********************getInternationalDayTimeSlotsTemplateTest**********");
        StringBuilder stringBuilder = new StringBuilder(urlConf.getFishcard_service()).
                append("/service/teacher").
                append("/international/").append(1296443).append("/timeSlots/template").
                append("?test=true").
                append("&userId=").
                append(112121).
                append("&date=").
                append("2016-02-03");
        logger.info("测试接口正常结果:");

        JsonResultModel jsonResultModel = restTemplate.getForObject(stringBuilder.toString(),JsonResultModel.class);
        Assert.isTrue(jsonResultModel.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.notNull(jsonResultModel.getData());
    }


    @Test
    public void internationalCourseScheduleListTest() throws Exception{
        logger.info("*********************internationalCourseScheduleListTest**********");
        StringBuilder stringBuilder = new StringBuilder(urlConf.getFishcard_service()).
                append("/service/teacher").
                append("/international/{teacher_id}/").append(1296443).append("/schedule/day").
                append("?test=true").
                append("&userId=").
                append(112121).
                append("&date=").
                append("2016-02-03");
        logger.info("测试接口正常结果:");

        JsonResultModel jsonResultModel = restTemplate.getForObject(stringBuilder.toString(),JsonResultModel.class);
        Assert.isTrue(jsonResultModel.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.notNull(jsonResultModel.getData());
    }
}
