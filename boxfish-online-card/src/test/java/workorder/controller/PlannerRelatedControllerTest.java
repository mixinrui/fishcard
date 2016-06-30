package workorder.controller;

import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.base.ReturnCode;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

/**
 * 与课程规划师相关的接口,主要为后台管理相关的接口
 * Created by zijun.jiao on 16/6/10.
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@SpringApplicationConfiguration(ServiceApplication.class)
//@WebIntegrationTest("server.port:8080")

public class PlannerRelatedControllerTest {

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
     * 根据订单id获取服务列表
     * @throws Exception
     */
    @Test
    @RequestMapping(value = "/planner/order/{order_id}/services" , method = RequestMethod.GET)
    public void getServicesByOrderTest() throws Exception{
        logger.info("***********getServicesByOrderTest*************");
        StringBuilder stringBuilder = new StringBuilder(urlConf.getFishcard_service()).append("/planner/order/").append(100476).append("/services").append("?test=true");
        logger.info("测试接口正常结果:");
        JsonResultModel jsonResultModel = restTemplate.getForObject(stringBuilder.toString(), JsonResultModel.class);
        Assert.isTrue(jsonResultModel.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.isInstanceOf(List.class, jsonResultModel.getData());
        Assert.notNull(jsonResultModel.getData());
    }

    /**
     * 根据订单号与指定的类型获取工单列表测试
     *
     * @param orderId
     * @param serviceType 服务类型:1.在线授课 2.在线答疑 3.课程规划 4.欧美外教
     * @return 返回该订单里特定服务的所有工单
     * @param{page:1,size:10}
     */
    @Test
    @RequestMapping(value = "/planner/order/{order_id}/service/{service_type}/workorders" ,method = RequestMethod.GET)
    public void getServiceWorkOrdersByOrderTest()throws  Exception{
        logger.info("******************getServiceWorkOrdersByOrderTest********");
        StringBuilder  stringbuilder = new StringBuilder(urlConf.getFishcard_service()).
                append("/planner/order/").
                append(100476).
                append("/service/").
                append(1).
                append("/workorders").
                append("?&page=0&size=10&test=true");
        logger.info("测试接口正常结果:");
        JsonResultModel  jsonResultMode = restTemplate.getForObject(stringbuilder.toString(),JsonResultModel.class);
        Assert.isTrue(jsonResultMode.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.isInstanceOf(List.class,jsonResultMode.getData());
        Assert.notNull(jsonResultMode.getData());
    }

    /**
     * 根据workorder_id获取教师列表,在换教师部分会用到
     */
    @Test
    @RequestMapping(value = "/planner/workorder/{workorder_id}/avaliable/teachers",method = RequestMethod.GET)
    public void getTeachersByWorkOrderTest()throws  Exception{
        logger.info("******************getTeachersByWorkOrderTest********");
        StringBuilder  stringbuilder = new StringBuilder(urlConf.getFishcard_service()).
                append("/planner/workorder/").
                append(6065).
                append("/avaliable/teachers/").
                append("?&page=0&size=10&test=true");
        logger.info("测试接口正常结果:");
        JsonResultModel  jsonResultMode = restTemplate.getForObject(stringbuilder.toString(),JsonResultModel.class);
        Assert.isTrue(jsonResultMode.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.isInstanceOf(List.class,jsonResultMode.getData());
        Assert.notNull(jsonResultMode.getData());
    }


    /**
     * 更换推荐课程
     */
    @Test
    @RequestMapping(value = "/planner/workorder/courses",method = RequestMethod.PUT)
    public void updateCourseIntoWorkOrderTest()throws  Exception{
        logger.info("******************getTeachersByWorkOrderTest********");
        StringBuilder  stringbuilder = new StringBuilder(urlConf.getFishcard_service()).
                append("/planner/workorder/courses").
                append("?test=true");
        logger.info("测试接口[{}]正常结果:", stringbuilder.toString());
        Map maps = Maps.newHashMap();
        List lists= Lists.newArrayList();
        lists.add(6065);lists.add(6066);lists.add(6067);
        maps.put("ids",lists);
        restTemplate.put(stringbuilder.toString(), maps);
    }

    /**
     * 查询某规划师的所有工单状态及数量;author:zhihao
     *
     * @param dateFlag 日期区间标识:today-当天,week-本周
     */
    @Test
    //@RequestMapping("/planner/listStatus/{plannerID}/{dateFlag}")
    public void listAmountTest()throws Exception {
        logger.info("******************listAmountTest********");
        StringBuilder  stringbuilder = new StringBuilder(urlConf.getFishcard_service()).
                append("/planner/listStatus/").
                append(978112).
                append("/thisWeek").
                append("?test=true");
        logger.info("测试接口正常结果:");
        JsonResultModel  jsonResultMode = restTemplate.getForObject(stringbuilder.toString(),JsonResultModel.class);
        Assert.isTrue(jsonResultMode.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        //Assert.isInstanceOf(List.class,jsonResultMode.getData());
        Assert.notNull(jsonResultMode.getData());

    }


    /**
     * 根据工单状态分页查询某规划师的数据;
     */
    @Test
    //@RequestMapping("/planner/page/{plannerID}/status/{status}")
    public void pageTest()throws Exception {
        logger.info("******************listAmountTest********");
        StringBuilder  stringbuilder = new StringBuilder(urlConf.getFishcard_service()).
                append("/planner/page/").
                append(978112).
                append("/status/").
                append(2).
                append("?dateFlag=thisWeek").
                append("&test=true");
        logger.info("测试接口正常结果:");
        JsonResultModel  jsonResultMode = restTemplate.getForObject(stringbuilder.toString(),JsonResultModel.class);
        Assert.isTrue(jsonResultMode.getReturnCode().toString().equals(ReturnCode.SUCCESS.getCode()));
        Assert.notNull(jsonResultMode.getData());
    }

}
