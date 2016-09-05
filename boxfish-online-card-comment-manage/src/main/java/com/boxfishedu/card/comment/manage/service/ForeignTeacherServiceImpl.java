package com.boxfishedu.card.comment.manage.service;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.card.comment.manage.entity.form.FromTeacherStudentForm;
import com.boxfishedu.card.comment.manage.entity.form.TeacherForm;
import com.boxfishedu.card.comment.manage.entity.jpa.EntityQuery;
import com.boxfishedu.card.comment.manage.service.sdk.CommentCardManageSDK;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
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

    @Override
    public void freezeTeacherId(Long teacherId) {
        commentCardManageSDK.freezeTeacherId(teacherId);
    }

    @Override
    public void unfreezeTeacherId(Long teacherId) {
        commentCardManageSDK.unfreezeTeacherId(teacherId);
    }

    @Override
    public JsonResultModel getTeacherOperations(Long teacherId){
        return commentCardManageSDK.getTeacherOperations(teacherId);
    }

    @Override
    public JsonResultModel getTeacherTimes(Long teacherId) {
        return commentCardManageSDK.getTeacherTimes(teacherId);
    }

    @Override
    public JsonResultModel getTeacherList(Pageable pageable,TeacherForm teacherForm) {
        EntityQuery entityQuery = new EntityQuery(entityManager,pageable) {
            @Override
            public Predicate[] predicates() {
                List<Predicate> predicateList = new ArrayList<>();
                if (Objects.nonNull(teacherForm.getTeacherId())){
                    predicateList.add(criteriaBuilder.equal(root.get("teacherId"),teacherForm.getTeacherId()));
                }
                if (StringUtils.isNotEmpty(teacherForm.getTeacherName()))
                return new Predicate[0];
            }
        };
        return null;
    }
}
