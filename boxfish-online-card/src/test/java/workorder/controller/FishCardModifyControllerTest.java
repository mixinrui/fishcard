package workorder.controller;

/**
 * Created by zijun.jiao on 16/6/7.
 */

import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.web.param.TeacherChangeParam;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

/**
 * 与上课相关的流程在此处理单元测试(CourseOnlineController.java)
 * Created by zijun.jiao on 16/6/6.
 *
 */

//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(ServiceApplication.class)
@WebIntegrationTest("server.port:8080")
public class FishCardModifyControllerTest {

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
     * 更新订单 排程 (教师 订单) 关联 关系
     * @throws Exception
     */
    @Test
 //   @RequestMapping(value = "/backend/fishcard/teacher", method = RequestMethod.PUT)
    public void testchangeTeacher()throws Exception{
        StringBuilder stringbuilder = new StringBuilder(urlConf.getFishcard_service()).append("/backend/fishcard/teacher");
        logger.info("测试接口[{}]正常结果:", stringbuilder.toString());
        TeacherChangeParam teacherchangeparam = new TeacherChangeParam();
        teacherchangeparam.setTeacherId(333698l);
        teacherchangeparam.setWorkOrderId(3979l);
        teacherchangeparam.setTeacherName("测试老师姓名");
        restTemplate.put(stringbuilder.toString(), teacherchangeparam);
    }





}
