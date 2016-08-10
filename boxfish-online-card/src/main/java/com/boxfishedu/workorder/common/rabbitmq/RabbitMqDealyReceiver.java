package com.boxfishedu.workorder.common.rabbitmq;

import com.boxfishedu.workorder.common.bean.FishCardDelayMessage;
import com.boxfishedu.workorder.common.bean.FishCardDelayMsgType;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.servicex.timer.FishCardUpdatorServiceX;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by hucl on 16/6/9.
 */
@Component
public class RabbitMqDealyReceiver {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private FishCardUpdatorServiceX fishCardUpdatorServiceX;

    @RabbitListener(queues = RabbitMqConstant.DELAY_TEACHER_DEALER_QUEUE)
    public void teacherAbsentDealer(FishCardDelayMessage fishCardDelayMessage) throws Exception {
        logger.info("@============>[teacherAbsentDealer]开始接收delay message{}", fishCardDelayMessage);
        if (fishCardDelayMessage.getType() == FishCardDelayMsgType.TEACHER_ABSENT.value()) {
            try {
                fishCardUpdatorServiceX.teacherAbsentUpdator(fishCardDelayMessage);
            } catch (Exception ex) {
                logger.error("@teacherAbsentDealer更新旷课情况失败",ex);
            }

        }
    }

    @RabbitListener(queues = RabbitMqConstant.DELAY_STUDENT_DEALER_QUEUE)
    public void studentAbsentDealer(FishCardDelayMessage fishCardDelayMessage) throws Exception {
        logger.info("@============>[studentAbsentDealer]开始接收delay message{}", fishCardDelayMessage);
        if (fishCardDelayMessage.getType() == FishCardDelayMsgType.STUDENT_ABSENT.value()) {
            try {
//                将学生旷课逻辑加到课程结束以后再做判断
                fishCardUpdatorServiceX.studentAbsentUpdator(fishCardDelayMessage);
            } catch (Exception ex) {
                logger.error("@studentAbsentDealer更新旷课情况失败");
            }
        }
    }

    @RabbitListener(queues = RabbitMqConstant.DELAY_COMPLETE_FORCE_DEALER_QUEUE)
    public void forceCompleteDealer(FishCardDelayMessage fishCardDelayMessage) throws Exception {
        logger.info("@============>[forceCompleteDealer]开始接收delay message{}", fishCardDelayMessage);
        if (fishCardDelayMessage.getType() == FishCardDelayMsgType.FORCE_COMPLETE_SERVER.value()) {
            try {
                fishCardUpdatorServiceX.forceCompleteUpdator(fishCardDelayMessage);
            } catch (Exception ex) {
                logger.error("@forceCompleteDealer#exception强制完成处理失败",ex);
            }
        }
    }

    @RabbitListener(queues = RabbitMqConstant.DELAY_NOTIFY_TEACHER_PREPARE_DEALER_QUEUE)
    public void teacherPrepareClassDelaer(FishCardDelayMessage fishCardDelayMessage) throws Exception {
        logger.info("@===============>[teacherPrepareClassDelaer],当前时间[{}]开始接受delay message{}", DateUtil.Date2String(new Date()), fishCardDelayMessage);
        if (fishCardDelayMessage.getType() == FishCardDelayMsgType.NOTIFY_TEACHER_PREPARE_CLASS.value()) {
            try {
                fishCardUpdatorServiceX.teacherPrepareClassUpdator(fishCardDelayMessage);
            } catch (Exception ex) {
                logger.error("@teacherPrepareClassDelaer通知教师准备上课失败");
            }
        }
    }
}
