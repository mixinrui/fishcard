package com.boxfishedu.workorder.servicex.commentcard;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.workorder.common.bean.AccountCourseBean;
import com.boxfishedu.workorder.common.bean.AccountCourseEnum;
import com.boxfishedu.workorder.common.bean.CommentCardStatus;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.common.util.ConstantUtil;
import com.boxfishedu.workorder.dao.jpa.CommentCardJpaRepository;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.accountcardinfo.AccountCardInfoService;
import com.boxfishedu.workorder.service.commentcard.CommentCardLogService;
import com.boxfishedu.workorder.service.commentcard.CommentCardTeacherAppService;
import com.boxfishedu.workorder.service.commentcard.ForeignTeacherCommentCardService;
import com.boxfishedu.workorder.service.commentcard.sdk.CommentCardSDK;
import com.boxfishedu.workorder.web.param.CommentCardSubmitParam;
import com.boxfishedu.workorder.web.param.commentcard.TeacherReadMsgParam;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @Autowired
    private ServiceJpaRepository serviceJpaRepository;

    @Autowired
    private RabbitMqSender rabbitMqSender;

    @Autowired
    private AccountCardInfoService accountCardInfoService;

    @Autowired
    ServeService serveService;

    @Autowired
    CommentCardJpaRepository commentCardJpaRepository;

    @Autowired
    UrlConf urlConf;

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    public CommentCard findById(Long cardId){
        return commentCardTeacherAppService.findById(cardId);
    }

    @Transactional
    public void markTeacherRead(TeacherReadMsgParam teacherReadMsgParam){
        CommentCard commentCard=this.findById(teacherReadMsgParam.getCommentCardId());
        if(null==commentCard){
            logger.error("不存在对应的点评卡,点评卡id[{}]",teacherReadMsgParam.getCommentCardId());
            throw new BusinessException("不存在对应的点评卡");
        }
//        commentCard.setTeacherReadFlag(CommentCardStatus.TEACHER_READ.getCode());
//        commentCardTeacherAppService.save(commentCard);
        logger.info("###markTeacherRead### 老师端标记点评卡已读");
        commentCardJpaRepository.markTeacherRead(teacherReadMsgParam.getCommentCardId());
    }

    @Transactional
    public void submitComment(@RequestBody CommentCardSubmitParam commentCardSubmitParam){
        CommentCard commentCard=commentCardTeacherAppService.findById(commentCardSubmitParam.getCommentCardId());
        if(null==commentCard){
            throw new BusinessException("不存在对应的点评卡");
        }

        // 提交时验证点评状态
        if(!Objects.equals(commentCard.getStatus(),CommentCardStatus.ASSIGNED_TEACHER.getCode())) {
            throw new BusinessException("Sorry! You do not have enough authorization.");
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
        //填充主页内容
        CommentCard homeCommentCard = commentCardJpaRepository.getHomePageCommentCard(commentCard.getStudentId());
        if (Objects.equals(homeCommentCard.getId(),commentCard.getId())){
            logger.info("@submitComment 更新首页中外C教点评项...");
            setCommentHomePage(commentCard);
        }
        logger.info("@submitComment 更新首页中外教点评项...");
        findHomeComment(commentCard.getStudentId());
    }

    public CommentCard checkTeacher(Long id, Long teacherId){
        return commentCardTeacherAppService.checkTeacher(id, teacherId);
    }

    public void findHomeComment(Long studentId) {
        Integer amount = serveService.getForeignCommentServiceCount(studentId).get("amount");
        if (amount == 0){
            List<CommentCard> commentCardList0 = commentCardJpaRepository.getUncommentedCard(studentId);
            if (commentCardList0.size() == 0){
                logger.info("@findHomeComment1 次数用尽,且点评都已查看!");
                accountCardInfoService.saveOrUpdate(studentId,new AccountCourseBean(), AccountCourseEnum.CRITIQUE);
                return;
            }
        }
        List<CommentCard> commentCardList1 = commentCardJpaRepository.getTeacherNewCommentCard(studentId);
        if (commentCardList1.size() != 0){
//            CommentCard commentCard = commentCardList1.get(0);
//            if (commentCard.getStatus().equals(CommentCardStatus.STUDENT_COMMENT_TO_TEACHER.getCode())){
//                commentCard.setStatus(CommentCardStatus.ANSWERED.getCode());
//            }
            setCommentHomePage(commentCardList1.get(0));
        }else {
            List<CommentCard> commentCardList2 = commentCardJpaRepository.getStudentNewCommentCard(studentId);
            if (commentCardList2.size() != 0){
                logger.info("@findHomeComment2 尚未有已完成的点评记录!");
                setCommentHomePage(commentCardList2.get(0));
            }else {
                setDefaultHomeComment(studentId);
            }
        }
    }

    public void setCommentHomePage(CommentCard commentCard) {
        logger.info("@setCommentHomePage1 设置外教点评首页" + commentCard);
        AccountCourseBean accountCourseBean = new AccountCourseBean();
        AccountCourseBean.CardCourseInfo cardCourseInfo = new AccountCourseBean.CardCourseInfo();
        cardCourseInfo.setCourseId(commentCard.getCourseId());
        cardCourseInfo.setCourseName(commentCard.getCourseName());
        if (Objects.nonNull(commentCard.getCourseId())){
            try {
                Map typeAndDifficultyMap = commentCardSDK.commentTypeAndDifficulty(commentCard.getCourseId());
                if (Objects.nonNull(typeAndDifficultyMap)){
                    if (Objects.nonNull(typeAndDifficultyMap.get("courseType"))){
                        cardCourseInfo.setCourseType(typeAndDifficultyMap.get("courseType").toString());
                    }
                    if (Objects.nonNull(typeAndDifficultyMap.get("courseDifficulty"))){
                        cardCourseInfo.setDifficulty(getLevel(typeAndDifficultyMap.get("courseDifficulty").toString()).toString());
                    }
                }
            }catch (Exception e){
                logger.error("@setCommentHomePage2 获取难度类型失败");
                e.printStackTrace();
            }
        }
        cardCourseInfo.setThumbnail(urlConf.getThumbnail_server()+commentCard.getCover());
        cardCourseInfo.setStudentReadFlag(commentCard.getStudentReadFlag());
        cardCourseInfo.setStatus(commentCard.getStatus());
        accountCourseBean.setLeftAmount(serveService.getForeignCommentServiceCount(commentCard.getStudentId()).get("amount"));
        accountCourseBean.setCourseInfo(cardCourseInfo);
        logger.info("@setCommentHomePage2 设置外教点评首页");
        accountCardInfoService.saveOrUpdate(commentCard.getStudentId(),accountCourseBean, AccountCourseEnum.CRITIQUE);
    }

    public void setDefaultHomeComment(Long userId){
        logger.info("@setDefaultHomeComment 首次购买外教点评,设置首页信息...");
        AccountCourseBean accountCourseBean = new AccountCourseBean();
        accountCourseBean.setLeftAmount(serveService.getForeignCommentServiceCount(userId).get("amount"));
        accountCardInfoService.saveOrUpdate(userId,accountCourseBean, AccountCourseEnum.CRITIQUE);
    }

    private Integer getLevel(String levelStr){
        switch (levelStr){
            case "LEVEL_1":
                return 1;
            case "LEVEL_2":
                return 2;
            case "LEVEL_3":
                return 3;
            case "LEVEL_4":
                return 4;
            case "LEVEL_5":
                return 5;
        }
        return -1;
    }

    //初始化所有外教点评主页相关
    public void initializeCommentHomePage(){
        List<Long> longs = commentCardJpaRepository.getCommentCardHomePageList();
        int sum = 0;
        for(Long studentId: longs){
            findHomeComment(studentId);
            sum +=1 ;
        }
        logger.info("@initializeCommentHomePage 初始化首页中外教点评相关项完毕,初始化个数为:"+sum);
    }

    private void notifyOrderUpdateStatus(Long orderId, Integer status) {
        logger.info("@notifyOrderUpdateStatus 通知订单中心修改状体...");
        Map param = Maps.newHashMap();
        param.put("id",orderId.toString());
        param.put("status",status.toString());
        rabbitMqSender.send(param, QueueTypeEnum.NOTIFY_ORDER);
    }

    public CommentCard checkCommentCard(Long cardId,Long teacherId){
        logger.info("@checkCommentCard 第2次检查点评是否过期");
        return commentCardJpaRepository.findByIdAndTeacherId(cardId,teacherId);
    }
}
