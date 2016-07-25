package com.boxfishedu.workorder.service.commentcard;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.common.bean.CommentCardStatus;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.exception.UnauthorizedException;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.dao.jpa.CommentCardJpaRepository;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.entity.mysql.FromTeacherStudentForm;
import com.boxfishedu.workorder.entity.mysql.ToTeacherStudentForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private Logger logger = LoggerFactory.getLogger(ForeignTeacherCommentCardServiceImpl.class);

    @Override
    public CommentCard foreignTeacherCommentCardAdd(CommentCard commentCard) {
        logger.info("调用外教点评接口新增学生问题,其中commentCard="+commentCard);
        Date dateNow = new Date();
        commentCard.setStudentAskTime(dateNow);
        commentCard.setCreateTime(dateNow);
        commentCard.setUpdateTime(dateNow);
        commentCard.setStatus(CommentCardStatus.ASKED.getCode());
        return commentCardJpaRepository.save(commentCard);
    }

    @Override
    public void foreignTeacherCommentUpdateQuestion(CommentCard commentCard) {
        logger.info("调用外教点评接口更新学生问题,其中commentCard="+commentCard);
        Date dateNow = new Date();
        commentCard.setUpdateTime(dateNow);
        commentCard.setStatus(CommentCardStatus.REQUEST_ASSIGN_TEACHER.getCode());
        commentCardJpaRepository.save(commentCard);
        ToTeacherStudentForm toTeacherStudentForm = ToTeacherStudentForm.getToTeacherStudentForm(commentCard);
        logger.info("向师生运营发生消息,通知分配外教进行点评...");
        rabbitMqSender.send(toTeacherStudentForm, QueueTypeEnum.ASSIGN_FOREIGN_TEACHER_COMMENT);
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
            commentCard.setStatus(CommentCardStatus.ASSIGNED_TEACHER.getCode());
            commentCardJpaRepository.save(commentCard);
            logger.info("调用外教点评接口更新点评卡中外教点评内容,其中commentCard=" + commentCard);
        }
    }

    @Override
    public void foreignTeacherCommentUpdateStatusRead(CommentCard commentCard) {
        Date dateNow = new Date();
        commentCard.setUpdateTime(dateNow);
        commentCard.setStatus(CommentCardStatus.READ.getCode());
        commentCardJpaRepository.save(commentCard);
        logger.info("调用外教点评接口更新点评卡状态为已读,其中commentCard="+commentCard);
    }

    @Override
    public Page<CommentCard> foreignTeacherCommentQuery(Pageable pageable, Long studentId) {
        logger.info("调用学生查询外教点评列表接口,其中studentId="+studentId+"pageable="+pageable);
        return commentCardJpaRepository.queryCommentCardList(pageable,studentId);
    }

    @Override
    public CommentCard foreignTeacherCommentDetailQuery(Long id, Long studentId) {
        logger.info("调用学生查询某条外教点评具体信息接口,其中id="+id);
        return commentCardJpaRepository.findByIdAndStudentId(id,studentId);
    }

    @Override
    @Transactional
    public void foreignTeacherCommentUnAnswer() {
        Date dateNow = new Date();
        logger.info("调用查询外教24小时未点评问题数据库接口");
        List<CommentCard> list = commentCardJpaRepository.queryCommentNoAnswerList();
        for (CommentCard commentCard: list) {
            commentCard.setStatus(CommentCardStatus.OVERTIME_ONE.getCode());
            commentCard.setAssignTeacherCount(1);
            commentCard.setAssignTeacherTime(dateNow);
            commentCard.setUpdateTime(dateNow);
            commentCardJpaRepository.save(commentCard);

        }
        logger.info("所有在24小时内为被点评的学生已重新求情分配外教完毕,一共重新分配的学生个数为:"+list.size());
    }

    @Override
    @Transactional
    public void foreignTeacherCommentUnAnswer2() {
        Date dateNow = new Date();
        logger.info("调用查询外教48小时未点评问题数据库接口");
        List<CommentCard> list = commentCardJpaRepository.queryCommentNoAnswerList2();
        for (CommentCard commentCard: list) {
            commentCard.setStatus(CommentCardStatus.OVERTIME_TWO.getCode());
            commentCard.setUpdateTime(dateNow);
            com.boxfishedu.workorder.entity.mysql.Service serviceTemp = serviceJpaRepository.findById(commentCard.getService().getId());
            commentCard.setService(serviceTemp);
            commentCardJpaRepository.save(commentCard);
            serviceTemp.setAmount(serviceTemp.getAmount() + 1);
            serviceJpaRepository.save(serviceTemp);
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
    public CommentCard getAssignTeacherCount(Long id) {
        CommentCard commentCard = commentCardJpaRepository.findOne(id);

        return null;
    }

}
