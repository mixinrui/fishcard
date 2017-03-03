package com.boxfishedu.card.comment.manage.service;

import com.alibaba.fastjson.JSONArray;
import com.boxfishedu.card.comment.manage.entity.dto.CommentCardDto;
import com.boxfishedu.card.comment.manage.entity.dto.CommentCardExcelDto;
import com.boxfishedu.card.comment.manage.entity.dto.CommentCardLogDto;
import com.boxfishedu.card.comment.manage.entity.dto.TeacherInfo;
import com.boxfishedu.card.comment.manage.entity.enums.CommentCardFormStatus;
import com.boxfishedu.card.comment.manage.entity.enums.CommentCardStatus;
import com.boxfishedu.card.comment.manage.entity.enums.NotAnswerTime;
import com.boxfishedu.card.comment.manage.entity.form.ChangeTeacherForm;
import com.boxfishedu.card.comment.manage.entity.form.CommentCardForm;
import com.boxfishedu.card.comment.manage.entity.jpa.CommentCardJdbc;
import com.boxfishedu.card.comment.manage.entity.jpa.CommentCardJpaRepository;
import com.boxfishedu.card.comment.manage.entity.jpa.EntityQuery;
import com.boxfishedu.card.comment.manage.entity.mysql.CommentCard;
import com.boxfishedu.card.comment.manage.exception.BoxfishAsserts;
import com.boxfishedu.card.comment.manage.exception.BusinessException;
import com.boxfishedu.card.comment.manage.service.sdk.CommentCardManageSDK;
import com.boxfishedu.card.comment.manage.util.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdto.DTOBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by LuoLiBing on 16/9/2.
 */
@Service
public class CommentCardServiceImpl implements CommentCardService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CommentCardJpaRepository commentCardJpaRepository;

    @Autowired
    private DTOBinder dtoBinder;

    @Autowired
    private CommentCardManageSDK commentCardManageSDK;

    @Autowired
    private CommentCardJdbc commentCardJdbc;

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
        Page page = entityQuery.page();
        List<CommentCardDto> resultList = dtoBinder.bindFromBusinessObjectList(CommentCardDto.class, page.getContent());
        return new PageImpl<>(resultList, pageable, page.getTotalElements());
    }

    @Override
    public List findCommentCardByOptions(CommentCardForm commentCardForm, int page, int size) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuffer sb = new StringBuffer("SELECT * FROM COMMENT_CARD WHERE 1=1");
        //点评卡id
        if (Objects.nonNull(commentCardForm.getCode())){
            sb.append(" AND (ID = ");
            sb.append(commentCardForm.getCode());
            sb.append(" OR PREVIOUS_ID = ");
            sb.append(commentCardForm.getCode());
            sb.append(")");
        }

        // 老师Id
        if(Objects.nonNull(commentCardForm.getTeacherId())) {
            sb.append(" AND TEACHER_ID = ");
            sb.append(commentCardForm.getTeacherId());
        }

        // 老师姓名
        if(StringUtils.isNotBlank(commentCardForm.getTeacherName())) {
            sb.append(" AND TEACHER_NAME = '");
            sb.append(commentCardForm.getTeacherName());
            sb.append("'");
        }

        // 学生Id
        if(Objects.nonNull(commentCardForm.getStudentId())) {
            sb.append(" AND STUDENT_ID = ");
            sb.append(commentCardForm.getStudentId());
        }

        // 学生姓名
        if(StringUtils.isNotBlank(commentCardForm.getStudentName())) {
            sb.append(" AND STUDENT_NAME = '");
            sb.append(commentCardForm.getStudentName());
            sb.append("'");
        }

        // 课程类型
        if(StringUtils.isNotBlank(commentCardForm.getCourseType())) {
            sb.append(" AND COURSE_TYPE = '");
            sb.append(commentCardForm.getCourseType());
            sb.append("'");
        }
        // 课程难度
        if(StringUtils.isNotBlank(commentCardForm.getCourseDifficulty())) {
            sb.append(" AND COURSE_DIFFICULTY = '");
            sb.append(commentCardForm.getCourseDifficulty());
            sb.append("'");
        }
        // 状态
        if(Objects.nonNull(commentCardForm.getStatus())) {
            // 未点评
            if(Objects.equals(commentCardForm.getStatus(), CommentCardFormStatus.NOTANSWER.value())) {
                sb.append(" AND STATUS < ");
                sb.append(CommentCardStatus.ANSWERED.getCode());
            }
            // 已点评
            else if(Objects.equals(commentCardForm.getStatus(), CommentCardFormStatus.ANSWERED.value())) {
                sb.append(" AND (STATUS = ");
                sb.append(CommentCardStatus.ANSWERED.getCode());
                sb.append(" OR STATUS = ");
                sb.append(CommentCardStatus.STUDENT_COMMENT_TO_TEACHER.getCode());
                sb.append(")");
            }
            // 超时未点评
            else if(Objects.equals(commentCardForm.getStatus(), CommentCardFormStatus.TIMEOUT.value())) {
                sb.append(" AND (STATUS < ");
                sb.append(CommentCardStatus.ANSWERED.getCode());
                NotAnswerTime.DateRange dateRange = NotAnswerTime._24_48HOURS.getRange();
                sb.append(" AND STUDENT_ASK_TIME >= '");
                sb.append(simpleDateFormat.format(DateUtils.parseFromLocalDateTime(dateRange.getFrom())));
                sb.append("'");
                sb.append(" AND STUDENT_ASK_TIME <= '");
                sb.append(simpleDateFormat.format(DateUtils.parseFromLocalDateTime(dateRange.getTo())));
                sb.append("')");
            }
        }


        // 时间区间
        if(notAnswerDateTimeRangeOption(commentCardForm)) {
            if(Objects.isNull(commentCardForm.getStatus())) {
                sb.append(" AND (STATUS < ");
                sb.append(CommentCardStatus.ANSWERED.getCode());
            }
            NotAnswerTime.DateRange dateRange = NotAnswerTime.resolve(commentCardForm.getNotAnswerTime()).getRange();
            if(Objects.equals(commentCardForm.getNotAnswerTime(), NotAnswerTime._36HOURS.code())) {
                sb.append(" AND STUDENT_ASK_TIME <= '");
                sb.append(simpleDateFormat.format(DateUtils.parseFromLocalDateTime(dateRange.getTo())));
                sb.append("')");
            } else {
                sb.append(" AND STUDENT_ASK_TIME >= '");
                sb.append(simpleDateFormat.format(DateUtils.parseFromLocalDateTime(dateRange.getFrom())));
                sb.append("'");
                sb.append(" AND STUDENT_ASK_TIME <= '");
                sb.append(simpleDateFormat.format(DateUtils.parseFromLocalDateTime(dateRange.getTo())));
                sb.append("')");
            }
        }

        // 订单类型
        if(StringUtils.isNotBlank(commentCardForm.getOrderChannel())) {

        }

        // 订单编号
        if(StringUtils.isNotBlank(commentCardForm.getOrderCode())) {
            sb.append(" AND ORDER_CODE = '");
            sb.append(commentCardForm.getOrderCode());
            sb.append("'");
        }

        // 点评创建开始时间
        if(Objects.nonNull(commentCardForm.getStudentAskTimeRangeFrom())) {
            sb.append(" AND STUDENT_ASK_TIME >= '");
            sb.append(simpleDateFormat.format(commentCardForm.getStudentAskTimeRangeFrom()));
            sb.append("'");
        }

        // 点评创建结束时间
        if(Objects.nonNull(commentCardForm.getStudentAskTimeRangeTo())) {
            sb.append(" AND STUDENT_ASK_TIME <= '");
            sb.append(simpleDateFormat.format(commentCardForm.getStudentAskTimeRangeTo()));
            sb.append("'");
        }

        // 超时过期作废的点评卡排除
        sb.append(" AND STATUS != ");
        sb.append(CommentCardStatus.OVERTIME.getCode());
        logger.info("export comment_card excel : sql [{}]",sb.toString());
        return commentCardJdbc.getCommentCardByOptions(sb.toString());
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

        //点评卡id
        if (Objects.nonNull(commentCardForm.getCode())){
            predicateList.add(criteriaBuilder.or(criteriaBuilder.equal(root.get("id"), commentCardForm.getCode()),criteriaBuilder.equal(root.get("previous_id"), commentCardForm.getCode())));
        }

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
        if(Objects.nonNull(commentCardForm.getStudentAskTimeRangeFrom())) {
            predicateList.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("studentAskTime"), commentCardForm.getStudentAskTimeRangeFrom()
            ));
        }

        // 点评创建结束时间
        if(Objects.nonNull(commentCardForm.getStudentAskTimeRangeTo())) {
            predicateList.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("studentAskTime"), commentCardForm.getStudentAskTimeRangeTo()
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
        System.out.println("page.getContent"+ page.getContent().toString());
        return new CommentCardExcelDto(page.getContent());
    }

    /**
     * 导出excel2
     */
    public CommentCardExcelDto exportExcel2(CommentCardForm commentCardForm, int page,  int size){
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String,Object>> list = findCommentCardByOptions(commentCardForm,page,size);
        List<CommentCard> commentCardList = new ArrayList<>();
        for(Map map:list){
            CommentCard commentCard = getCommentCard(map);
            commentCardList.add(commentCard);
        }
        if (list.size() == 0){
            throw new BusinessException();
        }
        List<CommentCardDto> resultList = dtoBinder.bindFromBusinessObjectList(CommentCardDto.class, commentCardList);
        return new CommentCardExcelDto(resultList);
    }

    private CommentCard getCommentCard(Map map){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        CommentCard commentCard = new CommentCard();
        commentCard.setId(Long.parseLong(map.get("id").toString()));
        commentCard.setPrevious_id(map.get("previous_id") == null?null:Long.parseLong(map.get("previous_id").toString()));
        commentCard.setStudentId(Long.parseLong(map.get("student_id").toString()));
        commentCard.setStudentName(map.get("student_name") == null?null:map.get("student_name").toString());
        commentCard.setStudentPicturePath(map.get("student_picture_path") == null?null:map.get("student_picture_path").toString());
        commentCard.setAskVoicePath(map.get("ask_voice_path") == null?null:map.get("ask_voice_path").toString());
        commentCard.setVoiceTime(map.get("voice_time") == null?null:Long.parseLong(map.get("voice_time").toString()));
        try {
            commentCard.setStudentAskTime(map.get("student_ask_time") == null?null:simpleDateFormat.parse(map.get("student_ask_time").toString()));
            commentCard.setAssignTeacherTime(map.get("assign_teacher_time") == null?null:simpleDateFormat.parse(map.get("assign_teacher_time").toString()));
            commentCard.setTeacherAnswerTime(map.get("teacher_answer_time") == null?null:simpleDateFormat.parse(map.get("teacher_answer_time").toString()));
            commentCard.setStudentCommentTeacherTime(map.get("student_comment_teacher_time") == null?null:simpleDateFormat.parse(map.get("student_comment_teacher_time").toString()));
            commentCard.setCreateTime(map.get("create_time") == null?null:simpleDateFormat.parse(map.get("create_time").toString()));
            commentCard.setUpdateTime(map.get("update_time") == null?null:simpleDateFormat.parse(map.get("update_time").toString()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        commentCard.setTeacherId(map.get("teacher_id")==null?null:Long.parseLong(map.get("teacher_id").toString()));
        commentCard.setTeacherFirstName(map.get("teacher_first_name") == null?null:map.get("teacher_first_name").toString());
        commentCard.setTeacherLastName(map.get("teacher_last_name") == null?null:map.get("teacher_last_name").toString());
        commentCard.setTeacherName(map.get("teacher_name") == null?null:map.get("teacher_name").toString());
        commentCard.setTeacherPicturePath(map.get("teacher_picture_path") == null?null:map.get("teacher_picture_path").toString());
        commentCard.setAskVoicePath(map.get("answer_video_path") == null?null:map.get("answer_video_path").toString());
        commentCard.setTeacherStatus(map.get("teacher_status") == null?null:Integer.parseInt(map.get("teacher_status").toString()));
        commentCard.setAssignTeacherCount(map.get("assign_teacher_count") == null?null:Integer.parseInt(map.get("assign_teacher_count").toString()));
        commentCard.setStatus(map.get("status") == null?null:Integer.parseInt(map.get("status").toString()));
        commentCard.setQuestionType(map.get("question_type") == null?null:Integer.parseInt(map.get("question_type").toString()));
        if (Objects.equals(map.get("student_read_flag"),0)){
            commentCard.setStudentReadFlag(0);
        }else {
            commentCard.setStudentReadFlag(1);
        }
        if (Objects.equals(map.get("teacher_read_flag"),0)){
            commentCard.setTeacherReadFlag(0);
        }else {
            commentCard.setStudentReadFlag(1);
        }
        commentCard.setAnswerVideoTime(map.get("answer_video_time")==null?null:Long.parseLong(map.get("answer_video_time").toString()));
        commentCard.setAnswerVideoSize(map.get("answer_video_size")==null?null:Long.parseLong(map.get("answer_video_size").toString()));
        commentCard.setOrderId(map.get("order_id")==null?null:Long.parseLong(map.get("order_id").toString()));
        commentCard.setServiceId(map.get("service_id")==null?null:Long.parseLong(map.get("service_id").toString()));
        commentCard.setCourseId(map.get("course_id") == null?null:map.get("course_id").toString());
        commentCard.setCourseName(map.get("course_name") == null?null:map.get("course_name").toString());
        commentCard.setCourseType(map.get("course_type") == null?null:map.get("course_type").toString());
        commentCard.setCourseDifficulty(map.get("course_difficulty") == null?null:map.get("course_difficulty").toString());
        commentCard.setQuestionName(map.get("question_name") == null?null:map.get("question_name").toString());
        commentCard.setCover(map.get("cover") == null?null:map.get("cover").toString());
        commentCard.setOrderCode(map.get("order_code") == null?null:map.get("order_code").toString());
        commentCard.setStudentCommentGoodTagCode(map.get("student_comment_good_tag_code") == null?null:map.get("student_comment_good_tag_code").toString());
        commentCard.setStudentCommentBadTagCode(map.get("student_comment_bad_tag_code") == null?null:map.get("student_comment_bad_tag_code").toString());
        commentCard.setQuestionCode(map.get("question_code") == null?null:map.get("question_code").toString());


        return commentCard;
    }
}
