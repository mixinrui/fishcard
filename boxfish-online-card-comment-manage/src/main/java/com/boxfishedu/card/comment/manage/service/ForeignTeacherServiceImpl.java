package com.boxfishedu.card.comment.manage.service;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.card.comment.manage.entity.dto.CommentTeacherInfo;
import com.boxfishedu.card.comment.manage.entity.enums.CommentCardStatus;
import com.boxfishedu.card.comment.manage.entity.form.TeacherForm;
import com.boxfishedu.card.comment.manage.entity.jpa.CommentCardJpaRepository;
import com.boxfishedu.card.comment.manage.entity.jpa.EntityQuery;
import com.boxfishedu.card.comment.manage.service.sdk.CommentCardManageSDK;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
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

    private final static Logger logger = LoggerFactory.getLogger(ForeignTeacherServiceImpl.class);

    @Override
    public void freezeTeacherId(Long teacherId) {
        logger.info("@ForeignTeacherServiceImpl: freezing teacher's id in 'freezeTeacherId'......");
        commentCardManageSDK.freezeTeacherId(teacherId);
        commentCardJpaRepository.freezeTeacherId(teacherId);
    }

    @Override
    public void unfreezeTeacherId(Long teacherId) {
        logger.info("@ForeignTeacherServiceImpl: unfreezing teacher's id in 'unfreezeTeacherId'......");
        commentCardManageSDK.unfreezeTeacherId(teacherId);
        commentCardJpaRepository.unfreezeTeacherId(teacherId);
    }

    @Override
    public JsonResultModel getTeacherOperations(Long teacherId){
        logger.info("@ForeignTeacherServiceImpl: getting teacher's operations in 'getTeacherOperations'......");
        return commentCardManageSDK.getTeacherOperations(teacherId);
    }

    @Override
    public JsonResultModel getTeacherTimes(Long teacherId) {
        logger.info("@ForeignTeacherServiceImpl: getting teacher's times in 'getTeacherTimes'......");
        return commentCardManageSDK.getTeacherTimes(teacherId);
    }

    @Override
    public JsonResultModel getTeacherList(Pageable pageable,TeacherForm teacherForm) {
        logger.info("@ForeignTeacherServiceImpl: getting teacher's list in 'getTeacherList'......");
        EntityQuery entityQuery = new EntityQuery(entityManager,pageable) {
            @Override
            public Predicate[] predicates() {
                List<Predicate> predicateList = new ArrayList<>();
                if (Objects.nonNull(teacherForm.getTeacherId())){
                    predicateList.add(criteriaBuilder.equal(root.get("teacherId"),teacherForm.getTeacherId()));
                }
                if (StringUtils.isNotEmpty(teacherForm.getTeacherName())){
                    predicateList.add(criteriaBuilder.equal(root.get("teacherName"),teacherForm.getTeacherName()));
                }
                predicateList.add(criteriaBuilder.equal(root.get("teacherStatus"), CommentCardStatus.TEACHER_NORMAL.getCode()));
                return predicateList.toArray(new Predicate[predicateList.size()]);
            }
        };

        return null;
    }


    /**
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
    public JsonResultModel commentTeacherPage(Pageable pageable, TeacherForm teacherForm) {
        StringBuilder builder = new StringBuilder(200);
        builder.append("select c.teacher_id teacherId,c.teacher_name teacherName,count(teacher_id) commentCount,")/*收到点评总数*/
                .append("sum(case when c.status=400 or c.status=600 then 1 else 0 end) finishCount,")/*已完成*/
                .append("sum(case when c.status=300 then 1 else 0 end) unfinishCount,")/*未回答*/
                .append("sum(case when c.status=500 then 1 else 0 end) timeoutCount")/*超时未回答*/
                .append("from comment_card c ")
                .append("where c.teacher_id is not null ");
        // 老师Id不为空
        if(Objects.nonNull(teacherForm.getTeacherId())) {
            builder.append("and c.teacher_id=? ");
        } else if(Objects.nonNull(teacherForm.getTeacherName())) {
            builder.append("and c.teacher_name like '?' ");
        }
        // 状态不为空
        if(Objects.nonNull(teacherForm.getTeacherStatus())) {

        }
        builder.append(" limit ");
        Query nativeQuery = entityManager.createNativeQuery(builder.toString(), CommentTeacherInfo.class);
        int size = nativeQuery.getMaxResults();
        nativeQuery.getResultList();
        return null;
    }

    @Override
    public JsonResultModel getUncommentTeacherList(Pageable pageable, TeacherForm teacherForm) {
        logger.info("@ForeignTeacherServiceImpl: getting uncomment-teacher's list in 'getTeacherList'......");
        return getUncommentTeacherList(pageable,teacherForm);
    }
}
