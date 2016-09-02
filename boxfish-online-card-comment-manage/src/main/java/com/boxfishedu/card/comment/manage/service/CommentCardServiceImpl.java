package com.boxfishedu.card.comment.manage.service;

import com.boxfishedu.card.comment.manage.entity.dto.CommentCardDto;
import com.boxfishedu.card.comment.manage.entity.form.CommentCardForm;
import com.boxfishedu.card.comment.manage.entity.jpa.EntityQuery;
import com.boxfishedu.card.comment.manage.entity.mysql.CommentCard;
import org.jdto.DTOBinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by LuoLiBing on 16/9/2.
 */
@Service
public class CommentCardServiceImpl implements CommentCardService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DTOBinder dtoBinder;

    /**
     * 多条件查询
     * @param commentCardForm
     * @param pageable
     * @return
     */
    @Override
    public Page<CommentCardDto> findCommentCardByOptions(CommentCardForm commentCardForm, Pageable pageable) {
        EntityQuery entityQuery = new EntityQuery<CommentCard>(entityManager, pageable) {
            @Override
            public Predicate[] predicates() {
                List<Predicate> predicateList = new ArrayList<>();
                // commentCardForm
                //.... 多条件
                return predicateList.toArray(new Predicate[predicateList.size()]);
            }
        };
        Page page = entityQuery.page();
        List<CommentCardDto> resultList = dtoBinder.bindFromBusinessObjectList(CommentCardDto.class, page.getContent());
        return new PageImpl<>(resultList, pageable, page.getTotalElements());
    }
}
