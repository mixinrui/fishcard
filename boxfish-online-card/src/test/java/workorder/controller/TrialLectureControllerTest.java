package workorder.controller;

import com.boxfishedu.workorder.common.util.JSONParser;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.web.param.TrialLectureParam;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by zijun.jiao on 16/6/10.
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(ServiceApplication.class)
@WebIntegrationTest("server.port:8080")
public class TrialLectureControllerTest {
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
     * 新建鱼卡
     * @throws Exception
     */
    @Test
    public void buildFishCardTest()throws Exception{

        TrialLectureParam  trialLectureParam = new TrialLectureParam();
        /**
             private Long teacherId;
             private Long studentId;
             private String courseId;
             private String courseName;
             private String startTime;
             private String endTime;
             private String courseType;
             private Integer timeSlotId;
         */
        trialLectureParam.setTeacherId(978112L);
        trialLectureParam.setStudentId(1296443L);
        trialLectureParam.setCourseId("L3NoYXJlL3N2bi9Cb3hmaXNo6K--56iL5qih5p2_LzAwMC7miYDmnInmqKHmnb_lkIjovpEueGxzeA");
        trialLectureParam.setCourseName("测试课程");
        trialLectureParam.setStartTime("2016-09-01 00:00:00");
        trialLectureParam.setEndTime  ("2016-09-01 01:00:00");
        trialLectureParam.setCourseType("1");
        this.mockMvc.perform(MockMvcRequestBuilders.post("/service/trial")
                .contentType(MediaType.APPLICATION_JSON_UTF8).content(JSONParser.toJson(trialLectureParam)))
                .andExpect(status().isOk());
    }

    /**
     * 修改鱼卡
     * @throws Exception
     */
    @Test
    public void modifyFishCardTest()throws Exception{

        TrialLectureParam  trialLectureParam = new TrialLectureParam();
        /**
         private Long teacherId;
         private Long studentId;
         private String courseId;
         private String courseName;
         private String startTime;
         private String endTime;
         private String courseType;
         private Integer timeSlotId;
         */
        trialLectureParam.setTeacherId(978112L);
        trialLectureParam.setStudentId(1296443L);
        trialLectureParam.setCourseId("L3NoYXJlL3N2bi9Cb3hmaXNo6K--56iL5qih5p2_LzAwMC7miYDmnInmqKHmnb_lkIjovpEueGxzeA");
        trialLectureParam.setCourseName("测试课程");
        trialLectureParam.setStartTime("2016-09-01 00:00:00");
        trialLectureParam.setEndTime  ("2016-09-01 01:00:00");
        trialLectureParam.setCourseType("1");
        this.mockMvc.perform(MockMvcRequestBuilders.put("/service/trial")
                .contentType(MediaType.APPLICATION_JSON_UTF8).content(JSONParser.toJson(trialLectureParam)))
                .andExpect(status().isOk());
    }


    /**
     * 删除鱼卡
     * @throws Exception
     */
    @Test
    public void deleteFishCardTest()throws Exception{

        TrialLectureParam  trialLectureParam = new TrialLectureParam();
        /**
         private Long teacherId;
         private Long studentId;
         private String courseId;
         private String courseName;
         private String startTime;
         private String endTime;
         private String courseType;
         private Integer timeSlotId;
         */
        trialLectureParam.setTeacherId(978112L);
        trialLectureParam.setStudentId(1296443L);
        trialLectureParam.setCourseId("L3NoYXJlL3N2bi9Cb3hmaXNo6K--56iL5qih5p2_LzAwMC7miYDmnInmqKHmnb_lkIjovpEueGxzeA");
        trialLectureParam.setCourseName("测试课程");
        trialLectureParam.setStartTime("2016-09-01 00:00:00");
        trialLectureParam.setEndTime  ("2016-09-01 01:00:00");
        trialLectureParam.setCourseType("1");
        this.mockMvc.perform(MockMvcRequestBuilders.delete("/service/trial")
                .contentType(MediaType.APPLICATION_JSON_UTF8).content(JSONParser.toJson(trialLectureParam)))
                .andExpect(status().isOk());
    }



}
