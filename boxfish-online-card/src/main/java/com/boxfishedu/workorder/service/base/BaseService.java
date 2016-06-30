package com.boxfishedu.workorder.service.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.List;

/**
 * Created by hucl on 16/3/31.
 * 通用的原子层,完成对数据访问层的调用
 */
public class BaseService <T, JPA extends JpaRepository<T,ID>,ID extends Serializable>{
    @Autowired
    protected JPA jpa;

    public List<T> findAll(Sort sort){
        return jpa.findAll(sort);
    }

    public <S extends T> List<S> save(Iterable<S> entities){
        return jpa.save(entities);
    }

    public void flush(){
        jpa.flush();
    }

    public <S extends T> S saveAndFlush(S entity){
        return jpa.saveAndFlush(entity);
    }

    public void deleteInBatch(Iterable<T> entities){
        jpa.deleteInBatch(entities);
    }

    public void deleteAllInBatch(){
        jpa.deleteAllInBatch();
    }

    public T getOne(ID id){
        return jpa.getOne(id);
    }

    public Page<T> findAll(Pageable pageable){
        return jpa.findAll(pageable);
    }

    public <S extends T> S save(S entity){
        return jpa.save(entity);
    }

    public T findOne(ID id){
        return jpa.findOne(id);
    }

    public boolean exists(ID id){
        return jpa.exists(id);
    }

    public List<T> findAll(){
        return jpa.findAll();
    }

    public Iterable<T> findAll(Iterable<ID> ids){
        return jpa.findAll(ids);
    }

    public long count(){
        return jpa.count();
    }

    public void delete(ID id){
        jpa.delete(id);
    }

    public void delete(T entity){
        jpa.delete(entity);
    }

    public void delete(Iterable<? extends T> entities){
        jpa.delete(entities);
    }

    void deleteAll(){
        jpa.deleteAll();
    }
}
