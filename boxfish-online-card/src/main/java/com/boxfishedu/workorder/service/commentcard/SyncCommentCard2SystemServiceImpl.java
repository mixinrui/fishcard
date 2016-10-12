package com.boxfishedu.workorder.service.commentcard;

import com.boxfishedu.workorder.common.bean.CommentCardStatus;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.dao.jpa.CommentCardJpaRepository;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by ansel on 16/10/11.
 */
@Service
public class SyncCommentCard2SystemServiceImpl implements SyncCommentCard2SystemService{

    @Autowired
    ServiceJpaRepository serviceJpaRepository;

    @Autowired
    CommentCardJpaRepository commentCardJpaRepository;

    @Autowired
    RabbitMqSender rabbitMqSender;

    private final Logger logger = LoggerFactory.getLogger(SyncCommentCard2SystemServiceImpl.class);

    @Override
    public void syncCommentCard2System(Long serviceId, int status, Date teacherAnswerTime) {
        com.boxfishedu.workorder.entity.mysql.Service service = serviceJpaRepository.findOne(serviceId);
        Map<Object,String> paramMap = getParamMap(service,status,teacherAnswerTime);
        logger.info("@syncCommentCard2System 外教点评次数修改,通知客服系统...");
        rabbitMqSender.send(paramMap,QueueTypeEnum.ASYNC_COMMENT_CARD_CUSTOMER_SERVICE);
    }

    @Override
    public long initializeCommentCard2System() {
        List<com.boxfishedu.workorder.entity.mysql.Service> serviceList = serviceJpaRepository.findByProductType();
        long count = 0;
        for (com.boxfishedu.workorder.entity.mysql.Service service: serviceList) {
            List<CommentCard> commentCardList = commentCardJpaRepository.getSystemCommentCard(service);
            Map<Object,String> paramMap = null;
            if (commentCardList.size() != 0){
                System.out.println("status============:" + commentCardList.get(0).getStatus() + " time==================:" + commentCardList.get(0).getTeacherAnswerTime());
                if (Objects.equals(commentCardList.get(0).getStatus(),CommentCardStatus.ANSWERED.getCode())
                        || Objects.equals(commentCardList.get(0).getStatus(),CommentCardStatus.STUDENT_COMMENT_TO_TEACHER.getCode())) {
                    paramMap = getParamMap(service,commentCardList.get(0).getStatus(), commentCardList.get(0).getTeacherAnswerTime());
                }else if (Objects.equals(commentCardList.get(0).getStatus(),null)){
                    paramMap = getParamMap(service,CommentCardStatus.OVERTIME.getCode(),null);
                }else {
                    paramMap = getParamMap(service,commentCardList.get(0).getStatus(),null);
                }
            }else {
                paramMap = getParamMap(service, CommentCardStatus.UNASKED.getCode(),null);
            }
            logger.info("@initializeCommentCard2System1 通知客服系统,维护外教点评历史数据...");
            rabbitMqSender.send(paramMap,QueueTypeEnum.ASYNC_COMMENT_CARD_CUSTOMER_SERVICE);
            count += 1;
        }
        logger.info("@initializeCommentCard2System2 一共初始化个数: " + count);
        return count;
    }

    private Map<Object,String> getParamMap(com.boxfishedu.workorder.entity.mysql.Service service,int status, Date teacherAnswerTime){
        Map<Object,String> paramMap = new HashMap<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        paramMap.put("id",service.getId().toString());
        paramMap.put("studentId",service.getStudentId().toString());
        paramMap.put("studentName",service.getStudentName());
        paramMap.put("orderId",service.getOrderId().toString());
        paramMap.put("orderCode",service.getOrderCode());
        paramMap.put("originalAmount",service.getOriginalAmount() == null? null: String.valueOf(service.getOriginalAmount()));
        paramMap.put("amount",service.getAmount().toString());
        paramMap.put("tutorType",service.getTutorType());
        paramMap.put("orderChannel",service.getOrderChannel());
        paramMap.put("comboType",service.getComboType());
        paramMap.put("productType",service.getProductType() == null? null: String.valueOf(service.getProductType()));
        paramMap.put("teachingType",service.getTeachingType() == null? null: String.valueOf(service.getTeachingType()));
        paramMap.put("createTime",service.getCreateTime() == null? null: format.format(service.getCreateTime()));
        paramMap.put("updateTime",service.getUpdateTime() == null? null: format.format(service.getUpdateTime()));
        paramMap.put("status",String.valueOf(status));
        paramMap.put("teacherAnswerTime",teacherAnswerTime == null? null: format.format(teacherAnswerTime));
        return paramMap;
    }
}
