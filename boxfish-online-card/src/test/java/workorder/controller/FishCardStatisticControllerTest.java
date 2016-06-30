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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

/**
 * Created by zijun.jiao on 16/6/6.
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(ServiceApplication.class)
//@WebIntegrationTest("server.port:8080")
public class FishCardStatisticControllerTest {

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
     * 用于结算中心
     * @throws Exception
     */
    @Test
   // @RequestMapping(value = "/balance/fishcard/list" ,method = RequestMethod.GET)
    public void testBalanceFishCardList()throws Exception{
        logger.info("***********开始listFishCardsByCond*************");
        // http://localhost:8080/balance/fishcard/list?teacherId=1133365&studentId=1296384&beginDate=2016-05-26&endDate=2016-06-24&test=true
        StringBuilder stringBuilder = new StringBuilder(urlConf.getFishcard_service()).append("/balance/fishcard/list?").append("teacherId=1133365&studentId=1296384&beginDate=2016-05-26&endDate=2016-06-24&test=true");
        logger.info("测试接口正常结果:");
        JsonResultModel jsonResultModel = restTemplate.getForObject(stringBuilder.toString(), JsonResultModel.class);
        Assert.isTrue(jsonResultModel.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.isInstanceOf(List.class, jsonResultModel.getData());
        Assert.notNull(jsonResultModel.getData());
    }
}

