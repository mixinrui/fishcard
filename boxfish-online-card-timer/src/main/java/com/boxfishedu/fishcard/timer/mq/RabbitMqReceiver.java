package com.boxfishedu.fishcard.timer.mq;

import com.boxfishedu.card.bean.ServiceTimerMessage;
import com.boxfishedu.card.bean.TeachingType;
import com.boxfishedu.card.bean.TimerMessageType;
import com.boxfishedu.fishcard.timer.common.mail.SpringMailSender;
import com.boxfishedu.fishcard.timer.common.util.DateUtil;
import com.boxfishedu.fishcard.timer.common.util.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * Created by hucl on 16/4/15.
 */
@Component
public class RabbitMqReceiver {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SpringMailSender springMailSender;

    @RabbitListener(queues = RabbitMqConstant.UNASSIGNED_TEACHER_REPLY_TIMER_QUEUE)
    public void getReplyFromService(ServiceTimerMessage message) throws Exception{
        logger.info(">>>>>>>>>>>>>>@getReplyFromService接收到来自服务中心的定时器内容回复:{}", JacksonUtil.toJSon(message));
        //分配教师定时器
        if (message.getType() == TimerMessageType.TEACHER_ASSIGN_NOTIFY_REPLY.value()) {
            logger.info("@@@收到教师分配定时器回复");
        }
        //教师预警定时器
        else {
            logger.info("$$$@getReplyFromService收到教师预警定时器回复");
            if (message.getStatus() == 0) {
                logger.info("教师充足,不需要发送预警邮件");
                return;
            }
            logger.warn("################固定时间内的教师资源不足###############");
            Map<Integer, Long> bodyMap = message.getBody();
            StringBuilder builder=new StringBuilder();
            builder.append("开始时间:").append(message.getStartTime()).append("\n");
            builder.append("结束时间:").append(message.getEndTime()).append("\n");
            for (Map.Entry<Integer, Long> entry : bodyMap.entrySet()) {
                builder.append(TeachingType.getDesc(entry.getKey())).append(":").append(entry.getValue()).append("\n");
            }
            builder.append("以上内容为boxfish资源分配预警;请登录[http://101.201.66.109:94]根据条件为学生进行教师匹配\n");
            springMailSender.send("教师资源分配不足预警-"+ DateUtil.Date2String(new Date()),builder.toString());
        }
    }
}
