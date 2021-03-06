package com.boxfishedu.workorder.service.commentcard;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.mall.enums.ProductType;
import com.boxfishedu.workorder.common.bean.*;
import com.boxfishedu.workorder.common.config.CommentCardTimeConf;
import com.boxfishedu.workorder.common.config.ServiceGateWayType;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.exception.NotFoundException;
import com.boxfishedu.workorder.common.exception.UnauthorizedException;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.common.redis.CacheKeyConstant;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JSONParser;
import com.boxfishedu.workorder.dao.jpa.CommentCardJpaRepository;
import com.boxfishedu.workorder.dao.jpa.CommentCardStatisticsJpaRepository;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.entity.mysql.*;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.accountcardinfo.AccountCardInfoService;
import com.boxfishedu.workorder.service.commentcard.sdk.CommentCardSDK;
import com.boxfishedu.workorder.servicex.commentcard.CommentTeacherAppServiceX;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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
    CommentCardStatisticsJpaRepository commentCardStatisticsJpaRepository;

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

    @Autowired
    ServiceGateWayType serviceGateWayType;

    @Autowired
    CommentTeacherAppServiceX commentTeacherAppServiceX;

    @Autowired
    AccountCardInfoService accountCardInfoService;

    @Autowired
    SyncCommentCard2SystemService syncCommentCard2SystemService;

    @Autowired
    CommentCardTimeConf commentCardTimeConf;

    @Autowired
    private CacheManager cacheManager;

    private Logger logger = LoggerFactory.getLogger(ForeignTeacherCommentCardServiceImpl.class);

    @Override
    @Transactional
    public CommentCard foreignTeacherCommentCardAdd(CommentCardForm commentCardForm, Long userId, String access_token) {
        commentCardForm.setCourseType(null);
        commentCardForm.setCourseDifficulty(null);
        try {
            Map courseMap = commentCardSDK.commentTypeAndDifficulty(commentCardForm.getCourseId());
            if (Objects.nonNull(courseMap)) {
                commentCardForm.setCourseType(courseMap.get("type") == null ? "" : courseMap.get("type").toString());
                commentCardForm.setCourseDifficulty(courseMap.get("difficulty") == null ? "" : courseMap.get("difficulty").toString());
            }
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        CommentCard commentCard=CommentCard.getCommentCard(commentCardForm);
        if(!serveService.findFirstAvailableForeignCommentService(userId).isPresent()){
            throw new BusinessException("学生的外教点评次数已经用尽,请先购买!");
        }
        com.boxfishedu.workorder.entity.mysql.Service service= serveService.findFirstAvailableForeignCommentService(userId).get();
        if(service.getAmount() <= 0){
            throw new BusinessException("学生的外教点评次数已经用尽,请先购买!");
        }else {
            CommentCard newCommentCard = null;
            boolean flag = true;
            try{
                service.setAmount(service.getAmount() - 1);
                updateCommentAmount(service);
                commentCard.setStudentId(userId);
                commentCard.setService(service);
                commentCard.setOrderId(service.getOrderId());
                commentCard.setOrderCode(service.getOrderCode());
                commentCard.setStudentPicturePath(getUserPicture(access_token));
                logger.info("@foreignTeacherCommentCardAdd1 调用外教点评接口新增学生点评卡,其中"+commentCard);
                Date dateNow = new Date();
                commentCard.setStudentAskTime(dateNow);
                commentCard.setCreateTime(dateNow);
                commentCard.setUpdateTime(dateNow);
                commentCard.setAssignTeacherCount(CommentCardStatus.ASSIGN_TEACHER_ONCE.getCode());
                commentCard.setStudentReadFlag(CommentCardStatus.STUDENT_READ.getCode());
                commentCard.setStatus(CommentCardStatus.REQUEST_ASSIGN_TEACHER.getCode());
                newCommentCard = commentCardJpaRepository.save(commentCard);
                logger.info("@foreignTeacherCommentCardAdd点评次数消耗一次...");
                CommentCardStatistics commentCardStatistics = new CommentCardStatistics();
                commentCardStatistics.setCommentCardId(newCommentCard.getId());
                commentCardStatistics.setStudentId(newCommentCard.getStudentId());
                commentCardStatistics.setAmount(service.getAmount());
                commentCardStatistics.setServicedId(service.getId());
                commentCardStatistics.setOperationType(CommentCardStatus.AMOUNT_MINUS.getCode());
                commentCardStatisticsJpaRepository.save(commentCardStatistics);
                if(commentCardJpaRepository.getCommentedCard(userId).size() == 0){
                    logger.info("@foreignTeacherCommentCardAdd1 首页中添加外教点评信息...");
                    commentTeacherAppServiceX.setCommentHomePage(commentCard);
                }else{
                    logger.info("@foreignTeacherCommentCardAdd2 修改首页中外教点评次数");
                    commentTeacherAppServiceX.setCommentHomePage(commentCardJpaRepository.getHomePageCommentCard(userId));
                }
                logger.info("@foreignTeacherCommentCardAdd2 发起外教点评,设置首页...");
                commentTeacherAppServiceX.findHomeComment(userId);
                return newCommentCard;
            }catch (Exception e){
                flag = false;
                throw new RuntimeException(e);
            }finally {
                if(flag){
                    ToTeacherStudentForm toTeacherStudentForm = ToTeacherStudentForm.getToTeacherStudentForm(newCommentCard);
                    logger.debug("@foreignTeacherCommentCardAdd3 向师生运营发生消息,通知分配外教进行点评...");
                    rabbitMqSender.send(toTeacherStudentForm, QueueTypeEnum.ASSIGN_FOREIGN_TEACHER_COMMENT);
                    logger.info("@foreignTeacherCommentCardAdd4 点评次数修改,通知客服系统...");
                    syncCommentCard2SystemService.syncCommentCard2System(service.getId(),commentCard.getStatus(),commentCard.getTeacherAnswerTime());
                }
            }
        }
    }

    @Override
    public void foreignTeacherCommentUpdateAnswer(FromTeacherStudentForm fromTeacherStudentForm) {
        CommentCard commentCard = commentCardJpaRepository.findOne(fromTeacherStudentForm.getFishCardId());
        String name;
        logger.info("@foreignTeacherCommentUpdateAnswer1 接收师生运营分配老师:"+fromTeacherStudentForm+",并准备修改点评卡:"+commentCard);
        if (Objects.isNull(commentCard)){
            logger.info("@foreignTeacherCommentUpdateAnswer2 根据点评卡id查到的点评卡为空,点评卡id来自师生运营回传参数:"+fromTeacherStudentForm);
            throw new UnauthorizedException("不存在的点评卡!");
        }else {
            if(Objects.isNull(fromTeacherStudentForm.getTeacherId())){
                logger.info("@foreignTeacherCommentUpdateAnswer3 现在老师资源空缺,没有分配到老师的点评卡id为:"+fromTeacherStudentForm.getFishCardId());
                //2016-09-03更改为第一次请求分配老师失败就分给内部账号
                Date updateTime = new Date();
                Map paramMap = new HashMap<>();
                paramMap.put("fishCardId",commentCard.getId());
                paramMap.put("studentId",commentCard.getStudentId());
                paramMap.put("courseId",commentCard.getCourseId());
                InnerTeacher innerTeacher = commentCardSDK.getInnerTeacherId(paramMap).getData(InnerTeacher.class);
                logger.info("InnerTeacher0 is "+ innerTeacher);
                if(Objects.isNull(innerTeacher) || Objects.isNull(innerTeacher.getTeacherId())) {
                    return;
                }
                commentCard.setTeacherId(innerTeacher.getTeacherId());
                name = getTeacherName(innerTeacher.getTeacherFirstName(),innerTeacher.getTeacherLastName());
                logger.info("==========================>@166name:"+name+" lastName"+innerTeacher.getTeacherLastName());
                commentCard.setTeacherName(name);
                commentCard.setTeacherFirstName(innerTeacher.getTeacherFirstName());
                commentCard.setTeacherLastName(innerTeacher.getTeacherLastName());
                commentCard.setTeacherStatus(CommentCardStatus.TEACHER_NORMAL.getCode());
                commentCard.setAssignTeacherTime(updateTime);
                commentCard.setTeacherReadFlag(CommentCardStatus.TEACHER_UNREAD.getCode());
                commentCard.setStudentReadFlag(CommentCardStatus.STUDENT_READ.getCode());
                commentCard.setStatus(CommentCardStatus.ASSIGNED_TEACHER.getCode());
                commentCard.setUpdateTime(updateTime);
                commentCardJpaRepository.save(commentCard);
                JsonResultModel jsonResultModel = pushInfoToStudentAndTeacher(innerTeacher.getTeacherId(), "You’ve got a new answer to access; Do it now~", "FOREIGNCOMMENT");
                if (jsonResultModel.getReturnCode().equals(HttpStatus.SC_OK)) {
                    logger.info("@foreignTeacherCommentUpdateAnswer4 已经向教师端推送消息,推送的教师teacherId=" + innerTeacher.getTeacherId());
                } else {
                    logger.info("@foreignTeacherCommentUpdateAnswer5 向教师端推送消息失败,推送失败的教师teacherId=" + innerTeacher.getTeacherId());
                }
            }else {
                logger.info("Teacher is "+fromTeacherStudentForm);
                Date dateNow = new Date();
                commentCard.setStatus(CommentCardStatus.ASSIGNED_TEACHER.getCode());
                commentCard.setAssignTeacherTime(dateNow);
                commentCard.setTeacherId(fromTeacherStudentForm.getTeacherId());
                commentCard.setTeacherFirstName(fromTeacherStudentForm.getTeacherFirstName());
                commentCard.setTeacherLastName(fromTeacherStudentForm.getTeacherLastName());
                name = getTeacherName(fromTeacherStudentForm.getTeacherFirstName(),fromTeacherStudentForm.getTeacherLastName());
                logger.info("==========================>@193name:"+name+" lastName:"+fromTeacherStudentForm.getTeacherLastName());
                commentCard.setTeacherName(name);
                commentCard.setTeacherStatus(CommentCardStatus.TEACHER_NORMAL.getCode());
                commentCard.setTeacherReadFlag(CommentCardStatus.TEACHER_UNREAD.getCode());
                commentCard.setUpdateTime(dateNow);
                logger.info("@foreignTeacherCommentUpdateAnswer6 调用外教点评接口更新点评卡中外教点评内容,其中" + commentCard);
                commentCardJpaRepository.save(commentCard);
                JsonResultModel jsonResultModel = pushInfoToStudentAndTeacher(fromTeacherStudentForm.getTeacherId(),"You’ve got a new answer to access; Do it now~","FOREIGNCOMMENT");
                if (jsonResultModel.getReturnCode().equals(HttpStatus.SC_OK)){
                    logger.info("@foreignTeacherCommentUpdateAnswer7 已经向教师端推送消息,推送的教师teacherId=" + fromTeacherStudentForm.getTeacherId());
                }else {
                    logger.info("@foreignTeacherCommentUpdateAnswer8 向教师端推送消息失败,推送失败的教师teacherId=" + fromTeacherStudentForm.getTeacherId());
                }
            }
        }
    }

    @Override
    public Map foreignTeacherCommentQuery(Pageable pageable, Long studentId) {
        logger.info("@foreignTeacherCommentQuery调用学生查询外教点评列表接口,其中studentId="+studentId+"pageable="+pageable);
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
    @Transactional
    public CommentCard foreignTeacherCommentDetailQuery(Long id,Long userId) {
        logger.info("@foreignTeacherCommentDetailQuery1 调用学生查询某条外教点评具体信息接口,并将此条设置为已读,其中id="+id);
        CommentCard commentCard = commentCardJpaRepository.findByIdAndStudentId(id,userId);
        if(Objects.isNull(commentCard)){
            logger.info("用户所查点评卡不存在,用户userId="+userId,", 点评卡id="+id);
            throw new UnauthorizedException();
        }
        if(commentCard.getStudentReadFlag() == CommentCardStatus.STUDENT_UNREAD.getCode()){
            commentCard.setStudentReadFlag(CommentCardStatus.STUDENT_READ.getCode());
//            commentCardJpaRepository.save(commentCard);
            commentCardJpaRepository.markStudentRead(commentCard.getId());
        }
        CommentCard homeCommentCard = commentCardJpaRepository.getHomePageCommentCard(userId);
        Optional<com.boxfishedu.workorder.entity.mysql.Service> optional =
                serveService.findFirstAvailableForeignCommentService(userId);
        int amount = 0;
        if(optional.isPresent()) {
            amount = optional.get().getAmount();
        }
        if (Objects.nonNull(homeCommentCard)){
            if (Objects.equals(id,homeCommentCard.getId())){
                if ((commentCardJpaRepository.getUncommentedCard(userId).size() == 0 ) && amount == 0){
                    logger.info("@setHomePage1 次数用尽,重置外教点评首页...");
                    accountCardInfoService.saveOrUpdate(userId,new AccountCourseBean(), AccountCourseEnum.CRITIQUE);
                } else {
                    logger.info("@setHomePage2 设置外教点评首页...");
                    commentTeacherAppServiceX.setCommentHomePage(commentCard);
                }
            }
        }
        logger.info("@foreignTeacherCommentDetailQuery 查看详情时设置首页...");
        commentTeacherAppServiceX.findHomeComment(userId);
        return commentCard;
    }

    /**
     * 24-48小时之间业务处理,直接分配内部账号
     */
    @Override
    public void foreignTeacherCommentUnAnswer() {
        String name;
        logger.info("@foreignTeacherCommentUnAnswer12 调用--查询24小时未点评的外教--接口");
        // 超过24小时,未超过48小时
        LocalDateTime now = LocalDateTime.now();
        Date updateDate = DateUtil.localDate2Date(now);
        //2016-12-08 修改为从配置文件获取过滤时间
        System.out.println("nodeOne:"+commentCardTimeConf.getNodeOne() + ",nodeTwo:" + commentCardTimeConf.getNodeTwo()
                        +",nodeThree:"+commentCardTimeConf.getNodeThree()+",nodeFour:" + commentCardTimeConf.getNodeFour());
        List<CommentCard> list = commentCardJpaRepository.findByDateRangeAndStatus(
                DateUtil.localDate2Date(now.minusMinutes(Long.parseLong(commentCardTimeConf.getNodeThree()))),
                DateUtil.localDate2Date(now.minusMinutes(Long.parseLong(commentCardTimeConf.getNodeTwo()))),
                CommentCardStatus.ASSIGNED_TEACHER.getCode());
        for (CommentCard commentCard : list) {
            try {
                // 1 24小时未分配外教的,直接分配内部账号
                if (Objects.isNull(commentCard.getTeacherId())) {
                    logger.info("@foreignTeacherCommentUnAnswer12 超过24小时没有分配到老师,为其分配内部账号,该点评卡id为:" + commentCard.getStudentId());
                    Map paramMap = new HashMap<>();
                    paramMap.put("fishCardId", commentCard.getId());
                    paramMap.put("studentId", commentCard.getStudentId());
                    paramMap.put("courseId", commentCard.getCourseId());
                    InnerTeacher innerTeacher = commentCardSDK.getInnerTeacherId(paramMap).getData(InnerTeacher.class);
                    logger.info("InnerTeacher1 is "+ innerTeacher);
                    commentCard.setTeacherId(innerTeacher.getTeacherId());
                    name = getTeacherName(innerTeacher.getTeacherFirstName(),innerTeacher.getTeacherLastName());
                    logger.info("==========================>@271name:"+name+" lastName:"+innerTeacher.getTeacherLastName());
                    commentCard.setTeacherName(name);
                    commentCard.setTeacherFirstName(innerTeacher.getTeacherFirstName());
                    commentCard.setTeacherLastName(innerTeacher.getTeacherLastName());
                    commentCard.setTeacherStatus(CommentCardStatus.TEACHER_NORMAL.getCode());
                    commentCard.setAssignTeacherCount(CommentCardStatus.ASSIGN_TEACHER_TWICE.getCode());
                    commentCard.setAssignTeacherTime(updateDate);
                    commentCard.setTeacherReadFlag(CommentCardStatus.TEACHER_UNREAD.getCode());
                    commentCard.setStudentReadFlag(CommentCardStatus.STUDENT_READ.getCode());
                    commentCard.setStatus(CommentCardStatus.ASSIGNED_TEACHER.getCode());
                    commentCardJpaRepository.save(commentCard);
                    JsonResultModel jsonResultModel = pushInfoToStudentAndTeacher(innerTeacher.getTeacherId(), "You’ve got a new answer to access; Do it now~", "FOREIGNCOMMENT");
                    if (jsonResultModel.getReturnCode().equals(HttpStatus.SC_OK)) {
                        logger.info("@foreignTeacherCommentUnAnswer13 已经向教师端推送消息,推送的教师teacherId=" + innerTeacher.getTeacherId());
                    } else {
                        logger.info("@foreignTeacherCommentUnAnswer14 向教师端推送消息失败,推送失败的教师teacherId=" + innerTeacher.getTeacherId());
                    }
                }
                // 2 将之前的点评标记为过期,并且复制一个新的点评,重新分配内部账号
                else {
                    commentCard.setStudentReadFlag(CommentCardStatus.STUDENT_READ.getCode());
                    commentCard.setTeacherReadFlag(CommentCardStatus.TEACHER_UNREAD.getCode());
                    commentCard.setStatus(CommentCardStatus.OVERTIME.getCode());
                    commentCard.setUpdateTime(updateDate);
                    logger.info("@foreignTeacherCommentUnAnswer15 调用师生运营接口,设置参与该点评卡的外教为旷课......", commentCard);
                    JsonResultModel jsonResultModel = commentCardSDK.setTeacherAbsence(commentCard.getTeacherId(), commentCard.getStudentId(), commentCard.getId());
                    logger.info("调用师生运营接口结果", jsonResultModel);
                    logger.info("向老师端推送消息,告知其点评超时......");
                    JsonResultModel pushResult = pushInfoToStudentAndTeacher(
                            Long.parseLong(commentCard.getTeacherId().toString()),
                            createPushUnAnswer2InfoToStudentAndTeacherMessage(commentCard),
                            "FOREIGNCOMMENT");
                    logger.info("向老师端推送消息结果" + pushResult);
                    CommentCard oldCommentCard = commentCardJpaRepository.save(commentCard);
                    // 克隆点评卡
                    //2016-09-1后将24小时后的处理逻辑改为直接分配内部老师
                    CommentCard newCommentCard = commentCardJpaRepository.save(oldCommentCard.cloneCommentCard());
                    newCommentCard.setAssignTeacherCount(CommentCardStatus.ASSIGN_TEACHER_TWICE.getCode());
                    newCommentCard.setAssignTeacherTime(updateDate);
                    newCommentCard.setTeacherReadFlag(CommentCardStatus.TEACHER_READ.getCode());
                    newCommentCard.setStudentReadFlag(CommentCardStatus.STUDENT_READ.getCode());
                    newCommentCard.setStatus(CommentCardStatus.REQUEST_ASSIGN_TEACHER.getCode());
                    newCommentCard.setCreateTime(updateDate);
                    if(Objects.nonNull(commentCard.getPrevious_id())) {
                        newCommentCard.setPrevious_id(commentCard.getPrevious_id());
                    } else {
                        newCommentCard.setPrevious_id(commentCard.getId());
                    }
                    commentCardJpaRepository.save(newCommentCard);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.info("@foreignTeacherCommentUnAnswer16 所有在24小时内为被点评的学生已重新请求分配外教完毕,一共重新分配外教点评的个数为:" + list.size());
        }
    }

    /**
     * 超过48小时未点评逻辑处理,退次数
     */
    @Override
    @Transactional
    public void foreignTeacherCommentUnAnswer2() {
        logger.info("@foreignTeacherCommentUnAnswer21 调用--查询48小时未点评的外教--接口");
        LocalDateTime now = LocalDateTime.now();
        //2016-12-08 修改为从配置文件获取过滤时间
        System.out.println("nodeOne2:"+commentCardTimeConf.getNodeOne() + ",nodeTwo2:" + commentCardTimeConf.getNodeTwo()
                +",nodeThree2:"+commentCardTimeConf.getNodeThree()+",nodeFour2:" + commentCardTimeConf.getNodeFour());
        List<CommentCard> list = commentCardJpaRepository.findByDateRangeAndStatus2(
                DateUtil.localDate2Date(now.minusMinutes(Long.parseLong(commentCardTimeConf.getNodeFour()))),
                DateUtil.localDate2Date(now.minusMinutes(Long.parseLong(commentCardTimeConf.getNodeThree()))),
                CommentCardStatus.ASSIGNED_TEACHER.getCode());
        Date updateDate = DateUtil.localDate2Date(now);
        for (CommentCard commentCard: list) {
            try {
                // 退还次数
                commentCard.changeToReturn();
                commentCard.setUpdateTime(updateDate);
                com.boxfishedu.workorder.entity.mysql.Service serviceTemp = serviceJpaRepository.findById(commentCard.getService().getId());
                commentCard.setService(serviceTemp);
                if (commentCard.getTeacherId() != null) {
                    logger.info("@foreignTeacherCommentUnAnswer22 调用师生运营接口,设置参与该点评卡的外教为旷课......" + commentCard);
                    JsonResultModel jsonResultModel = commentCardSDK.setTeacherAbsence(commentCard.getTeacherId(), commentCard.getStudentId(), commentCard.getId());
                    logger.info("调用师生运营接口结果", jsonResultModel);
                    logger.info("向老师端推送消息,告知其点评超时......");
                    JsonResultModel pushResult = pushInfoToStudentAndTeacher(Long.parseLong(
                            commentCard.getTeacherId().toString()),
                            createPushUnAnswer2InfoToStudentAndTeacherMessage(commentCard),
                            "FOREIGNCOMMENT");
                    logger.info("@foreignTeacherCommentUnAnswer23 向老师端推送消息结果" + pushResult + ", 推送消息为:" + createPushUnAnswer2InfoToStudentAndTeacherMessage(commentCard));
                }
                commentCardJpaRepository.save(commentCard);
                serviceTemp.setAmount(serviceTemp.getAmount() + 1);
                serviceTemp.setUpdateTime(updateDate);

                // 刷新点评次数缓存
                cacheManager.getCache(CacheKeyConstant.COMMENT_CARD_AMOUNT).evict(commentCard.getStudentId());
                serviceJpaRepository.save(serviceTemp);
                logger.info("@foreignTeacherCommentUnAnswer24 外教在48小时内未点评,为学生返还点评次数...");
                CommentCardStatistics commentCardStatistics = new CommentCardStatistics();
                commentCardStatistics.setCommentCardId(commentCard.getId());
                commentCardStatistics.setStudentId(commentCard.getStudentId());
                commentCardStatistics.setAmount(serviceTemp.getAmount());
                commentCardStatistics.setServicedId(serviceTemp.getId());
                commentCardStatistics.setOperationType(CommentCardStatus.AMOUNT_ADD.getCode());
                commentCardStatisticsJpaRepository.save(commentCardStatistics);
                logger.info("@foreignTeacherCommentUnAnswer25 修改首页中外教点评次数");
                commentTeacherAppServiceX.setCommentHomePage(commentCardJpaRepository.getHomePageCommentCard(commentCard.getStudentId()));
                commentTeacherAppServiceX.findHomeComment(commentCard.getStudentId());
                logger.info("@foreignTeacherCommentUnAnswer26 次数修改,通知客服系统....");
                syncCommentCard2SystemService.syncCommentCard2System(serviceTemp.getId(),commentCard.getStatus(),commentCard.getTeacherAnswerTime());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        logger.info("@foreignTeacherCommentUnAnswer26 所有学生外教点评次数返还完毕,一共返回次数为:"+list.size());
    }

    private String createPushUnAnswer2InfoToStudentAndTeacherMessage(CommentCard commentCard) {
        //2016-11-16 由于推送时间无法转换时区,所以修改.
        return  "You have not assessed an answer in 24 hours. \n" +
                "If you should fail to do that again, you would be disqualified.";
    }

    /**
     * 扫描未分配老师点评,为其分配内部账号
     */
    @Override
    public void foreignUndistributedTeacherCommentCards() {
        String name;
        logger.info("@foreignUndistributedTeacherCommentCards1 调用-查询24小时内暂时还未分配到老师的点评卡--接口,为其重新请求分配老师...");
        LocalDateTime now = LocalDateTime.now();
        //2016-12-08 修改为从配置文件获取过滤时间
        System.out.println("nodeOne3:"+commentCardTimeConf.getNodeOne() + ",nodeTwo3:" + commentCardTimeConf.getNodeTwo()
                +",nodeThree3:"+commentCardTimeConf.getNodeThree()+",nodeFour3:" + commentCardTimeConf.getNodeFour());
        List<CommentCard> list = commentCardJpaRepository.findUndistributedTeacher(
                DateUtil.localDate2Date(now.minusMinutes(Long.parseLong(commentCardTimeConf.getNodeFour()))),
                DateUtil.localDate2Date(now.minusMinutes(Long.parseLong(commentCardTimeConf.getNodeOne()))),
                CommentCardStatus.ASSIGNED_TEACHER.getCode());
        for (CommentCard commentCard: list) {
            try {
                Map paramMap = new HashMap<>();
                paramMap.put("fishCardId", commentCard.getId());
                paramMap.put("studentId", commentCard.getStudentId());
                paramMap.put("courseId", commentCard.getCourseId());
                InnerTeacher innerTeacher = commentCardSDK.getInnerTeacherId(paramMap).getData(InnerTeacher.class);
                logger.info("InnerTeacher2 is "+ innerTeacher);
                commentCard.setTeacherId(innerTeacher.getTeacherId());
                name = getTeacherName(innerTeacher.getTeacherFirstName(),innerTeacher.getTeacherLastName());
                logger.info("==========================>@461name:"+name+" lastName:"+innerTeacher.getTeacherLastName());
                commentCard.setTeacherName(name);
                commentCard.setTeacherFirstName(innerTeacher.getTeacherFirstName());
                commentCard.setTeacherLastName(innerTeacher.getTeacherLastName());
                commentCard.setTeacherStatus(CommentCardStatus.TEACHER_NORMAL.getCode());
                commentCard.setAssignTeacherTime(new Date());
                commentCard.setTeacherReadFlag(CommentCardStatus.TEACHER_UNREAD.getCode());
                commentCard.setStudentReadFlag(CommentCardStatus.STUDENT_READ.getCode());
                commentCard.setStatus(CommentCardStatus.ASSIGNED_TEACHER.getCode());
                commentCardJpaRepository.save(commentCard);
                JsonResultModel jsonResultModel = pushInfoToStudentAndTeacher(innerTeacher.getTeacherId(), "You’ve got a new answer to access; Do it now~", "FOREIGNCOMMENT");
                if (jsonResultModel.getReturnCode().equals(HttpStatus.SC_OK)) {
                    logger.info("@foreignUndistributedTeacherCommentCards2 已经向教师端推送消息,推送的教师teacherId=" + innerTeacher.getTeacherId());
                } else {
                    logger.info("@foreignUndistributedTeacherCommentCards3 向教师端推送消息失败,推送失败的教师teacherId=" + innerTeacher.getTeacherId());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private JsonResultModel updateCommentAmount(com.boxfishedu.workorder.entity.mysql.Service service) {
        Date dateNow = new Date();
        service.setUpdateTime(dateNow);
        logger.info("@updateCommentAmount 调用修改学生点评次数接口,其中service="+service);
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
        commentCardSDK.pushToStudentAndTeacher(userId,title,type);
        return new JsonResultModel();
    }

    @Override
    public JsonResultModel countStudentUnreadCommentCards(Long userId) {
        logger.info("@countStudentUnreadCommentCards 学生端调用查询未读点评个数,用户id为:"+userId);
        JsonResultModel jsonResultModel = new JsonResultModel();
        jsonResultModel.setData(String.valueOf(commentCardJpaRepository.countStudentUnreadCommentCards(userId)));
        jsonResultModel.setReturnCode(HttpStatus.SC_OK);
        jsonResultModel.setReturnMsg("success");
        return jsonResultModel;
    }

    @Override
    public JsonResultModel countTeacherUnreadCommentCards(Long userId){
        logger.info("@countTeacherUnreadCommentCards 教师端调用查询未读点评个数,用户id为:"+userId);
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
    @Transactional
    public void updateCommentCardsPictures(UpdatePicturesForm updatePicturesForm) {
        if(Objects.nonNull(updatePicturesForm) && Objects.equals(updatePicturesForm.getType(), "STUDENT")){
            logger.info("@updateCommentCardsPictures1 调用点评卡修改头像接口---->修改的角色为:学生,userId="+updatePicturesForm.getId());
            commentCardJpaRepository.updateStudentPicture(updatePicturesForm.getFigure_url(),updatePicturesForm.getId());
        }else if (Objects.nonNull(updatePicturesForm) && Objects.equals(updatePicturesForm.getType(), "TEACHER")){
            logger.info("@updateCommentCardsPictures2 调用点评卡修改头像接口---->修改的角色为:外教,userId="+updatePicturesForm.getId());
            commentCardJpaRepository.updateTeacherPicture(updatePicturesForm.getFigure_url(),updatePicturesForm.getId());
        }
    }

    @Override
    public void forceToChangeTeacher(Long fromTeacherId, Long toTeacherId) {
        logger.info("@forceToChangeTeacher 外教点评强制换掉老师");
        Date date = new Date();
        List<CommentCard> commentCardList = commentCardJpaRepository.findByTeacherIdAndStatus(fromTeacherId,CommentCardStatus.ASSIGNED_TEACHER.getCode());
        for (CommentCard oldCommentCard :commentCardList){
            CommentCard newCommentCard = oldCommentCard.cloneCommentCard();
            newCommentCard.setAssignTeacherCount(oldCommentCard.getAssignTeacherCount());
            oldCommentCard.setAssignTeacherCount(CommentCardStatus.ASSIGN_TEACHER_TRIPLE.getCode());
            oldCommentCard.setStudentReadFlag(CommentCardStatus.STUDENT_READ.getCode());
            oldCommentCard.setTeacherReadFlag(CommentCardStatus.TEACHER_UNREAD.getCode());
            oldCommentCard.setStatus(CommentCardStatus.OVERTIME.getCode());
            oldCommentCard.setUpdateTime(date);
            commentCardJpaRepository.save(oldCommentCard);
            newCommentCard.setTeacherId(toTeacherId);
            newCommentCard.setAssignTeacherTime(date);
            newCommentCard.setStatus(CommentCardStatus.ASSIGNED_TEACHER.getCode());
            commentCardJpaRepository.save(newCommentCard);
        }
    }

    /**
     * 外教点评过期通知
     */
    @Override
    public void notifyExpireCommentCards() {
        // 会员有效期倒数第三天时,如果用户还有会员外教点评未使用,下午17:00点推送提醒: 会员要到期啦,外教点评还没用完呢,赶快去行使会员特权~
        LocalDate now = LocalDate.now();
        Set<Long> studentIds = serviceJpaRepository.getAvailableForeignCommentService(
                ProductType.COMMENT.value(),
                DateUtil.convertToDate(now.plusDays(3)),
                DateUtil.convertToDate(now.plusDays(4)),
                UserTypeEnum.GENERAL_MEMBER.type());
        logger.info("push expire commentCards to {}", studentIds);
        if(CollectionUtils.isNotEmpty(studentIds)) {
            pushExpireMessage(studentIds);
        }
    }

    // 推送
    private void pushExpireMessage(Set<Long> ids) {
        commentCardSDK.notifyCommentCardExpire(ids, commentCardTimeConf.getExpireMessage(), "MEMBER_FOREIGN_COMMENT");
    }

    private String getTeacherName(String teacherFirstName,String teacherLastName){
        logger.info("@getTeacherName --->teacherFirstName:" + teacherFirstName + " teacherLastName:"+teacherLastName);
            return (teacherFirstName == null ? "" : teacherFirstName.trim())
                    + " "+ (teacherLastName == null ? "" :teacherLastName.trim());
    }
}
