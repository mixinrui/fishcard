package workorder.controller;

import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.util.JSONParser;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.base.ReturnCode;
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
public class CommonServiceControllerTest {
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
    public void getWorkorderByIdTest()throws  Exception{
        logger.info("***********getWorkorderByIdTest*************");
        StringBuilder stringBuilder = new StringBuilder(urlConf.getFishcard_service()).
                append("/service/common/").
                append(6066).
                append("?test=true").
                append("&userId=").
                append(232323);

        logger.info("测试接口正常结果:");
        JsonResultModel jsonResultModel = restTemplate.getForObject(stringBuilder.toString(), JsonResultModel.class);
        Assert.isTrue(jsonResultModel.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.isInstanceOf(List.class, jsonResultModel.getData());
        Assert.notNull(jsonResultModel.getData());

    }


    /**
     * 根据订单id获取剩余课程
     * @throws Exception
     */
    @Test
    public void getAmountofSurplusTest()throws Exception{

        List<Long> list =  Lists.newArrayList();
        list.add(6077L);
        list.add(6078L);

        //试图更新一个不存在的订单时,出现400错误
        this.mockMvc.perform(MockMvcRequestBuilders.post("/service/common/surplus")
                .contentType(MediaType.APPLICATION_JSON_UTF8).content(JSONParser.toJson(list)))
                .andExpect(status().isOk());
    }







}
