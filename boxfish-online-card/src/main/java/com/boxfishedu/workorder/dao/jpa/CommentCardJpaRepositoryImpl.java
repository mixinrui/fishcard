package com.boxfishedu.workorder.dao.jpa;

import com.boxfishedu.workorder.entity.mysql.CommentCard;
import com.boxfishedu.workorder.common.bean.CommentCardStatus;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;
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
                //predicateList.add(criteriaBuilder.greaterThan(root.get()))
                predicateList.add(criteriaBuilder.between(root.get("status"),CommentCardStatus.getCode("已提问"),CommentCardStatus.getCode("已分配教师")));
                return predicateList.toArray(new Predicate[predicateList.size()]);
            }
        };

        return entityQuery.list();
    }
}
