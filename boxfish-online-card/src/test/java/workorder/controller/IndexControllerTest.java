package workorder.controller;

import com.boxfishedu.workorder.common.util.JSONParser;
import com.boxfishedu.workorder.common.config.UrlConf;
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
public class IndexControllerTest {
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


    @Test
    //  @RequestMapping(value = "/student/workorders", method = RequestMethod.POST)
    public void ensureCourseTimesTest()throws Exception{
        this.mockMvc.perform(MockMvcRequestBuilders.post("/redis/"+"内容文件")
                .contentType(MediaType.APPLICATION_JSON_UTF8).content(JSONParser.toJson(null)))
                .andExpect(status().isOk());
    }
}
