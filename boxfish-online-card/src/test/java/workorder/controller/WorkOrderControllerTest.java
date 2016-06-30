package workorder.controller;

import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.web.view.fishcard.WorkOrderView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

//import com.boxfishedu.workorder.ServiceApplication;

/**
 * Created by hucl on 16/3/29.
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(ServiceApplication.class)
@WebIntegrationTest("server.port:8080")
public class WorkOrderControllerTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UrlConf urlConf;

    private MockMvc mockMvc;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Before
    public void setupMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    //@RequestMapping(value = "/modification/teacher/list/{workorder_id}", method = RequestMethod.GET)
    public void getTeachersByOrder() throws Exception {
        String url = String.format("%s/modification/teacher/list/%s",urlConf.getFishcard_service(),15);
        JsonResultModel jsonResultModel=restTemplate.getForObject(url,JsonResultModel.class);
        Assert.isInstanceOf(Pageable.class,jsonResultModel);
    }

    //@RequestMapping(value = "/student/com.boxfishedu.order.service/{service_id}/date/{selected_date}/time/{time_slot_id}", method = RequestMethod.POST)
    public void ensureCourseTime() throws Exception {

    }

    //@RequestMapping(value = "/evaluation/teacher/{work_order_id}}", method = RequestMethod.POST)
    public JsonResultModel evaluate2Teacher() throws Exception {
        return null;
    }

    //@RequestMapping(value = "/evaluation/student/{work_order_id}}", method = RequestMethod.POST)
    public JsonResultModel evaluate2Student(@PathVariable("work_order_id") Long workOrderId) throws Exception {
        return null;
    }

    @Test
    //@RequestMapping(value = "/{workorder_id}/teacher/{teacher_id}/name/{teacher_name}", method = RequestMethod.PUT)
    public void updateTeacher2Workorder() throws Exception {
        StringBuilder stringBuilder = new StringBuilder(urlConf.getFishcard_service()).append("/workorder/").append(15).append("/teacher/").append(15).append("/name/").append("我是测试老师啊");
        logger.info("测试接口[{}]正常结果:", stringBuilder.toString());
        restTemplate.put(stringBuilder.toString(), null);
        //Assert.isTrue(jsonResultModel.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
    }

    @Test
    //@RequestMapping(value = "/{workorder_ids}/courses", method = RequestMethod.PUT)
    public void updateCourse2Workorder() throws Exception {
        String url = String.format("%s/workorder/%s/courses",urlConf.getFishcard_service(),"15,16");
        restTemplate.put(url.toString(), null);
    }

    @Test
    //@RequestMapping(value = "/{workorder_id}", method = RequestMethod.GET)
    public void getWorkorderById(@PathVariable("workorder_id") Long workorderId) throws Exception {
        String url = String.format("%s/workorder/%s",urlConf.getFishcard_service(),15);
        JsonResultModel jsonResultModel=restTemplate.getForObject(url,JsonResultModel.class);
        Assert.isInstanceOf(WorkOrderView.class,jsonResultModel);
    }

}
