package com.boxfishedu.workorder.service.commentcard;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.common.bean.CommentCardStatus;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.exception.UnauthorizedException;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JSONParser;
import com.boxfishedu.workorder.common.util.SimpleDateUtil;
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

import java.time.LocalDateTime;
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
    CommentCardTeacherAppService commentCardTeacherAppService;

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
        }else {
            service.setAmount(service.getAmount() - 1);
            updateCommentAmount(service);
            commentCard.setStudentId(userId);
            commentCard.setService(service);
            commentCard.setOrderId(service.getOrderId());
            commentCard.setOrderCode(service.getOrderCode());
            commentCard.setStudentPicturePath(getUserPicture(access_token));
        }
        logger.info("调用外教点评接口新增学生点评卡,其中"+commentCard);
        Date dateNow = new Date();
        commentCard.setStudentAskTime(dateNow);
        commentCard.setCreateTime(dateNow);
        commentCard.setUpdateTime(dateNow);
        commentCard.setAssignTeacherCount(CommentCardStatus.ASSIGN_TEACHER_ONCE.getCode());
        commentCard.setStudentReadFlag(CommentCardStatus.STUDENT_READ.getCode());
        commentCard.setStatus(CommentCardStatus.REQUEST_ASSIGN_TEACHER.getCode());
        CommentCard newCommentCard = commentCardJpaRepository.save(commentCard);
        ToTeacherStudentForm toTeacherStudentForm = ToTeacherStudentForm.getToTeacherStudentForm(newCommentCard);
        logger.debug("@foreignTeacherCommentCardAdd向师生运营发生消息,通知分配外教进行点评...");
        rabbitMqSender.send(toTeacherStudentForm, QueueTypeEnum.ASSIGN_FOREIGN_TEACHER_COMMENT);
        return newCommentCard;
    }

    @Override
    public void foreignTeacherCommentUpdateAnswer(FromTeacherStudentForm fromTeacherStudentForm) {
        CommentCard commentCard = commentCardJpaRepository.findById(fromTeacherStudentForm.getFishCardId());
        if (commentCard == null){
            logger.info("根据点评卡id查到的点评卡为空,点评卡id来自师生运营回传参数:"+fromTeacherStudentForm);
            throw new UnauthorizedException("不存在的点评卡!");
        }else {
            if(fromTeacherStudentForm.getTeacherId() == null){
                if (commentCard.getAssignTeacherCount() == CommentCardStatus.ASSIGN_TEACHER_TWICE.getCode()){
                    Date updateTime = new Date();
                    Map paramMap = new HashMap<>();
                    paramMap.put("fishCardId",commentCard.getId());
                    paramMap.put("studentId",commentCard.getStudentId());
                    paramMap.put("courseId",commentCard.getCourseId());
                    Map innerTeacherMap = (Map)commentCardSDK.getInnerTeacherId(paramMap).getData();
                    if(innerTeacherMap.get("teacherId") != null) {
                        commentCard.setTeacherId(Long.parseLong(innerTeacherMap.get("teacherId").toString()));
                    }
                    commentCard.setAssignTeacherTime(updateTime);
                    commentCard.setTeacherReadFlag(CommentCardStatus.TEACHER_UNREAD.getCode());
                    commentCard.setStudentReadFlag(CommentCardStatus.STUDENT_READ.getCode());
                    commentCard.setStatus(CommentCardStatus.ASSIGNED_TEACHER.getCode());
                    commentCard.setUpdateTime(updateTime);
                    commentCardJpaRepository.save(commentCard);
                    if(innerTeacherMap.get("teacherId") != null) {
                        JsonResultModel jsonResultModel = pushInfoToStudentAndTeacher(Long.parseLong(innerTeacherMap.get("teacherId").toString()), "学生发来一次求点评，点击查看。\n" +
                                "You’ve got a new answer to access; Do it now~", "FOREIGNCOMMENT");
                        if (jsonResultModel.getReturnCode().equals(HttpStatus.SC_OK)) {
                            logger.info("已经向教师端推送消息,推送的教师teacherId=" + innerTeacherMap.get("teacherId").toString());
                        } else {
                            logger.info("向教师端推送消息失败,推送失败的教师teacherId=" + innerTeacherMap.get("teacherId").toString());
                        }
                    }
                }
                logger.info("现在老师资源空缺,没有分配到老师的点评卡id为:"+fromTeacherStudentForm.getFishCardId());
            }else {
                logger.info("学生点评卡分配到老师,点评卡id为:"+fromTeacherStudentForm.getFishCardId());
                Date dateNow = new Date();
                commentCard.setStatus(CommentCardStatus.ASSIGNED_TEACHER.getCode());
                commentCard.setAssignTeacherTime(dateNow);
                commentCard.setTeacherId(fromTeacherStudentForm.getTeacherId());
                commentCard.setTeacherReadFlag(CommentCardStatus.TEACHER_UNREAD.getCode());
                commentCard.setUpdateTime(dateNow);
                logger.info("调用外教点评接口更新点评卡中外教点评内容,其中" + commentCard);
                commentCardJpaRepository.save(commentCard);
                JsonResultModel jsonResultModel = pushInfoToStudentAndTeacher(fromTeacherStudentForm.getTeacherId(),"学生发来一次求点评，点击查看。\n" +
                        "You’ve got a new answer to access; Do it now~","FOREIGNCOMMENT");
                if (jsonResultModel.getReturnCode().equals(HttpStatus.SC_OK)){
                    logger.info("已经向教师端推送消息,推送的教师teacherId=" + fromTeacherStudentForm.getTeacherId());
                }else {
                    logger.info("向教师端推送消息失败,推送失败的教师teacherId=" + fromTeacherStudentForm.getTeacherId());
                }
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
            logger.info("用户所查点评卡不存在,用户userId="+userId,", 点评卡id="+id);
            throw new UnauthorizedException();
        }
        if(commentCard.getStudentReadFlag() == CommentCardStatus.STUDENT_UNREAD.getCode()){
            commentCard.setStudentReadFlag(CommentCardStatus.STUDENT_READ.getCode());
            commentCardJpaRepository.save(commentCard);
        }
        return commentCard;
    }

    @Override
    @Transactional
    public void foreignTeacherCommentUnAnswer() {
        logger.info("调用--查询24小时未点评的外教--接口");
        // 超过24小时,未超过48小时
        LocalDateTime now = LocalDateTime.now();
        Date updateDate = DateUtil.localDate2Date(now);
        List<CommentCard> list = commentCardJpaRepository.findByDateRangeAndStatus(
//                DateUtil.localDate2Date(now.minusDays(2)),
//                DateUtil.localDate2Date(now.minusDays(1)),
                DateUtil.localDate2Date(now.minusMinutes(20)),
                DateUtil.localDate2Date(now.minusMinutes(10)),
                CommentCardStatus.ASSIGNED_TEACHER.getCode());
        for (CommentCard commentCard: list) {
            if (StringUtils.isEmpty(commentCard.getTeacherId())){
                logger.info("超过24小时没有分配到老师,为其分配内部账号,该点评卡id为:"+commentCard.getStudentId());
                Map paramMap = new HashMap<>();
                paramMap.put("fishCardId",commentCard.getId());
                paramMap.put("studentId",commentCard.getStudentId());
                paramMap.put("courseId",commentCard.getCourseId());
                Map innerTeacherMap = (Map)commentCardSDK.getInnerTeacherId(paramMap).getData();
                commentCard.setTeacherId(Long.parseLong(innerTeacherMap.get("teacherId").toString()));
                commentCard.setAssignTeacherCount(CommentCardStatus.ASSIGN_TEACHER_TWICE.getCode());
                commentCard.setAssignTeacherTime(updateDate);
                commentCard.setTeacherReadFlag(CommentCardStatus.TEACHER_UNREAD.getCode());
                commentCard.setStudentReadFlag(CommentCardStatus.STUDENT_READ.getCode());
                commentCard.setStatus(CommentCardStatus.ASSIGNED_TEACHER.getCode());
                commentCardJpaRepository.save(commentCard);
                JsonResultModel jsonResultModel = pushInfoToStudentAndTeacher(Long.parseLong(innerTeacherMap.get("teacherId").toString()),"学生发来一次求点评，点击查看。\n" +
                        "You’ve got a new answer to access; Do it now~","FOREIGNCOMMENT");
                if (jsonResultModel.getReturnCode().equals(HttpStatus.SC_OK)){
                    logger.info("已经向教师端推送消息,推送的教师teacherId=" + innerTeacherMap.get("teacherId").toString());
                }else {
                    logger.info("向教师端推送消息失败,推送失败的教师teacherId=" + innerTeacherMap.get("teacherId").toString());
                }
            }else {
                commentCard.setStudentReadFlag(CommentCardStatus.STUDENT_READ.getCode());
                commentCard.setStatus(CommentCardStatus.OVERTIME.getCode());
                commentCard.setUpdateTime(updateDate);
                logger.info("调用师生运营接口,设置参与该点评卡的外教为旷课......",commentCard);
                JsonResultModel jsonResultModel = commentCardSDK.setTeacherAbsence(commentCard.getTeacherId(),commentCard.getStudentId(),commentCard.getId());
                logger.info("调用师生运营接口结果",jsonResultModel);
                logger.info("向老师端推送消息,告知其点评超时......");
                JsonResultModel pushResult = pushInfoToStudentAndTeacher(
                        Long.parseLong(commentCard.getTeacherId().toString()),
                        createPushUnAnswerInfoToStudentAndTeacherMessage(commentCard),
                        "FOREIGNCOMMENT");
                logger.info("向老师端推送消息结果"+pushResult);
                CommentCard oldCommentCard = commentCardJpaRepository.save(commentCard);
                // 克隆点评卡
                CommentCard temp = oldCommentCard.cloneCommentCard();
                // 将这个点评卡转换为changeToOverTime状态
                temp.changeToOverTime();
                CommentCard newCommentCard = commentCardJpaRepository.save(temp);
                ToTeacherStudentForm toTeacherStudentForm = ToTeacherStudentForm.getToTeacherStudentForm(newCommentCard);
                logger.debug("@foreignTeacherCommentUnAnswer再次向师生运营发生消息,通知重新分配外教进行点评,重新分配的commentCard:"+newCommentCard);
                rabbitMqSender.send(toTeacherStudentForm, QueueTypeEnum.ASSIGN_FOREIGN_TEACHER_COMMENT);
            }
        }
        logger.info("所有在24小时内为被点评的学生已重新请求分配外教完毕,一共重新分配外教点评的个数为:"+list.size());
    }

    private String createPushUnAnswerInfoToStudentAndTeacherMessage(CommentCard commentCard) {
        String assignTeacherTime = commentCard.getAssignTeacherTime() == null ?
                "UNKNOW" : SimpleDateUtil.getTimeFromDate(commentCard.getAssignTeacherTime());
        String englishAssignTeacherTime = commentCard.getAssignTeacherTime() == null ?
                "UNKNOW" : SimpleDateUtil.getEnglishDate2(commentCard.getAssignTeacherTime());
        return  "You have not assessed the answer at "+ assignTeacherTime+
                " on "+ englishAssignTeacherTime +",in 24 hours. If you should not assess an answer again, you would be disqualified.\n" +
                "GET IT";
    }

    @Override
    @Transactional
    public void foreignTeacherCommentUnAnswer2() {
        logger.info("调用--查询48小时未点评的外教--接口");
        LocalDateTime now = LocalDateTime.now();
        List<CommentCard> list = commentCardJpaRepository.findByDateRangeAndStatus2(
//                DateUtil.localDate2Date(now.minusDays(3)),
//                DateUtil.localDate2Date(now.minusDays(2)),
                DateUtil.localDate2Date(now.minusMinutes(40)),
                DateUtil.localDate2Date(now.minusMinutes(20)),
                CommentCardStatus.ASSIGNED_TEACHER.getCode());
        Date updateDate = DateUtil.localDate2Date(now);
        for (CommentCard commentCard: list) {
            commentCard.changeToReturn();
            commentCard.setUpdateTime(updateDate);
            com.boxfishedu.workorder.entity.mysql.Service serviceTemp = serviceJpaRepository.findById(commentCard.getService().getId());
            commentCard.setService(serviceTemp);

            logger.info("调用师生运营接口,设置参与该点评卡的外教为旷课......",commentCard);
            JsonResultModel jsonResultModel = commentCardSDK.setTeacherAbsence(commentCard.getTeacherId(),commentCard.getStudentId(),commentCard.getId());
            logger.info("调用师生运营接口结果",jsonResultModel);
            logger.info("向老师端推送消息,告知其点评超时......");
            JsonResultModel pushResult = pushInfoToStudentAndTeacher(Long.parseLong(
                    commentCard.getTeacherId().toString()),
                    createPushUnAnswer2InfoToStudentAndTeacherMessage(commentCard),
                    "FOREIGNCOMMENT");
            logger.info("向老师端推送消息结果"+pushResult);

            commentCardJpaRepository.save(commentCard);
            serviceTemp.setAmount(serviceTemp.getAmount() + 1);
            serviceTemp.setUpdateTime(updateDate);
            serviceJpaRepository.save(serviceTemp);
            logger.info("外教在48小时内未点评,为学生返还点评次数...");
        }
        logger.info("所有学生外教点评次数返还完毕,一共返回次数为:"+list.size());
    }

    private String createPushUnAnswer2InfoToStudentAndTeacherMessage(CommentCard commentCard) {
        String assignTeacherTime = commentCard.getAssignTeacherTime() == null ?
                "UNKNOW" : SimpleDateUtil.getTimeFromDate(commentCard.getAssignTeacherTime());
        String englishAssignTeacherTime = commentCard.getAssignTeacherTime() == null ?
                "UNKNOW" : SimpleDateUtil.getEnglishDate2(commentCard.getAssignTeacherTime());
        return  "You have not assessed the answer at "+ assignTeacherTime +
                " on "+ englishAssignTeacherTime + ",in 24 hours. If you should not assess an answer again, you would be disqualified.\n" +
                "GET IT";
    }


    @Override
    @Transactional
    public void foreignUndistributedTeacherCommentCards() {
        logger.info("调用-查询24小时内暂时还未分配到老师的点评卡--接口,为其重新请求分配老师...");
        LocalDateTime now = LocalDateTime.now();
        List<CommentCard> list = commentCardJpaRepository.findUndistributedTeacher(
//                DateUtil.localDate2Date(now.minusDays(1)),
//                DateUtil.localDate2Date(now.minusDays(0)),
                DateUtil.localDate2Date(now.minusMinutes(10)),
                DateUtil.localDate2Date(now.minusMinutes(0)),
                CommentCardStatus.ASSIGNED_TEACHER.getCode());
        for (CommentCard commentCard: list) {
            ToTeacherStudentForm toTeacherStudentForm = ToTeacherStudentForm.getToTeacherStudentForm(commentCard);
            logger.debug("@foreignUndistributedTeacherCommentCards向师生运营发生消息,通知分配外教进行点评...");
            rabbitMqSender.send(toTeacherStudentForm, QueueTypeEnum.ASSIGN_FOREIGN_TEACHER_COMMENT);
        }
    }

    private JsonResultModel updateCommentAmount(com.boxfishedu.workorder.entity.mysql.Service service) {
        Date dateNow = new Date();
        service.setUpdateTime(dateNow);
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
        logger.info("学生端调用查询未读点评个数,用户id为:"+userId);
        JsonResultModel jsonResultModel = new JsonResultModel();
        jsonResultModel.setData(String.valueOf(commentCardJpaRepository.countStudentUnreadCommentCards(userId)));
        jsonResultModel.setReturnCode(HttpStatus.SC_OK);
        jsonResultModel.setReturnMsg("success");
        return jsonResultModel;
    }

    @Override
    public JsonResultModel countTeacherUnreadCommentCards(Long userId){
        logger.info("教师端调用查询未读点评个数,用户id为:"+userId);
        JsonResultModel jsonResultModel = new JsonResultModel();
        Map<String,Long> countMap = new LinkedHashMap<>();
        countMap.put("todoUnreadElements",commentCardTeacherAppService.countTeacherTodoUnread(userId));
        countMap.put("doneUnreadElements",commentCardTeacherAppService.countTeacherDoneUnread(userId));
        jsonResultModel.setData(countMap);
        jsonResultModel.setReturnCode(HttpStatus.SC_OK);
        jsonResultModel.setReturnMsg("success");
        return jsonResultModel;
    }

    @Override
    public void updateCommentCardsPictures(UpdatePicturesForm updatePicturesForm) {
        if(updatePicturesForm.getType().equals("STUDENT")){
            logger.info("调用点评卡修改头像接口---->修改的角色为:学生,userId="+updatePicturesForm.getId());
            commentCardJpaRepository.updateStudentPicture(updatePicturesForm.getFigure_url(),updatePicturesForm.getId());
        }else if (updatePicturesForm.getType().equals("TEACHER")){
            logger.info("调用点评卡修改头像接口---->修改的角色为:外教,userId="+updatePicturesForm.getId());
            commentCardJpaRepository.updateTeacherPicture(updatePicturesForm.getFigure_url(),updatePicturesForm.getId());
        }
    }
}
