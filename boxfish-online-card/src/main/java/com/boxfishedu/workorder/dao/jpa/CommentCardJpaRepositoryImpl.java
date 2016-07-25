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
                Long timeLong = dateNow.getTime() - 24 * 60 * 60 * 1000L;
                dateNow.setTime(timeLong);
                predicateList.add(criteriaBuilder.lessThan(root.get("studentAskTime"),dateNow));
                predicateList.add(criteriaBuilder.or(criteriaBuilder.between(root.get("status"),CommentCardStatus.ASKED.getCode(),CommentCardStatus.ASSIGNED_TEACHER.getCode()),
                        criteriaBuilder.equal(root.get("status"),CommentCardStatus.TEACHER_UNREADED.getCode()),criteriaBuilder.equal(root.get("status"),CommentCardStatus.TEACHER_READED.getCode())));
                predicateList.add(criteriaBuilder.equal(root.get("assignTeacherCount"),0));
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
                Long timeLong = dateNow.getTime() - 48 * 60 * 60 * 1000L;
                dateNow.setTime(timeLong);
                predicateList.add(criteriaBuilder.lessThan(root.get("studentAskTime"),dateNow));
                predicateList.add(criteriaBuilder.or(criteriaBuilder.equal(root.get("status"),CommentCardStatus.OVERTIME_ONE.getCode()),criteriaBuilder.equal(root.get("status"),CommentCardStatus.TEACHER_UNREADED.getCode())));
                predicateList.add(criteriaBuilder.equal(root.get("assignTeacherCount"),1));
                return predicateList.toArray(new Predicate[predicateList.size()]);
            }
        };
        return entityQuery.list();
    }
}
