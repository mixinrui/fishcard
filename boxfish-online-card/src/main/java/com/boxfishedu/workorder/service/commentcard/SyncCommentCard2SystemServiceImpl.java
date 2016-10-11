package com.boxfishedu.workorder.service.commentcard;

import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ansel on 16/10/11.
 */
@Service
public class SyncCommentCard2SystemServiceImpl implements SyncCommentCard2SystemService{

    @Autowired
    ServiceJpaRepository serviceJpaRepository;

    @Autowired
    RabbitMqSender rabbitMqSender;

    private final Logger logger = LoggerFactory.getLogger(SyncCommentCard2SystemServiceImpl.class);

    @Override
    public void syncCommentCard2System(Long serviceId) {
        com.boxfishedu.workorder.entity.mysql.Service service = serviceJpaRepository.findOne(serviceId);
        Map<Object,Object> paramMap = new HashMap<>();
        paramMap.put("id",service.getId());
        paramMap.put("studentId",service.getStudentId());
        paramMap.put("studentName",service.getStudentName());
        paramMap.put("orderId",service.getOrderId());
        paramMap.put("orderCode",service.getOrderCode());
        paramMap.put("originalAmount",service.getOriginalAmount());
        paramMap.put("amount",service.getAmount());
        paramMap.put("tutorType",service.getTutorType());
        paramMap.put("orderChannel",service.getOrderChannel());
        paramMap.put("comboType",service.getComboType());
        paramMap.put("productType",service.getProductType());
        paramMap.put("teachingType",service.getTeachingType());
        paramMap.put("createTime",service.getCreateTime());
        paramMap.put("updateTime",service.getUpdateTime());
        logger.info("@syncCommentCard2System 外教点评次数修改,通知客服系统...");
        rabbitMqSender.send(paramMap,QueueTypeEnum.ASYNC_COMMENT_CARD_CUSTOMER_SERVICE);
    }
}
