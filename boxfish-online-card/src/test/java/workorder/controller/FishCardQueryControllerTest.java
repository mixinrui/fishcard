package workorder.controller;

import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.base.ReturnCode;
import com.boxfishedu.workorder.common.config.UrlConf;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

/**
 * Created by zijun.jiao on 16/6/7.
 */

@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(ServiceApplication.class)
//@WebIntegrationTest("server.port:8080")
public class FishCardQueryControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UrlConf urlConf;

    private MockMvc mockMvc;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * 此接口主要提供给鱼卡中心的管理后台使用,主要包括:鱼卡列表 (换课,换教师,换时间 暂时没有)
     * @throws Exception
     */
    @Test
//    @RequestMapping(value = "/backend/fishcard/list",method = RequestMethod.GET)
    public void testlistFishCardsByCond() throws Exception{
        logger.info("***********testtestlistFishCardsByCond*************");
        StringBuilder stringBuilder = new StringBuilder(urlConf.getFishcard_service()).append("/backend/fishcard/list").
                append("?studentId=736986&teacherId=333698").
                append("&beginDate=2016-05-26&endDate=2016-06-24&page=0&size=10&test=true");
        logger.info("测试接口正常结果:");
        JsonResultModel jsonResultModel = restTemplate.getForObject(stringBuilder.toString(), JsonResultModel.class);
        Assert.isTrue(jsonResultModel.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.isInstanceOf(List.class, jsonResultModel.getData());
        Assert.notNull(jsonResultModel.getData());
    }
}
