package com.boxfishedu.card.comment.manage.service;

import com.boxfishedu.card.comment.manage.entity.dto.CommentCardDto;
import com.boxfishedu.card.comment.manage.entity.enums.CommentCardStatus;
import com.boxfishedu.card.comment.manage.entity.enums.NotAnswerTime;
import com.boxfishedu.card.comment.manage.entity.form.CommentCardForm;
import com.boxfishedu.card.comment.manage.entity.form.CommentCardFormStatus;
import com.boxfishedu.card.comment.manage.entity.jpa.CommentCardJpaRepository;
import com.boxfishedu.card.comment.manage.entity.jpa.EntityQuery;
import com.boxfishedu.card.comment.manage.entity.mysql.CommentCard;
import com.boxfishedu.card.comment.manage.service.sdk.CommentCardManageSDK;
import com.boxfishedu.card.comment.manage.util.DateUtils;
import org.apache.commons.lang3.StringUtils;
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

                // 老师Id
                if(Objects.nonNull(commentCardForm.getTeacherId())) {
                    predicateList.add(criteriaBuilder.equal(root.get("teacherId"), commentCardForm.getTeacherId()));
                }

                // 老师姓名
                if(StringUtils.isNotBlank(commentCardForm.getTeacherName())) {
                    predicateList.add(criteriaBuilder.equal(root.get("teacherName"), commentCardForm.getTeacherName()));
                }

                // 学生Id
                if(Objects.nonNull(commentCardForm.getStudentId())) {
                    predicateList.add(criteriaBuilder.equal(root.get("studentId"), commentCardForm.getStudentId()));
                }

                // 学生姓名
                if(StringUtils.isNotBlank(commentCardForm.getStudentName())) {
                    predicateList.add(criteriaBuilder.equal(root.get("studentName"), commentCardForm.getStudentName()));
                }

                // 状态
                if(Objects.nonNull(commentCardForm.getStatus())) {
                    // 未点评
                    if(Objects.equals(commentCardForm.getStatus(), CommentCardFormStatus.NOTANSWER.value())) {
                        predicateList.add(criteriaBuilder.lt(
                                root.get("status"), CommentCardStatus.ANSWERED.getCode()));
                        // 未点评的时候,时间段才起作用
                        if(Objects.nonNull(commentCardForm.getNotAnswerTime())) {
                            NotAnswerTime.DateRange dateRange = commentCardForm.getNotAnswerTime().getRange();
                            predicateList.add(criteriaBuilder.between(
                                    root.get("studentAskTime"), dateRange.getFrom(), dateRange.getTo()));
                        }
                    }
                    // 已点评
                    else if(Objects.equals(commentCardForm.getStatus(), CommentCardFormStatus.ANSWERED.value())) {
                        predicateList.add(criteriaBuilder.equal(
                                root.get("status"), CommentCardStatus.ANSWERED.getCode()));
                    }
                    // 超时未点评
                    else if(Objects.equals(commentCardForm.getStatus(), CommentCardFormStatus.TIMEOUT.value())) {
                        predicateList.add(criteriaBuilder.lt(
                                root.get("status"), CommentCardStatus.ANSWERED.getCode()
                        ));
                        NotAnswerTime.DateRange dateRange = NotAnswerTime._24_48HOURS.getRange();
                        predicateList.add(criteriaBuilder.between(
                                root.get("studentAskTime"), dateRange.getFrom(),dateRange.getTo()
                        ));
                    }
                }

                // 订单类型
                if(StringUtils.isNotBlank(commentCardForm.getOrderChannel())) {

                }

                //.... 多条件
                return predicateList.toArray(new Predicate[predicateList.size()]);
            }
        };
        // 排序方式 entityQuery.getQuery().orderBy(pageable.getSort().iterator());
        entityQuery.orderBy(pageable.getSort().iterator());
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
        changeTeacher(commentCard, teacherId);
    }

    @Override
    @Transactional
    public void changeTeacherBatch(Long[] ids, Long teacherId) {
        for(Long id : ids) {
            changeTeacher(id, teacherId);
        }
    }

    @Override
    public void changeTeacher(CommentCard commentCard, Long teacherId) {
        Assert.notNull(commentCard, "点评不存在");
        CommentCard newCommentCard = commentCard.changeTeacher(teacherId);
        commentCardJpaRepository.save(newCommentCard);
        commentCardJpaRepository.save(commentCard);
        // 调用中外教运营老师减可分配次数
        // commentCardManageSDK.teacherDecrementCount(teacherId);
    }

    @Override
    public Integer[] findNoAnswerCountsByAskTime() {
        Integer[] result = new Integer[3];
        result[0] = findNoAnswerCountByAskTime(NotAnswerTime._24HOURS.getRange());
        result[1] = findNoAnswerCountByAskTime(NotAnswerTime._24_36HOURS.getRange());
        result[2] = findNoAnswerCountByAskTime(NotAnswerTime._36HOURS.getRange());
        return result;
    }

    private Integer findNoAnswerCountByAskTime(NotAnswerTime.DateRange dateRange) {
        return commentCardJpaRepository.findNoAnswerCountByAskTime(
                DateUtils.parseFromLocalDateTime(dateRange.getFrom()),
                DateUtils.parseFromLocalDateTime(dateRange.getTo()));
    }

}
