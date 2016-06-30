package workorder.controller;

import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.base.ReturnCode;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.web.view.fishcard.WorkOrderView;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

/**
 * 与上课相关的流程在此处理单元测试(CourseOnlineController.java)
 * Created by zijun.jiao on 16/6/6.
 *
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(ServiceApplication.class)
@WebIntegrationTest("server.port:8080")
public class CourseOnlineControllerTest {

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
     * 通过教师id获取其对应时间段的工单信息
     * @throws Exception
     */
    @Test
 //   @RequestMapping(value = "/coursing/teacher/{teacher_id}/workorders" ,method = RequestMethod.GET)
    public void testgetWorkordersByTeacher()throws Exception{
        logger.info("***********开始lgetWorkordersByTeacher*************");
        // http://localhost:8080/coursing/teacher/1296370/workorders?start=2016-05-26&end=2016-06-24&page=0&size=10&test=true
        StringBuilder stringBuilder = new StringBuilder(urlConf.getFishcard_service()).append("/coursing/teacher/").append(1296370).append("/workorders").append("?start=2016-05-26&end=2016-06-24&page=0&size=10&test=true");
        logger.info("测试接口正常结果:");
        JsonResultModel jsonResultModel = restTemplate.getForObject(stringBuilder.toString(), JsonResultModel.class);
        Assert.isTrue(jsonResultModel.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.isInstanceOf(List.class, jsonResultModel.getData());
        Assert.notNull(jsonResultModel.getData());
    }


    /**
     * 老师请求上课,功能为校验工单的有效性
     */
    @Test
 //   @RequestMapping(value = "/coursing/workorder/{workorder_id}/teacher/class",method = RequestMethod.GET)
    public void testTeacherRequestClass()throws  Exception{
        logger.info("***********testTeacherRequestClass*************");
        // http://localhost:8080/coursing/workorder/4880/teacher/class?test=true
        StringBuilder stringBuilder = new StringBuilder(urlConf.getFishcard_service()).append("/coursing/workorder/").append(4880).append("/teacher/class").append("?test=true");
        logger.info("测试接口正常结果:");
        JsonResultModel jsonResultModel = restTemplate.getForObject(stringBuilder.toString(), JsonResultModel.class);
        Assert.isTrue(jsonResultModel.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.isInstanceOf(List.class, jsonResultModel.getData());
        Assert.notNull(jsonResultModel.getData());

    }





    /**
     * 更新鱼卡的状态,同时保存异常的说明
     * @throws Exception
     */
    @Test
 //   @RequestMapping(value = "/coursing/workorder/status", method = RequestMethod.PUT)
    public void testUpdateWorkOrderStatus() throws Exception {
        // http://localhost:8080/coursing/workorder/4880/teacher/class?test=true
        StringBuilder stringBuilder = new StringBuilder(urlConf.getFishcard_service()).append("/workorder/status");
        logger.info("测试接口[{}]正常结果:", stringBuilder.toString());
        WorkOrderView workOrderView = new WorkOrderView();
        workOrderView.setId(10L);
        workOrderView.setStatus(30);
        workOrderView.setContent("我的最新的内容的哥更新");
        restTemplate.put(stringBuilder.toString(), workOrderView);
        //Assert.isTrue(jsonResultModel.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
    }


    /**
     * 提供上课中心调用 释放资源
     * @throws Exception
     */
    @Test
 //   @RequestMapping(value = "/coursing/workorder/complete", method = RequestMethod.PUT)
    public void testCompleteCourse()throws Exception{
        StringBuilder stringbuilder = new StringBuilder(urlConf.getFishcard_service()).append("/workorder/complete");
        logger.info("测试接口[{}]正常结果:", stringbuilder.toString());
        WorkOrderView workOrderView = new WorkOrderView();
        workOrderView.setId(10L);
        restTemplate.put(stringbuilder.toString(), workOrderView);
    }


    /**
     * 根据订单id查找订单
     * @throws Exception
     */
    @Test
 //   @RequestMapping(value = "/course/schedule/{workOrderId}")
    public void testCourseSchedule()throws  Exception{
        logger.info("***********testTeacherRequestClass*************");
        StringBuilder stringBuilder = new StringBuilder(urlConf.getFishcard_service()).append("/courseschedule/").append(4880).append("?test=true");
        logger.info("测试接口正常结果:");
        JsonResultModel jsonResultModel = restTemplate.getForObject(stringBuilder.toString(), JsonResultModel.class);
        Assert.isTrue(jsonResultModel.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.isInstanceOf(List.class, jsonResultModel.getData());
        Assert.notNull(jsonResultModel.getData());
    }



}
