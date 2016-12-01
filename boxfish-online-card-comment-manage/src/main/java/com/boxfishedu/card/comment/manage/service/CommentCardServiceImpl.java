package com.boxfishedu.card.comment.manage.service;

import com.boxfishedu.card.comment.manage.entity.dto.*;
import com.boxfishedu.card.comment.manage.entity.enums.CommentCardFormStatus;
import com.boxfishedu.card.comment.manage.entity.enums.CommentCardStatus;
import com.boxfishedu.card.comment.manage.entity.enums.NotAnswerTime;
import com.boxfishedu.card.comment.manage.entity.form.ChangeTeacherForm;
import com.boxfishedu.card.comment.manage.entity.form.CommentCardForm;
import com.boxfishedu.card.comment.manage.entity.jpa.CommentCardJpaRepository;
import com.boxfishedu.card.comment.manage.entity.jpa.EntityQuery;
import com.boxfishedu.card.comment.manage.entity.mysql.CommentCard;
import com.boxfishedu.card.comment.manage.exception.BoxfishAsserts;
import com.boxfishedu.card.comment.manage.exception.BusinessException;
import com.boxfishedu.card.comment.manage.service.sdk.CommentCardManageSDK;
import com.boxfishedu.card.comment.manage.util.DateUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdto.DTOBinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
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
                return createPredicates(root, criteriaBuilder, commentCardForm);
            }
        };
        // 排序方式
        if(Objects.nonNull(pageable.getSort())) {
            entityQuery.orderBy(pageable.getSort().iterator());
        }
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

    /**
     * 批量更换老师
     * @param ids
     * @param teacherId
     */
    @Override
    @Transactional
    public void changeTeacherBatch(Long[] ids, Long teacherId) {
        // 验证老师次数
        TeacherInfo teacherInfo = validateTeacher(teacherId, ids.length);
        List<CommentCard> commentCards = commentCardJpaRepository.findAll(Arrays.asList(ids));

        ChangeTeacherForm changeTeacherForm = new ChangeTeacherForm();
        for(CommentCard commentCard : commentCards) {
            CommentCard newCommentCard = commentCard.changeTeacher(teacherInfo);
            commentCardJpaRepository.save(newCommentCard);
            commentCardJpaRepository.save(commentCard);

            // 添加换的老师
            changeTeacherForm.addChangeTeacher(commentCard, newCommentCard);
        }
        // 调用中外教管理接口
        commentCardManageSDK.changeTeacherBatch(changeTeacherForm);
    }

    @Override
    @Transactional
    public void changeTeacher(CommentCard commentCard, Long teacherId) {
        BoxfishAsserts.notNull(commentCard, "点评不存在");
        // 对老师进行验证
        TeacherInfo teacherInfo = validateTeacher(teacherId, 1);

        CommentCard newCommentCard = commentCard.changeTeacher(teacherInfo);
        commentCardJpaRepository.save(newCommentCard);
        commentCardJpaRepository.save(commentCard);
        // 调用中外教运营老师减可分配次数
        ChangeTeacherForm changeTeacherForm = new ChangeTeacherForm();
        changeTeacherForm.addChangeTeacher(commentCard, newCommentCard);
        commentCardManageSDK.changeTeacherBatch(changeTeacherForm);
    }

    @Override
    public Integer[] findNoAnswerCountsByAskTime() {
        Integer[] result = new Integer[3];
        result[0] = findNoAnswerCountByAskTime(NotAnswerTime._24HOURS.getRange());
        result[1] = findNoAnswerCountByAskTime(NotAnswerTime._24_36HOURS.getRange());
        result[2] = commentCardJpaRepository.findNoAnswerCountByAskTime(
                DateUtils.parseFromLocalDateTime(NotAnswerTime._36HOURS.getRange().getTo()));
        return result;
    }

    /**
     * 外教点评日志
     * @param id
     * @return
     */
    @Override
    public CommentCardLogDto findCommentCardLog(Long id) {
        CommentCard firstCommentCard = commentCardJpaRepository.findOne(id);
        BoxfishAsserts.notNull(firstCommentCard,  id + "该点评卡不存在");
        List<CommentCard> commentCardList = commentCardJpaRepository.findByPrevious_id(id);
        if(CollectionUtils.isNotEmpty(commentCardList)) {
            return new CommentCardLogDto(firstCommentCard, commentCardList);
        }
        return new CommentCardLogDto(firstCommentCard);
    }

    @Override
    public ChangeTeacherForm changeCommentCardToInnerTeacher(Long teacherId) {
        ChangeTeacherForm changeTeacherForm = new ChangeTeacherForm();

        return changeTeacherForm;
    }

    private TeacherInfo validateTeacher(Long teacherId, int count) {
        TeacherInfo teacherInfo = commentCardManageSDK.getTeacherInfoById(teacherId);
        // 内部账号,不需要验证
        if(Objects.equals(teacherInfo.getTeacherType(), 1)) {
            return teacherInfo;
        }
        teacherInfo.validate(count);
        return teacherInfo;
    }

    private Integer findNoAnswerCountByAskTime(NotAnswerTime.DateRange dateRange) {
        return commentCardJpaRepository.findNoAnswerCountByAskTime(
                DateUtils.parseFromLocalDateTime(dateRange.getFrom()),
                DateUtils.parseFromLocalDateTime(dateRange.getTo()));
    }

    /**
     * 多条件查询
     * @param root
     * @param criteriaBuilder
     * @param commentCardForm
     * @return
     */
    private Predicate[] createPredicates(Root<CommentCard> root, CriteriaBuilder criteriaBuilder, CommentCardForm commentCardForm) {
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

        // 课程类型
        if(StringUtils.isNotBlank(commentCardForm.getCourseType())) {
            predicateList.add(criteriaBuilder.equal(root.get("courseType"), commentCardForm.getCourseType()));
        }
        // 课程难度
        if(StringUtils.isNotBlank(commentCardForm.getCourseDifficulty())) {
            predicateList.add(criteriaBuilder.equal(root.get("courseDifficulty"), commentCardForm.getCourseDifficulty()));
        }
        // 状态
        if(Objects.nonNull(commentCardForm.getStatus())) {
            // 未点评
            if(Objects.equals(commentCardForm.getStatus(), CommentCardFormStatus.NOTANSWER.value())) {
                predicateList.add(criteriaBuilder.lt(
                        root.get("status"), CommentCardStatus.ANSWERED.getCode()));
            }
            // 已点评
            else if(Objects.equals(commentCardForm.getStatus(), CommentCardFormStatus.ANSWERED.value())) {
                predicateList.add(
                    criteriaBuilder.or(
                        criteriaBuilder.equal(root.get("status"), CommentCardStatus.ANSWERED.getCode()),
                        criteriaBuilder.equal(root.get("status"), CommentCardStatus.STUDENT_COMMENT_TO_TEACHER.getCode())
                    )
                );
            }
            // 超时未点评
            else if(Objects.equals(commentCardForm.getStatus(), CommentCardFormStatus.TIMEOUT.value())) {
                predicateList.add(criteriaBuilder.lt(
                        root.get("status"), CommentCardStatus.ANSWERED.getCode()
                ));
                NotAnswerTime.DateRange dateRange = NotAnswerTime._24_48HOURS.getRange();
                predicateList.add(criteriaBuilder.between(
                        root.get("studentAskTime"),
                        DateUtils.parseFromLocalDateTime(dateRange.getFrom()),
                        DateUtils.parseFromLocalDateTime(dateRange.getTo())
                ));
            }
        }


        // 时间区间
        if(notAnswerDateTimeRangeOption(commentCardForm)) {
            if(Objects.isNull(commentCardForm.getStatus())) {
                predicateList.add(criteriaBuilder.lt(
                        root.get("status"), CommentCardStatus.ANSWERED.getCode()));
            }
            NotAnswerTime.DateRange dateRange = NotAnswerTime.resolve(commentCardForm.getNotAnswerTime()).getRange();
            if(Objects.equals(commentCardForm.getNotAnswerTime(), NotAnswerTime._36HOURS.code())) {
                predicateList.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("studentAskTime"),
                        DateUtils.parseFromLocalDateTime(dateRange.getTo())
                ));
            } else {
                predicateList.add(criteriaBuilder.between(
                        root.get("studentAskTime"),
                        DateUtils.parseFromLocalDateTime(dateRange.getFrom()),
                        DateUtils.parseFromLocalDateTime(dateRange.getTo())
                ));
            }
        }

        // 订单类型
        if(StringUtils.isNotBlank(commentCardForm.getOrderChannel())) {

        }

        // 订单编号
        if(StringUtils.isNotBlank(commentCardForm.getOrderCode())) {
            predicateList.add(criteriaBuilder.equal(
                    root.get("orderCode"), commentCardForm.getOrderCode()
            ));
        }

        // 点评创建开始时间
        if(Objects.nonNull(commentCardForm.getFrom())) {
            predicateList.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createTime"), commentCardForm.getFrom()
            ));
        }

        // 点评创建结束时间
        if(Objects.nonNull(commentCardForm.getTo())) {
            predicateList.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("createTime"), commentCardForm.getTo()
            ));
        }

        // 超时过期作废的点评卡排除
        predicateList.add(criteriaBuilder.notEqual(root.get("status"), CommentCardStatus.OVERTIME.getCode()));

        //.... 多条件
        return predicateList.toArray(new Predicate[predicateList.size()]);
    }


    private boolean notAnswerDateTimeRangeOption(CommentCardForm commentCardForm) {
        // 点评状态条件为全部或者是未点评时,选择未点评时间区间才起作用
        return (Objects.isNull(commentCardForm.getStatus())
                || Objects.equals(commentCardForm.getStatus(), CommentCardFormStatus.NOTANSWER.value()))
                && Objects.nonNull(commentCardForm.getNotAnswerTime())
                && Objects.nonNull(NotAnswerTime.resolve(commentCardForm.getNotAnswerTime()));
    }

    /**
     * 导出excel
     */
    public CommentCardExcelDto exportExcel(CommentCardForm commentCardForm, Pageable pageable){
        Page<CommentCardDto> page = findCommentCardByOptions(commentCardForm,pageable);
        if (page.getSize() == 0){
            throw new BusinessException();
        }
        return new CommentCardExcelDto(page.getContent());
    }

}
