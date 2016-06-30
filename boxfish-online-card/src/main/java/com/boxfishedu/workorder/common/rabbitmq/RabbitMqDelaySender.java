package com.boxfishedu.workorder.common.rabbitmq;

import com.boxfishedu.workorder.common.bean.FishCardDelayMessage;
import com.boxfishedu.workorder.common.bean.FishCardDelayMsgType;
import com.boxfishedu.workorder.service.FishCardStatusService;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Created by hucl on 16/6/8.
 */
@Component
public class RabbitMqDelaySender {
    @Autowired
    private @Qualifier(RabbitMqConstant.DELAY_QUEUE_TEACHER_ABSENT_TEMPLATE_NAME) RabbitTemplate rabbitDelayTeacherTemplate;
    @Autowired
    private @Qualifier(RabbitMqConstant.DELAY_QUEUE_STUDENT_ABSENT_TEMPLATE_NAME) RabbitTemplate rabbitDelayStudentTemplate;
    @Autowired
    private @Qualifier(RabbitMqConstant.DELAY_QUEUE_FORCE_COMPLETE_TEMPLATE_NAME) RabbitTemplate rabbitDelayForceCompleteTemplate;

    @Autowired
    private @Qualifier(RabbitMqConstant.DELAY_QUEUE_NOTIFY_TEACHER_PREPARE_TEMPLATE_NAME) RabbitTemplate rabbitDelayPrepareClassTemplate;

    @Autowired
    private Jackson2JsonMessageConverter jackson2JsonMessageConverter;
    @Autowired
    private FishCardStatusService fishCardStatusService;

    public void send(FishCardDelayMessage fishCardDelayMessage, MessageProperties messageProperties) {
        /**
         * 检测教师旷课
         */
        if(fishCardDelayMessage.getType()==FishCardDelayMsgType.TEACHER_ABSENT.value()) {
            rabbitDelayTeacherTemplate.send(RabbitMqConstant.DELAY_TEACHER_ABSENT_QUEUE,
                    jackson2JsonMessageConverter.toMessage(fishCardDelayMessage, messageProperties));
        }
        /**
         * 检测学生旷课
         */
        else if(fishCardDelayMessage.getType()==FishCardDelayMsgType.STUDENT_ABSENT.value()){
            rabbitDelayStudentTemplate.send(RabbitMqConstant.DELAY_STUDENT_ABSENT_QUEUE,
                    jackson2JsonMessageConverter.toMessage(fishCardDelayMessage, messageProperties));
        }
        /**
         * 检测强制完成
         */
        else if(fishCardDelayMessage.getType()==FishCardDelayMsgType.FORCE_COMPLETE_SERVER.value()){
            rabbitDelayForceCompleteTemplate.send(RabbitMqConstant.DELAY_COMPLETE_FORCE_QUEUE,
                    jackson2JsonMessageConverter.toMessage(fishCardDelayMessage, messageProperties));
        }
        /**
         * 通知教师准备上课
         */
        else if(fishCardDelayMessage.getType()==FishCardDelayMsgType.NOTIFY_TEACHER_PREPARE_CLASS.value()){
            rabbitDelayPrepareClassTemplate.send(RabbitMqConstant.DELAY__NOTIFY_TEACHER_PREPARE_QUEUE,
                    jackson2JsonMessageConverter.toMessage(fishCardDelayMessage, messageProperties));
        }
    }
}
