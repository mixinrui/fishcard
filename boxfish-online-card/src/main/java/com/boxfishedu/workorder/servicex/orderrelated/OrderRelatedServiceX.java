package com.boxfishedu.workorder.servicex.orderrelated;

import com.boxfishedu.workorder.common.exception.BoxfishException;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.online.order.entity.OrderForm;
import com.boxfishedu.workorder.common.config.PoolConf;
import com.boxfishedu.workorder.common.threadpool.OrderConvertPoolManager;
import com.boxfishedu.workorder.service.ServeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by hucl on 16/3/31.
 * 与订单相关的服务
 */
@org.springframework.stereotype.Service
public class OrderRelatedServiceX {
    @Autowired
    private OrderConvertPoolManager orderConvertPoolManager;

    @Autowired
    private ServeService serveService;
    @Autowired
    private PoolConf poolConf;


    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void preHandleOrder(OrderForm orderView) {
        //如果线程池中等待转换的数目不多,修改redis中的对象状态,同时开始转换
        if (!(orderConvertPoolManager.getActiveNum() > poolConf.getSize_core_pool()*2)) {
//            RedisOrder redisOrder = new RedisOrder(orderView,OrderConvertEnum.PROCESSING);
//            redisService.set(redisOrder, RedisTypeEnum.ORDER2SERVICE, orderView.getId());
            orderConvertPoolManager.execute(new Thread(() -> {
                serveService.order2ServiceAndWorkOrder(orderView);
            }));
        }
        //直接放redis,等待手动触发的转换操作
        else{
            String tips="当前转换线程较多,排队等待中;活跃转换线程:"+orderConvertPoolManager.getActiveNum();
            throw new BusinessException(tips);
        }
    }

    public void order2ServiceAndWorkOrder(OrderForm orderView) throws BoxfishException {
        serveService.order2ServiceAndWorkOrder(orderView);
    }
}
