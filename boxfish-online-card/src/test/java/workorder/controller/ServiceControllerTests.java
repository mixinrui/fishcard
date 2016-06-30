package workorder.controller;

import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.base.ReturnCode;
import com.boxfishedu.online.order.entity.OrderForm;
import com.boxfishedu.workorder.common.config.UrlConf;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.List;

//
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(ServiceApplication.class)
@WebIntegrationTest("server.port:8080")
public class ServiceControllerTests {

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
    public void listBeans() {
        String[] beanNames = context.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            System.out.println(beanName);
        }
    }

    //@RequestMapping(value = "/order/services", method = RequestMethod.POST)
    public void generatorService(@RequestBody OrderForm orderView) throws Exception {

    }

    @Test
    //@RequestMapping(value = "/order/{order_id}/services", method = RequestMethod.GET)
    public void getServicesByOrder() throws Exception {
        logger.info("***********开始getServicesByOrder*************");
        StringBuilder stringBuilder = new StringBuilder(urlConf.getFishcard_service()).append("/com.boxfishedu.order.service/order/").append(101).append("/services");
        logger.info("测试接口正常结果:");
        JsonResultModel jsonResultModel = restTemplate.getForObject(stringBuilder.toString(), JsonResultModel.class);
        Assert.isTrue(jsonResultModel.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.isInstanceOf(List.class, jsonResultModel.getData());
        Assert.notNull(jsonResultModel.getData());
    }

    //@RequestMapping(value = "/order/{order_id}/com.boxfishedu.order.service/{service_type}/workorders", method = RequestMethod.GET)
    public void getServiceWOrkOrderByOrder() throws Exception {
        logger.info("***********getServiceWOrkOrderByOrder*************");
        StringBuilder stringBuilder = new StringBuilder(urlConf.getFishcard_service()).append("/order/").append(30).append("/com.boxfishedu.order.service/").append(13).append("/workorders");
        logger.info("测试接口正常结果:");
        JsonResultModel jsonResultModel = restTemplate.getForObject(stringBuilder.toString(), JsonResultModel.class);
        Assert.isTrue(jsonResultModel.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.isInstanceOf(Pageable.class, jsonResultModel.getData());
        Assert.notNull(jsonResultModel.getData());
    }
}
