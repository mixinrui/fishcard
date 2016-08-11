package com.boxfishedu.workorder.service.commentcard;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.common.bean.CommentCardStatus;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.exception.UnauthorizedException;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.common.util.JSONParser;
import com.boxfishedu.workorder.dao.jpa.CommentCardJpaRepository;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.entity.mysql.*;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.commentcard.sdk.CommentCardSDK;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

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
    ServeService serveService;

    private Logger logger = LoggerFactory.getLogger(ForeignTeacherCommentCardServiceImpl.class);

    @Override
    @Transactional
    public CommentCard foreignTeacherCommentCardAdd(CommentCardForm commentCardForm, Long userId, String access_token) {
        CommentCard commentCard=CommentCard.getCommentCard(commentCardForm);
        if(! serveService.findFirstAvailableForeignCommentService(userId).isPresent()){
            throw new BusinessException("学生的外教点评次数已经用尽,请先购买!");
        }
        com.boxfishedu.workorder.entity.mysql.Service service= serveService.findFirstAvailableForeignCommentService(userId).get();
        if(service.getAmount() <= 0){
            throw new BusinessException("学生的外教点评次数已经用尽,请先购买!");
        }
        else {
            service.setAmount(service.getAmount() - 1);
            updateCommentAmount(service);
            commentCard.setStudentId(userId);
            commentCard.setService(service);
            commentCard.setOrderId(service.getOrderId());
            commentCard.setOrderCode(service.getOrderCode());
            commentCard.setAskVoicePath(commentCardForm.getAskVoicePath());
            commentCard.setVoiceTime(commentCardForm.getVoiceTime());
            commentCard.setStudentPicturePath(getUserPicture(access_token));
        }
        logger.info("调用外教点评接口新增学生问题,其中"+commentCard);
        Date dateNow = new Date();
        commentCard.setStudentAskTime(dateNow);
        commentCard.setCreateTime(dateNow);
        commentCard.setUpdateTime(dateNow);
        commentCard.setAssignTeacherCount(CommentCardStatus.ASSIGN_TEACHER_ONCE.getCode());
        commentCard.setStudentReadFlag(CommentCardStatus.STUDENT_READ.getCode());
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
            commentCard.setUpdateTime(dateNow);
            commentCard.setAssignTeacherTime(dateNow);
            commentCard.setStatus(CommentCardStatus.ASSIGNED_TEACHER.getCode());
            commentCard.setTeacherReadFlag(CommentCardStatus.TEACHER_UNREAD.getCode());
            commentCardJpaRepository.save(commentCard);
            logger.info("调用外教点评接口更新点评卡中外教点评内容,其中" + commentCard);
            JsonResultModel jsonResultModel = pushInfoToStudentAndTeacher(fromTeacherStudentForm.getTeacherId(),"学生发来一次求点评，点击查看。\n" +
                    "You’ve got a new answer to access; Do it now~","FOREIGNCOMMENT");
            if (jsonResultModel.getReturnCode().equals(HttpStatus.SC_OK)){
                logger.info("已经向教师端推送消息,推送的教师teacherId=" + fromTeacherStudentForm.getTeacherId());
            }else {
                logger.info("向教师端推送消息失败,推送失败的教师teacherId=" + fromTeacherStudentForm.getTeacherId());
            }
        }
    }

    @Override
    public Map foreignTeacherCommentQuery(Pageable pageable, Long studentId) {
        logger.info("调用学生查询外教点评列表接口,其中studentId="+studentId+"pageable="+pageable);
        Page<CommentCard> commentCardPage = commentCardJpaRepository.queryCommentCardList(pageable,studentId);
        Map commentCardsMap = new LinkedHashMap<>();
        commentCardsMap.put("content",commentCardPage.getContent());
        commentCardsMap.put("totalPages",commentCardPage.getTotalPages());
        commentCardsMap.put("number",commentCardPage.getNumber());
        commentCardsMap.put("totalElements",commentCardPage.getTotalElements());
        commentCardsMap.put("unreadTotalElements",countStudentUnreadCommentCards(studentId).getData().toString());
        return commentCardsMap;
    }

    @Override
    public CommentCard foreignTeacherCommentDetailQuery(Long id,Long userId) {
        logger.info("调用学生查询某条外教点评具体信息接口,并将此条设置为已读,其中id="+id);
        CommentCard commentCard = commentCardJpaRepository.findByIdAndStudentId(id,userId);
        if(commentCard == null){
            throw new UnauthorizedException();
        }
        if(commentCard.getStudentReadFlag() == CommentCardStatus.STUDENT_UNREAD.getCode()){
            Date dateNow = new Date();
            commentCard.setUpdateTime(dateNow);
            commentCard.setStudentReadFlag(CommentCardStatus.STUDENT_READ.getCode());
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
            if(!StringUtils.isEmpty(commentCard.getTeacherId())){
                logger.info("调用师生运营接口,设置该外教为旷课......",commentCard);
                JsonResultModel jsonResultModel = commentCardSDK.setTeacherAbsence(commentCard.getTeacherId(),commentCard.getStudentId(),commentCard.getId());
                logger.info("此外教标注旷课状态情况{}",jsonResultModel);
            }
            CommentCard oldCommentCard = commentCardJpaRepository.save(commentCard);
            CommentCard temp = new CommentCard();
            temp.setStudentId(oldCommentCard.getStudentId());
            temp.setStudentPicturePath(oldCommentCard.getStudentPicturePath());
            temp.setAskVoicePath(oldCommentCard.getAskVoicePath());
            temp.setVoiceTime(oldCommentCard.getVoiceTime());
            temp.setStudentAskTime(oldCommentCard.getStudentAskTime());
            temp.setAssignTeacherCount(CommentCardStatus.ASSIGN_TEACHER_TWICE.getCode());
            temp.setCourseId(oldCommentCard.getCourseId());
            temp.setCourseName(oldCommentCard.getCourseName());
            temp.setQuestionName(oldCommentCard.getQuestionName());
            temp.setCover(oldCommentCard.getCover());
            temp.setService(oldCommentCard.getService());
            temp.setOrderId(oldCommentCard.getOrderId());
            temp.setOrderCode(oldCommentCard.getOrderCode());
            temp.setStudentReadFlag(CommentCardStatus.STUDENT_UNREAD.getCode());
            temp.setTeacherReadFlag(CommentCardStatus.TEACHER_READ.getCode());
            temp.setStatus(CommentCardStatus.REQUEST_ASSIGN_TEACHER.getCode());
            temp.setCreateTime(oldCommentCard.getCreateTime());
            temp.setUpdateTime(dateNow);
            CommentCard newCommentCard = commentCardJpaRepository.save(temp);
            ToTeacherStudentForm toTeacherStudentForm = ToTeacherStudentForm.getToTeacherStudentForm(newCommentCard);
            logger.info("再次向师生运营发生消息,通知重新分配外教进行点评,重新分配的commentCard:"+newCommentCard);
            rabbitMqSender.send(toTeacherStudentForm, QueueTypeEnum.ASSIGN_FOREIGN_TEACHER_COMMENT);

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
        }
        logger.info("所有学生外教点评次数返还完毕,一共返回次数为:"+list.size());
    }


    private JsonResultModel updateCommentAmount(com.boxfishedu.workorder.entity.mysql.Service service) {
        logger.info("调用修改学生点评次数接口,其中service="+service);
        serviceJpaRepository.save(service);
        return new JsonResultModel();
    }

    @Override
    public CommentCard testTeacherComment(CommentCardForm commentCardForm,Long userId,String access_token) {
        logger.info("!!!!!!调用测试接口---->commentCardForm: "+commentCardForm.toString());
        Date dateNow = new Date();
        CommentCard commentCard = commentCardJpaRepository.findOne(commentCardForm.getId());
        commentCard.setTeacherId(userId);
        commentCard.setTeacherPicturePath(getUserPicture(access_token));
        commentCard.setUpdateTime(dateNow);
        commentCard.setAnswerVideoPath(commentCardForm.getAnswerVideoPath());
        commentCard.setAnswerVideoTime(commentCardForm.getAnswerVideoTime());
        commentCard.setAnswerVideoSize(commentCardForm.getAnswerVideoSize());
        commentCard.setStatus(commentCardForm.getStatus());
        if (commentCardForm.getStatus() == CommentCardStatus.ASSIGNED_TEACHER.getCode() || commentCardForm.getStatus() == CommentCardStatus.OVERTIME.getCode()){
            commentCard.setTeacherReadFlag(CommentCardStatus.TEACHER_UNREAD.getCode());
            commentCard.setStudentReadFlag(CommentCardStatus.STUDENT_READ.getCode());
        }else {
            commentCard.setTeacherReadFlag(CommentCardStatus.TEACHER_READ.getCode());
            commentCard.setStudentReadFlag(CommentCardStatus.STUDENT_UNREAD.getCode());
        }

        return commentCardJpaRepository.save(commentCard);
    }

    @Override
    public Page<CommentCard> testQueryAll(Pageable pageable) {
        logger.info("!!!!!!调用测试接口---->教师端查询所有存在的点评卡");
        return commentCardJpaRepository.findAll(pageable);
    }

    public String getUserPicture(String access_token) {
        UserInfo userInfo = JSONParser.fromJson(commentCardSDK.getUserPicture(access_token),UserInfo.class);
        return userInfo.getFigure_url() == null?"":userInfo.getFigure_url();
    }

    @Override
    public JsonResultModel pushInfoToStudentAndTeacher(Long userId, String title, String type) {
        return commentCardSDK.pushToStudentAndTeacher(userId,title,type);
    }

    @Override
    public JsonResultModel countStudentUnreadCommentCards(Long userId) {
        JsonResultModel jsonResultModel = new JsonResultModel();
        jsonResultModel.setData(String.valueOf(commentCardJpaRepository.countStudentUnreadCommentCards(userId)));
        jsonResultModel.setReturnCode(HttpStatus.SC_OK);
        jsonResultModel.setReturnMsg("success");
        return jsonResultModel;
    }

    @Override
    public void updateCommentCardsPictures(UpdatePicturesForm updatePicturesForm) {

    }
}
