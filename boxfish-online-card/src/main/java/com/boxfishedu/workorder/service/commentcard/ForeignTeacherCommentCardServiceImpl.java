package com.boxfishedu.workorder.service.commentcard;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.common.bean.CommentCardStatus;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.exception.UnauthorizedException;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.common.util.JSONParser;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.dao.jpa.CommentCardJpaRepository;
import com.boxfishedu.workorder.dao.jpa.CommentCardUnanswerTeacherJpaRepository;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.entity.mysql.*;
import com.boxfishedu.workorder.service.commentcard.sdk.CommentCardSDK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * Created by ansel on 16/7/18.
 */
@Service
public class ForeignTeacherCommentCardServiceImpl implements ForeignTeacherCommentCardService{
    @Autowired
    CommentCardJpaRepository commentCardJpaRepository;

    @Autowired
    RabbitMqSender rabbitMqSender;

    @Autowired
    ServiceJpaRepository serviceJpaRepository;

    @Autowired
    CommentCardSDK commentCardSDK;

    @Autowired
    CommentCardUnanswerTeacherJpaRepository commentCardUnanswerTeacherJpaRepository;

    private Logger logger = LoggerFactory.getLogger(ForeignTeacherCommentCardServiceImpl.class);

    @Override
    public CommentCard foreignTeacherCommentCardAdd(CommentCard commentCard) {
        logger.info("调用外教点评接口新增学生问题,其中"+commentCard);
        Date dateNow = new Date();
        commentCard.setStudentAskTime(dateNow);
        commentCard.setCreateTime(dateNow);
        commentCard.setUpdateTime(dateNow);
        commentCard.setAssignTeacherCount(1);
        commentCard.setStatus(CommentCardStatus.REQUEST_ASSIGN_TEACHER.getCode());
        CommentCard temp = commentCardJpaRepository.save(commentCard);
        ToTeacherStudentForm toTeacherStudentForm = ToTeacherStudentForm.getToTeacherStudentForm(temp);
        logger.info("向师生运营发生消息,通知分配外教进行点评...");
        rabbitMqSender.send(toTeacherStudentForm, QueueTypeEnum.ASSIGN_FOREIGN_TEACHER_COMMENT);
        return temp;
    }

    @Override
    public void foreignTeacherCommentUpdateAnswer(FromTeacherStudentForm fromTeacherStudentForm) {
        CommentCard commentCard = commentCardJpaRepository.findOne(fromTeacherStudentForm.getFishCardId());
        if (commentCard == null){
            throw new UnauthorizedException("不存在的点评卡!");
        }else {
            Date dateNow = new Date();
            commentCard.setTeacherId(fromTeacherStudentForm.getTeacherId());
            commentCard.setTeacherName(fromTeacherStudentForm.getTeacherName());
            commentCard.setUpdateTime(dateNow);
            commentCard.setAssignTeacherTime(dateNow);
            commentCard.setStatus(CommentCardStatus.ASSIGNED_TEACHER.getCode());
            commentCard.setTeacherReadFlag(0);
            commentCardJpaRepository.save(commentCard);
            logger.info("调用外教点评接口更新点评卡中外教点评内容,其中" + commentCard);
        }
    }

    @Override
    public Page<CommentCard> foreignTeacherCommentQuery(Pageable pageable, Long studentId) {
        logger.info("调用学生查询外教点评列表接口,其中studentId="+studentId+"pageable="+pageable);
        return commentCardJpaRepository.queryCommentCardList(pageable,studentId);
    }

    @Override
    public CommentCard foreignTeacherCommentDetailQuery(Long id,Long userId) {
        logger.info("调用学生查询某条外教点评具体信息接口,并将此条设置为已读,其中id="+id);
        CommentCard commentCard = commentCardJpaRepository.findByIdAndStudentId(id,userId);
        if(commentCard == null){
            throw new UnauthorizedException();
        }
        if(commentCard.getStudentReadFlag() == 0){
            Date dateNow = new Date();
            commentCard.setUpdateTime(dateNow);
            commentCard.setStudentReadFlag(1);
            commentCardJpaRepository.save(commentCard);
        }
        return commentCard;
    }

    @Override
    @Transactional
    public void foreignTeacherCommentUnAnswer() {
        Date dateNow = new Date();
        logger.info("调用查询24小时未点评的外教接口");
        List<CommentCard> list = commentCardJpaRepository.queryCommentNoAnswerList();
        for (CommentCard commentCard: list) {
            commentCard.setStatus(CommentCardStatus.OVERTIME.getCode());
            commentCard.setUpdateTime(dateNow);
            commentCard.setAssignTeacherCount(2);
            commentCardJpaRepository.save(commentCard);
            ToTeacherStudentForm toTeacherStudentForm = ToTeacherStudentForm.getToTeacherStudentForm(commentCard);
            logger.info("再次向师生运营发生消息,通知重新分配外教进行点评,重新分配的commentCard:"+commentCard);
            rabbitMqSender.send(toTeacherStudentForm, QueueTypeEnum.ASSIGN_FOREIGN_TEACHER_COMMENT);
            if(!StringUtils.isEmpty(commentCard.getTeacherId())){
                CommentCardUnanswerTeacher commentCardUnanswerTeacher = new CommentCardUnanswerTeacher();
                commentCardUnanswerTeacher.setCommentCard(commentCard);
                commentCardUnanswerTeacher.setTeacherId(commentCard.getTeacherId());
                commentCardUnanswerTeacher.setCreateTime(dateNow);
                logger.info("记录超时未点评的外教,同时调用师生运营接口,设置该外教为旷课......",commentCard);
                commentCardUnanswerTeacherJpaRepository.save(commentCardUnanswerTeacher);
                JsonResultModel jsonResultModel = commentCardSDK.setTeacherAbsence(commentCard.getTeacherId(),commentCard.getStudentId(),commentCard.getId());
                logger.info("此外教标注旷课状态情况{}",jsonResultModel);
            }
        }
        logger.info("所有在24小时内为被点评的学生已重新请求分配外教完毕,一共重新分配外教点评的个数为:"+list.size());
    }

    @Override
    @Transactional
    public void foreignTeacherCommentUnAnswer2() {
        Date dateNow = new Date();
        logger.info("调用查询48小时未点评的外教接口");
        List<CommentCard> list = commentCardJpaRepository.queryCommentNoAnswerList2();
        for (CommentCard commentCard: list) {
            commentCard.setStatus(CommentCardStatus.OVERTIME.getCode());
            commentCard.setUpdateTime(dateNow);
            com.boxfishedu.workorder.entity.mysql.Service serviceTemp = serviceJpaRepository.findById(commentCard.getService().getId());
            commentCard.setService(serviceTemp);
            commentCardJpaRepository.save(commentCard);
            serviceTemp.setAmount(serviceTemp.getAmount() + 1);
            serviceJpaRepository.save(serviceTemp);
            CommentCardUnanswerTeacher commentCardUnanswerTeacher = new CommentCardUnanswerTeacher();
            commentCardUnanswerTeacher.setCommentCard(commentCard);
            commentCardUnanswerTeacher.setTeacherId(commentCard.getTeacherId());
            commentCardUnanswerTeacher.setCreateTime(dateNow);
            commentCardUnanswerTeacherJpaRepository.save(commentCardUnanswerTeacher);
        }
        logger.info("所有学生外教点评次数返还完毕,一共返回次数为:"+list.size());
    }

    @Override
    public JsonResultModel updateCommentAmount(com.boxfishedu.workorder.entity.mysql.Service service) {
        logger.info("调用修改学生点评次数接口,其中service="+service);
        serviceJpaRepository.save(service);
        return new JsonResultModel();
    }

    @Override
    public CommentCard testTeacherComment(CommentCardForm commentCardForm,Long userId) {
        logger.info("!!!!!!调用测试接口---->commentCardForm: "+commentCardForm.toString());
        Date dateNow = new Date();
        CommentCard commentCard = commentCardJpaRepository.findOne(commentCardForm.getId());
        commentCard.setTeacherId(userId);
        commentCard.setUpdateTime(dateNow);
        commentCard.setAnswerVideoPath(commentCardForm.getAnswerVideoPath());
        commentCard.setStatus(CommentCardStatus.ANSWERED.getCode());
        commentCard.setTeacherReadFlag(1);
        commentCard.setStudentReadFlag(0);
        return commentCardJpaRepository.save(commentCard);
    }

    @Override
    public Page<CommentCard> testQueryAll(Pageable pageable) {
        logger.info("!!!!!!调用测试接口---->教师端查询所有存在的点评卡");
        return commentCardJpaRepository.findAll(pageable);
    }

    @Override
    public String getUserPicture(String access_token) {
        UserInfo userInfo = JSONParser.fromJson(commentCardSDK.getUserPicture(access_token),UserInfo.class);
        return userInfo.getFigure_url();
    }

}
