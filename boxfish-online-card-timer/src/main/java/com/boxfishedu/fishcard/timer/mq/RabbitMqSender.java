package com.boxfishedu.fishcard.timer.mq;

import com.boxfishedu.fishcard.timer.common.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public class RabbitMqSender {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private
    @Qualifier(RabbitMqConstant.UNASSIGNED_TEACHER_TIMER_TEMPLATE_NAME)
    RabbitTemplate rabbitTemplate;

    // 抢单
    @Autowired
    private
    @Qualifier(RabbitMqConstant.GRAB_WORKER_ORDER_TIME_QUEUE)
    RabbitTemplate rabbitGrabOrderTemplate;



    public void send(Object object) {
        logger.info("开始发送通知请求:" + DateUtil.Date2String(new Date()));
        rabbitTemplate.convertAndSend(object);
    }

    // 发送抢单队列
    public void sendGrabOrder(Object object){
        logger.info("开始发送抢单通知请求:" + DateUtil.Date2String(new Date()));
        rabbitGrabOrderTemplate.convertAndSend(object);
    }
}
