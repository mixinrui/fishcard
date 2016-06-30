package com.boxfishedu.workorder.dao.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by ansel on 16/5/18.
 */
@SuppressWarnings("ALL")
public abstract class EntityQuery<T> {

    protected CriteriaBuilder criteriaBuilder;
    protected CriteriaQuery criteriaQuery;
    protected Root<T> root;
    protected Predicate[] whereClause;
    protected EntityManager entityManager;
    protected Class<T> domainClass;
    protected Pageable pageable;

    public EntityQuery(EntityManager entityManager){
        this.entityManager = entityManager;
        this.domainClass = getGenericClassType();
        this.criteriaBuilder=entityManager.getCriteriaBuilder();
        criteriaQuery = criteriaBuilder.createQuery();
        root = criteriaQuery.from(domainClass);
        whereClause = predicates();
    }

    public EntityQuery(EntityManager entityManager, Pageable pageable){
        this(entityManager);
        this.pageable = pageable;
    }

    private Class<T> getGenericClassType(){
        Type mySuperClass = this.getClass().getGenericSuperclass();
        Type tType = ((ParameterizedType)mySuperClass).getActualTypeArguments()[0];
        return (Class<T> )tType;
    }

    public abstract Predicate[] predicates();

    public CriteriaQuery<T> getQuery(){
        criteriaQuery.select(root);
        criteriaQuery.where(whereClause);
        return criteriaQuery;
    }

    public List<T> list(){
        TypedQuery<T> tTypedQuery = this.entityManager.createQuery(this.getQuery());
        return tTypedQuery.getResultList();
    }

    public Page<T> page(){
        sort();
        return new PageImpl<>(getPageList(), pageable, count());
    }

    public Long count(){
        TypedQuery<Long> typedQuery = this.entityManager.createQuery(this.getQueryForCount());
        return typedQuery.getSingleResult();
    }

    private void sort(){
        if (pageable.getSort() != null){
            Sort sort  = pageable.getSort();
            for (Sort.Order order:sort){
                String property = order.getProperty();
                if (order.isAscending()){
                    criteriaQuery.orderBy(criteriaBuilder.asc(root.get(property)));
                } else {
                    criteriaQuery.orderBy(criteriaBuilder.desc(root.get(property)));
                }
            }
        }
    }

    private List<T> getPageList(){
        return this.entityManager
                .createQuery(this.getQuery())
                .setFirstResult(pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }


    private CriteriaQuery<Long> getQueryForCount(){

        criteriaQuery.select(criteriaBuilder.count(root));
        criteriaQuery.where(whereClause);
        return criteriaQuery;
    }
}
