package com.boxfishedu.card.comment.manage.entity.jpa;

import com.google.common.collect.Lists;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by LuoLiBing on 16/5/14.
 */
public abstract class EntityQuery<T> {

    protected CriteriaBuilder criteriaBuilder;
    protected CriteriaQuery query;
    protected Root<T> root;
    protected Predicate[] whereClause;
    protected EntityManager entityManager;
    protected Class<T> domainClass;
    protected Pageable pageable;

    public EntityQuery(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.domainClass = getGenericClassType();
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        query = criteriaBuilder.createQuery();
        root = query.from(domainClass);
        whereClause = predicates();
    }

    public EntityQuery(EntityManager entityManager, Pageable pageable) {
        this(entityManager);
        this.pageable = pageable;
    }

    private Class<T> getGenericClassType() {
        Type mySuperclass = this.getClass().getGenericSuperclass();
        Type tType = ((ParameterizedType)mySuperclass).getActualTypeArguments()[0];
        System.out.println(tType);
        return (Class<T>) tType;
    }

    public abstract Predicate[] predicates();

    public CriteriaQuery<T> getQuery() {
        query.select(root);
        query.where(whereClause);
        return query;
    }

    public List<T> list() {
        TypedQuery<T> q = this.entityManager.createQuery(this.getQuery());
        return q.getResultList();
    }

    public Page<T> page() {
        // 排序
        sort();
        return new PageImpl<>(getPageList(), pageable, count());
    }

    public Long count() {
        TypedQuery<Long> q = this.entityManager.createQuery(this.getQueryForCount());
        return q.getSingleResult();
    }

    public void orderBy(Iterator<Sort.Order> orderIterator) {
        query.orderBy(Lists.newArrayList(orderIterator));
    }

    private void sort() {
        if(pageable.getSort() != null) {
            Sort sort = pageable.getSort();
            List<Order> result = new ArrayList<>();
            for (Sort.Order order : sort) {

                String property = order.getProperty();
                if (order.isAscending()) {
                    result.add(criteriaBuilder.asc(root.get(property)));
                } else {
                    result.add(criteriaBuilder.desc(root.get(property)));
                }
            }
            orderBy(result);
        }
    }

    public void orderBy(List<Order> orders) {
        query.orderBy(orders);
    }

    private List<T> getPageList() {
        return this.entityManager
                .createQuery(this.getQuery())
                .setFirstResult(pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    private CriteriaQuery<Long> getQueryForCount() {
        query.select(criteriaBuilder.count(root));
        query.where(whereClause);
        return query;
    }

}
