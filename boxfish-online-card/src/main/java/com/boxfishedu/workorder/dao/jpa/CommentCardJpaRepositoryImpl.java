package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.common.bean.CommentCardStatus;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;
import java.util.Date;
import java.util.List;

/**
 * Created by ansel on 16/7/19.
 */
public class CommentCardJpaRepositoryImpl implements CommentCardJpaRepositoryCustom{
    @Autowired
    EntityManager entityManager;

    @Override
    public Page<CommentCard> queryCommentCardList(Pageable pageable,Long studentId) {
        EntityQuery entityQuery = new EntityQuery<CommentCard>(entityManager,pageable) {

            @Override
            public Predicate[] predicates() {
                List<Predicate> predicateList = Lists.newArrayList();
                predicateList.add(criteriaBuilder.equal(root.get("studentId"),studentId));
                predicateList.add(criteriaBuilder.or(
                        criteriaBuilder.or(criteriaBuilder.between(root.get("status"),CommentCardStatus.ASKED.getCode(),CommentCardStatus.ANSWERED.getCode()),
                        criteriaBuilder.equal(root.get("status"),CommentCardStatus.STUDENT_COMMENT_TO_TEACHER.getCode())),
                        criteriaBuilder.and(criteriaBuilder.equal(root.get("status"),CommentCardStatus.OVERTIME.getCode()),criteriaBuilder.equal(
                        root.get("assignTeacherCount"),CommentCardStatus.ASSIGN_TEACHER_TWICE.getCode()
                ))));
                return predicateList.toArray(new Predicate[predicateList.size()]);
            }
        };
        return entityQuery.page();
    }

    @Override
    public Page<CommentCard> queryTeacherAnsweredList(Pageable pageable,Long teacherId) {
        EntityQuery entityQuery = new EntityQuery<CommentCard>(entityManager,pageable) {
            @Override
            public Predicate[] predicates() {
                List<Predicate> predicateList = Lists.newArrayList();
                predicateList.add(criteriaBuilder.equal(root.get("teacherId"),teacherId));
                predicateList.add(criteriaBuilder.or(criteriaBuilder.equal(root.get("status"),CommentCardStatus.ANSWERED.getCode()),
                        criteriaBuilder.equal(root.get("status"),CommentCardStatus.STUDENT_COMMENT_TO_TEACHER.getCode()),
                        criteriaBuilder.equal(root.get("status"),CommentCardStatus.OVERTIME.getCode())));
                return predicateList.toArray(new Predicate[predicateList.size()]);
            }
        };
        return entityQuery.page();
    }

    @Override
    public Page<CommentCard> queryTeacherUnAnsweredList(Pageable pageable,Long teacherId) {
        EntityQuery entityQuery = new EntityQuery<CommentCard>(entityManager,pageable) {
            @Override
            public Predicate[] predicates() {
                List<Predicate> predicateList = Lists.newArrayList();
                predicateList.add(criteriaBuilder.equal(root.get("teacherId"),teacherId));
                predicateList.add(criteriaBuilder.equal(root.get("status"),CommentCardStatus.ASSIGNED_TEACHER.getCode()));
                return predicateList.toArray(new Predicate[predicateList.size()]);
            }
        };
        return entityQuery.page();
    }
}
