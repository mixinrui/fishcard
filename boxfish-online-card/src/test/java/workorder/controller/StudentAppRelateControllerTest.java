package workorder.controller;

import com.boxfishedu.mall.enums.ComboTypeToRoleId;
import com.boxfishedu.workorder.common.util.JSONParser;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.base.ReturnCode;
//import com.boxfishedu.order.entity.Order;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.web.param.SelectedTime;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by zijun.jiao on 16/6/10.
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(ServiceApplication.class)
@WebIntegrationTest("server.port:8080")
public class StudentAppRelateControllerTest {
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
     * 学生端批量选择课程的接口
     * TODO:1.获取课程的接口为假数据 2.获取教师的时候需要根据coursetype把外教区分出来,并且体现到workorder和course_schedule的冗余表里
     */
    @Test
    //  @RequestMapping(value = "/student/workorders", method = RequestMethod.POST)
    public void ensureCourseTimesTest()throws Exception{

        TimeSlotParam timeSlotParam = new TimeSlotParam();
        timeSlotParam.setOrderId(232l);
        timeSlotParam.setComboType(ComboTypeToRoleId.OVERALL.name());


        List<SelectedTime> selectedTimes = Lists.newArrayList();
        SelectedTime selectedTime = new SelectedTime();
        selectedTime.setTimeSlotId(2);
        selectedTime.setSelectedDate("2012-09-09 00:00:00");
        selectedTimes.add(selectedTime);

        timeSlotParam.setSelectedTimes(selectedTimes);

        //试图更新一个不存在的订单时,出现400错误
        this.mockMvc.perform(MockMvcRequestBuilders.post("/service/student/workorders")
                .contentType(MediaType.APPLICATION_JSON_UTF8).content(JSONParser.toJson(timeSlotParam)))
                .andExpect(status().isOk());
    }




    @Test
    //   {student_Id}/schedule/month
    public void courseScheduleListTest()throws  Exception{
        logger.info("***********testTeacherRequestClass*************");
        StringBuilder stringBuilder = new StringBuilder(urlConf.getFishcard_service()).
                append("/service/student").
                append(1296443).
                append("/schedule/month").
                append("?test=true");

        logger.info("测试接口正常结果:");
        JsonResultModel jsonResultModel = restTemplate.getForObject(stringBuilder.toString(), JsonResultModel.class);
        Assert.isTrue(jsonResultModel.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.isInstanceOf(List.class, jsonResultModel.getData());
        Assert.notNull(jsonResultModel.getData());

    }

    @Test
    //  time/available
    public void timeAvailableTest()throws  Exception{
        logger.info("***********timeAvailableTest*************");
        StringBuilder stringBuilder = new StringBuilder(urlConf.getFishcard_service()).
                append("?test=true").
                append("/service/student/time/available").
                append("&test=true").
                append("&studentId=").append(1010236).
                append("&roleId=").append(1).

                append("&userId").append(223322);
        /*
            private Long studentId;
            private Long roleId;
            //是否免费
            private Boolean isFree;
            private Long orderId;
            private String date;

        */

        logger.info("测试接口正常结果:");
        JsonResultModel jsonResultModel = restTemplate.getForObject(stringBuilder.toString(), JsonResultModel.class);
        Assert.isTrue(jsonResultModel.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.isInstanceOf(List.class, jsonResultModel.getData());
        Assert.notNull(jsonResultModel.getData());

    }


    @Test
    public void getFinishCourseSchedulePageTest()throws  Exception{
        logger.info("***********getFinishCourseSchedulePageTest*************");
        StringBuilder stringBuilder = new StringBuilder(urlConf.getFishcard_service()).
                append("/service/student/schedule/finish/page").
                append("?test=true").
                append("&userId=").append(1010236).
                append("&page=0&size=10") ;

        logger.info("测试接口正常结果:");
        JsonResultModel jsonResultModel = restTemplate.getForObject(stringBuilder.toString(), JsonResultModel.class);
        Assert.isTrue(jsonResultModel.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.isInstanceOf(List.class, jsonResultModel.getData());
        Assert.notNull(jsonResultModel.getData());

    }

    @Test
    public void getUnFinishCourseSchedulePageTest()throws  Exception{
        logger.info("***********getUnFinishCourseSchedulePageTest*************");
        StringBuilder stringBuilder = new StringBuilder(urlConf.getFishcard_service()).
                append("/service/student/schedule/unfinish/page").
                append("?test=true").
                append("&userId=").append(1010236).
                append("&page=0&size=10") ;

        logger.info("测试接口正常结果:");
        JsonResultModel jsonResultModel = restTemplate.getForObject(stringBuilder.toString(), JsonResultModel.class);
        Assert.isTrue(jsonResultModel.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.isInstanceOf(List.class, jsonResultModel.getData());
        Assert.notNull(jsonResultModel.getData());

    }

    @Test
    public void existsCourseScheduleTest()throws  Exception{
        logger.info("***********getUnFinishCourseSchedulePageTest*************");
        StringBuilder stringBuilder = new StringBuilder(urlConf.getFishcard_service()).
                append("/service/student/schedule/exists").
                append("?test=true").
                append("&userId=").append(1010236);

        logger.info("测试接口正常结果:");
        JsonResultModel jsonResultModel = restTemplate.getForObject(stringBuilder.toString(), JsonResultModel.class);
        Assert.isTrue(jsonResultModel.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.isInstanceOf(List.class, jsonResultModel.getData());
        Assert.notNull(jsonResultModel.getData());

    }





}
