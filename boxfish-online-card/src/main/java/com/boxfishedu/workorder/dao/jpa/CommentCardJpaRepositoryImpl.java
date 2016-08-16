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
                predicateList.add(criteriaBuilder.not(criteriaBuilder.and(criteriaBuilder.equal(root.get("assignTeacherCount"),
                        CommentCardStatus.ASSIGN_TEACHER_ONCE.getCode()),
                        criteriaBuilder.equal(root.get("status"),CommentCardStatus.OVERTIME.getCode()))));
                return predicateList.toArray(new Predicate[predicateList.size()]);
            }
        };
        return entityQuery.page();
    }

    @Override
    public List<CommentCard> queryCommentNoAnswerList() {
        EntityQuery entityQuery = new EntityQuery<CommentCard>(entityManager){

            @Override
            public Predicate[] predicates() {
                List<Predicate> predicateList = Lists.newArrayList();
                Date dateNow = new Date();
                Date dateNow2 = new Date();
//                Long timeLong = dateNow.getTime() - 24 * 60 * 60 * 1000L;
                Long timeLong = dateNow.getTime() - 5 * 60 * 1000L;  //测试用5分钟
                dateNow.setTime(timeLong);
//                timeLong = dateNow2.getTime() - 48 * 60 * 60 * 1000L;
                timeLong = dateNow2.getTime() - 10 * 60 * 1000L;//测试用10分钟
                dateNow2.setTime(timeLong);

                // 超过24小时,没有超过48小时未回答
//                predicateList.add(criteriaBuilder.lessThan(root.get("studentAskTime"),dateNow));
//                predicateList.add(criteriaBuilder.greaterThan(root.get("studentAskTime"),dateNow2));
                predicateList.add(criteriaBuilder.between(root.get("status"),CommentCardStatus.ASKED.getCode(),CommentCardStatus.ASSIGNED_TEACHER.getCode()));
                predicateList.add(criteriaBuilder.equal(root.get("assignTeacherCount"),CommentCardStatus.ASSIGN_TEACHER_ONCE.getCode()));
                return predicateList.toArray(new Predicate[predicateList.size()]);
            }
        };
        return entityQuery.list();
    }

    @Override
    public List<CommentCard> queryCommentNoAnswerList2() {
        EntityQuery entityQuery = new EntityQuery<CommentCard>(entityManager){

            @Override
            public Predicate[] predicates() {
                List<Predicate> predicateList = Lists.newArrayList();
                Date dateNow = new Date();
                Date dateNow2 = new Date();
//                Long timeLong = dateNow.getTime() - 48 * 60 * 60 * 1000L;
                Long timeLong = dateNow.getTime() - 10 * 60 * 1000L;  //测试用10分钟
                dateNow.setTime(timeLong);
//                timeLong = dateNow.getTime() - 72 * 60 * 60 * 1000L;
                timeLong = dateNow.getTime() - 11 * 60 * 1000L;
                dateNow2.setTime(timeLong);
                predicateList.add(criteriaBuilder.lessThan(root.get("studentAskTime"),dateNow));
                predicateList.add(criteriaBuilder.greaterThan(root.get("studentAskTime"),dateNow2));
                predicateList.add(criteriaBuilder.between(root.get("status"),CommentCardStatus.ASKED.getCode(),CommentCardStatus.ASSIGNED_TEACHER.getCode()));
                predicateList.add(criteriaBuilder.equal(root.get("assignTeacherCount"),CommentCardStatus.ASSIGN_TEACHER_TWICE.getCode()));
                return predicateList.toArray(new Predicate[predicateList.size()]);
            }
        };
        return entityQuery.list();
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
