package workorder.controller;

import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.base.ReturnCode;
import com.boxfishedu.workorder.common.config.UrlConf;
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
 * 与订单相关的接口操作,目前主要为与订单系统相关的操作
 * Created by zijun.jiao on 16/6/10.
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(ServiceApplication.class)
@WebIntegrationTest("server.port:8080")
public class OrderRelatedControllerTest {

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
    //  @RequestMapping(value = "/service/order/{order_id}/info", method = RequestMethod.GET)
    public void getOrderConsumeInfoTest()throws Exception{
        logger.info("***********开始lgetWorkordersByTeacher*************");
        StringBuilder stringBuilder = new StringBuilder(urlConf.getFishcard_service()).
                append("/service/order/").
                append(100476).
                append("/info").
                append("&test=true");
        logger.info("测试接口正常结果:");
        JsonResultModel jsonResultModel = restTemplate.getForObject(stringBuilder.toString(), JsonResultModel.class);
        Assert.isTrue(jsonResultModel.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.isInstanceOf(List.class, jsonResultModel.getData());
        Assert.notNull(jsonResultModel.getData());
    }
}

