package com.boxfishedu.card.comment.manage.service;

import com.boxfishedu.card.comment.manage.entity.dto.CommentCardDto;
import com.boxfishedu.card.comment.manage.entity.form.CommentCardForm;
import com.boxfishedu.card.comment.manage.entity.jpa.CommentCardJpaRepository;
import com.boxfishedu.card.comment.manage.entity.jpa.EntityQuery;
import com.boxfishedu.card.comment.manage.entity.mysql.CommentCard;
import com.boxfishedu.card.comment.manage.service.sdk.CommentCardManageSDK;
import com.boxfishedu.card.comment.manage.util.DateUtils;
import org.jdto.DTOBinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by LuoLiBing on 16/9/2.
 */
@Service
public class CommentCardServiceImpl implements CommentCardService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CommentCardJpaRepository commentCardJpaRepository;

    @Autowired
    private DTOBinder dtoBinder;

    @Autowired
    private CommentCardManageSDK commentCardManageSDK;

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
                if(Objects.nonNull(commentCardForm.getStudentAskTimeRange())) {
                    CommentCardForm.DateRange dateRange = commentCardForm.getStudentAskTimeRange();
                    predicateList.add(criteriaBuilder.between(
                            root.get("studentAskTime"),
                            DateUtils.parseFromDate(dateRange.getFrom()),
                            DateUtils.parseFromDate(dateRange.getTo())
                    ));
                }
                //.... 多条件
                return predicateList.toArray(new Predicate[predicateList.size()]);
            }
        };
        Page page = entityQuery.page();
        List<CommentCardDto> resultList = dtoBinder.bindFromBusinessObjectList(CommentCardDto.class, page.getContent());
        return new PageImpl<>(resultList, pageable, page.getTotalElements());
    }

    @Override
    public CommentCardDto findCommentCardById(Long id) {
        CommentCard commentCard = commentCardJpaRepository.findOne(id);
        return dtoBinder.bindFromBusinessObject(CommentCardDto.class, commentCard);
    }

    /**
     * 换老师逻辑
     * @param id
     * @param teacherId
     */
    @Override
    @Transactional
    public void changeTeacher(Long id, Long teacherId) {
        CommentCard commentCard = commentCardJpaRepository.findOne(id);
        Assert.notNull(commentCard, "点评不存在");
        CommentCard newCommentCard = commentCard.changeTeacher(teacherId);
        commentCardJpaRepository.save(newCommentCard);
        commentCardJpaRepository.save(commentCard);

        // 调用中外教运营老师减可分配次数
        // commentCardManageSDK.teacherDecrementCount(teacherId);
    }

}
