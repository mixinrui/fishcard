package com.boxfishedu.workorder.servicex.commentcard;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.common.bean.CommentCardStatus;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.entity.mysql.CommentCardUnanswerTeacher;
import com.boxfishedu.workorder.requester.TeacherStudentCommentCardRequester;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.commentcard.CommentCardLogService;
import com.boxfishedu.workorder.service.commentcard.CommentCardTeacherAppService;
import com.boxfishedu.workorder.service.commentcard.ForeignTeacherCommentCardService;
import com.boxfishedu.workorder.service.commentcard.sdk.CommentCardSDK;
import com.boxfishedu.workorder.web.param.CommentCardSubmitParam;
import com.boxfishedu.workorder.web.param.Student2TeacherCommentParam;
import com.boxfishedu.workorder.web.param.commentcard.TeacherReadMsgParam;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;
import java.util.Map;

/**
 * Created by hucl on 16/7/20.
 */
@Service
public class CommentTeacherAppServiceX {

    @Autowired
    private CommentCardTeacherAppService commentCardTeacherAppService;

    @Autowired
    private CommentCardLogService commentCardLogService;

    @Autowired
    private ForeignTeacherCommentCardService foreignTeacherCommentCardService;

    @Autowired
    CommentCardSDK commentCardSDK;
    private ServiceJpaRepository serviceJpaRepository;

    @Autowired
    private RabbitMqSender rabbitMqSender;

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    public CommentCard findById(Long cardId){
        return commentCardTeacherAppService.findById(cardId);
    }

    public void markTeacherRead(TeacherReadMsgParam teacherReadMsgParam){
        CommentCard commentCard=this.findById(teacherReadMsgParam.getCommentCardId());
        if(null==commentCard){
            logger.error("不存在对应的点评卡,点评卡id[{}]",teacherReadMsgParam.getCommentCardId());
            throw new BusinessException("不存在对应的点评卡");
        }
        commentCard.setTeacherReadFlag(CommentCardStatus.TEACHER_READ.getCode());
        commentCardTeacherAppService.save(commentCard);
    }

    public void submitComment(@RequestBody CommentCardSubmitParam commentCardSubmitParam){
        CommentCard commentCard=commentCardTeacherAppService.findById(commentCardSubmitParam.getCommentCardId());
        if(null==commentCard){
            throw new BusinessException("不存在对应的点评卡");
        }
        commentCard.setStudentReadFlag(0);
        commentCard.setStatus(CommentCardStatus.ANSWERED.getCode());
        commentCard.setUpdateTime(new Date());
        //commentCard.setTeacherId(commentCardSubmitParam.getTeacherId());//测试时使用,正式去掉
        commentCard.setAnswerVideoPath(commentCardSubmitParam.getVideoPath());
        commentCard.setAnswerVideoTime(commentCardSubmitParam.getAnswerVideoTime());
        commentCard.setAnswerVideoSize(commentCardSubmitParam.getAnswerVideoSize());
        commentCard.setTeacherAnswerTime(new Date());
        commentCard.setTeacherPicturePath(commentCardSubmitParam.getTeacherPicturePath());
        commentCard.setTeacherReadFlag(CommentCardStatus.TEACHER_READ.getCode());
        commentCardTeacherAppService.save(commentCard);
        commentCardLogService.saveCommentCardLog(commentCard);
        JsonResultModel jsonResultModel = foreignTeacherCommentCardService.pushInfoToStudentAndTeacher(commentCard.getStudentId(),"收到一条外教点评，去查看。","FOREIGNCOMMENT");
        if (jsonResultModel.getReturnCode().equals(HttpStatus.OK.value())){
            logger.info("已经向学生端推送消息,推送的学生studentId=" + commentCard.getStudentId());
        }else {
            logger.info("向学生端推送消息失败,推送失败的学生studentId=" + commentCard.getStudentId());
        }
        //通知订单中心修改状态
        com.boxfishedu.workorder.entity.mysql.Service service = serviceJpaRepository.findOne(commentCard.getService().getId());
        if (service.getAmount() == 0){
            logger.info("@CommentTeacherAppServiceX 调用notifyOrderUpdateStatus,通知修改状态...");
            notifyOrderUpdateStatus(service.getOrderId(), ConstantUtil.WORKORDER_COMPLETED);
        }
    }

    public CommentCard checkTeacher(Long id, Long teacherId){
        return commentCardTeacherAppService.checkTeacher(id, teacherId);
    }

    private void commentHomePage(CommentCard commentCard) {
        Map typeAndDifficultyMap = commentCardSDK.commentTypeAndDifficulty(commentCard.getCourseId());
        String courseType = typeAndDifficultyMap.get("courseType").toString();
        String courseDifficulty = typeAndDifficultyMap.get("courseDifficulty").toString();
    }
    private void notifyOrderUpdateStatus(Long orderId, Integer status) {
        logger.info("@notifyOrderUpdateStatus 通知订单中心修改状体...");
        Map param = Maps.newHashMap();
        param.put("id",orderId.toString());
        param.put("status",status.toString());
        rabbitMqSender.send(param, QueueTypeEnum.NOTIFY_ORDER);
    }
}
