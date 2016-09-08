package com.boxfishedu.card.comment.manage.service;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.card.comment.manage.entity.dto.CommentCountSetLog;
import com.boxfishedu.card.comment.manage.entity.dto.CommentTeacherInfo;
import com.boxfishedu.card.comment.manage.entity.dto.NoCommentTeacherInfoDto;
import com.boxfishedu.card.comment.manage.entity.form.TeacherForm;
import com.boxfishedu.card.comment.manage.entity.jpa.CommentCardJpaRepository;
import com.boxfishedu.card.comment.manage.entity.mysql.CommentCard;
import com.boxfishedu.card.comment.manage.service.sdk.CommentCardManageSDK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by ansel on 16/9/2.
 */
@Service
public class ForeignTeacherServiceImpl implements ForeignTeacherService{
    @Autowired
    CommentCardManageSDK commentCardManageSDK;

    @Autowired
    EntityManager entityManager;

    @Autowired
    CommentCardJpaRepository commentCardJpaRepository;

    @Autowired
    private CommentCardService commentCardService;

    private final static Logger logger = LoggerFactory.getLogger(ForeignTeacherServiceImpl.class);

    /**
     * 冻结老师
     * @param teacherId
     */
    @Override
    public void freezeTeacherId(Long teacherId) {
        logger.info("@ForeignTeacherServiceImpl: freezing teacher's id in 'freezeTeacherId'......");
        // 将该老师未完成的点评,转移给内部账号
        List<CommentCard> commentCardList = commentCardJpaRepository.findNoAnswerCommentCardByTeacherId(teacherId);
        for(CommentCard commentCard : commentCardList) {
            commentCardService.changeTeacher(commentCard, getInnerTeacherId(commentCard));
        }
        // 调用中外教管理管理冻结老师账号
        commentCardManageSDK.freezeTeacherId(teacherId);
    }

    /**
     * 解冻老师
     * @param teacherId
     */
    @Override
    public void unfreezeTeacherId(Long teacherId) {
        logger.info("@ForeignTeacherServiceImpl: unfreezing teacher's id in 'unfreezeTeacherId'......");
        commentCardManageSDK.unfreezeTeacherId(teacherId);
    }

    @Override
    public JsonResultModel getTeacherOperations(Long teacherId){
        logger.info("@ForeignTeacherServiceImpl: getting teacher's operations in 'getTeacherOperations'......");
        return commentCardManageSDK.getTeacherOperations(teacherId);
    }


    /**
     * 查询老师对应点评统计
     *
     * select c.teacher_id,c.teacher_name,count(teacher_id) 收到点评总数,
     * sum(case when c.status=400 or c.status=600 then 1 else 0 end)已完成 c1,
     * sum(case when c.status=300 then 1 else 0 end)未回答 c2,
     * sum(case when c.status=500 then 1 else 0 end)超时未回答 c3
     * from comment_card c where c.teacher_id is not null
     * group by c.teacher_id
     * @param pageable
     * @param teacherForm
     * @return
     */
    @Override
    public Page<CommentTeacherInfo> commentTeacherPage(Pageable pageable, TeacherForm teacherForm) {
        // 获取查询sql
        StringBuilder querySb = commentTeacherSql();
        StringBuilder countSb = commentTeacherCountSql();
        Map<String, Object> parameters = new HashMap<>();
        // 设置查询条件
        setQueryOptions(querySb, countSb, teacherForm, parameters);
        // 分组条件
        querySb.append(" group by c.teacher_id");
        countSb.append(" group by c.teacher_id) t");
        // 分页
        querySb.append(" limit ")
                .append(pageable.getPageSize() * pageable.getPageNumber())
                .append(",")
                .append(pageable.getPageSize());
        // 查询参数设置
        Query nativeQuery = entityManager.createNativeQuery(querySb.toString(), "commentTeacherInfo");
        setParameter(nativeQuery, parameters);

        Query countQuery = entityManager.createNativeQuery(countSb.toString());
        setParameter(countQuery, parameters);
        BigInteger size = (BigInteger) countQuery.getSingleResult();
        return new PageImpl<>(nativeQuery.getResultList(), pageable, size.intValue());
    }

    /**
     * 未收到点评老师
     * @param pageable
     * @param teacherForm
     * @return
     */
    @Override
    public Page<NoCommentTeacherInfoDto> uncommentTeacherPage(Pageable pageable, TeacherForm teacherForm) {
        return commentCardManageSDK.getNoCommentPage(pageable, teacherForm);
    }

    /**
     * 老师点评次数设置日志
     * @param pageable
     * @param teacherId
     * @return
     */
    @Override
    public Page<CommentCountSetLog> commentCountSetLogPage(Pageable pageable, Long teacherId) {
        return commentCardManageSDK.getCommentCountSetLogPage(pageable, teacherId);
    }


    private StringBuilder commentTeacherCountSql() {
        StringBuilder builder = new StringBuilder(200);
        builder.append("select count(1) from (select c.teacher_id from comment_card c where c.teacher_id is not null ");
        return builder;
    }

    private StringBuilder commentTeacherSql() {
        StringBuilder builder = new StringBuilder(200);
        builder.append("select c.teacher_id teacherId,ifnull(c.teacher_name,'') teacherName,count(teacher_id) commentCount,")/*收到点评总数*/
                .append("sum(case when c.status=400 or c.status=600 then 1 else 0 end) finishCount,")/*已完成*/
                .append("sum(case when c.status=300 then 1 else 0 end) unfinishCount,")/*未回答*/
                .append("sum(case when c.status=500 then 1 else 0 end) timeoutCount ")/*超时未回答*/
                .append("from comment_card c ")
                .append("where c.teacher_id is not null ");
        return builder;
    }

    private void setQueryOptions(StringBuilder queryBuilder, StringBuilder countQueryBuilder,
                                 TeacherForm teacherForm, Map<String, Object> parameters) {
        // 老师Id不为空
        if(Objects.nonNull(teacherForm.getTeacherId())) {
            queryBuilder.append("and c.teacher_id=:teacherId ");
            countQueryBuilder.append("and c.teacher_id=:teacherId ");
            parameters.put("teacherId", teacherForm.getTeacherId());
        } else if(Objects.nonNull(teacherForm.getTeacherName())) {
            queryBuilder.append("and c.teacher_name like '?' ");
            countQueryBuilder.append("and c.teacher_name like '?' ");
            parameters.put("teacherName", teacherForm.getTeacherName());
        }
        // 状态不为空
        if(Objects.nonNull(teacherForm.getTeacherStatus())) {

        }
    }

    private void setParameter(Query query, Map<String, Object> parameters) {
        parameters.forEach(query::setParameter);
    }

    private Long getInnerTeacherId(CommentCard commentCard) {
        JsonResultModel jsonResultModel = commentCardManageSDK.getInnerTeacherId(getInnerTeacherParameterMap(commentCard));
        Map<String, String> data = (Map<String, String>) jsonResultModel.getData();
        return Long.valueOf(data.get("teacherId"));
    }

    private Map<String, Object> getInnerTeacherParameterMap(CommentCard commentCard) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("fishCardId",commentCard.getId());
        paramMap.put("studentId",commentCard.getStudentId());
        paramMap.put("courseId",commentCard.getCourseId());
        return paramMap;
    }
}
