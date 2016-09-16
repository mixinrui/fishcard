package com.boxfishedu.card.comment.manage.service.sdk;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.card.comment.manage.config.CommentCardManageUrl;
import com.boxfishedu.card.comment.manage.entity.dto.*;
import com.boxfishedu.card.comment.manage.entity.form.ChangeTeacherForm;
import com.boxfishedu.card.comment.manage.entity.form.TeacherForm;
import com.boxfishedu.card.comment.manage.entity.mysql.CommentCard;
import com.boxfishedu.card.comment.manage.util.JsonResultModuleUtils;
import com.boxfishedu.card.comment.manage.util.ObjectUtils;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ansel on 16/9/2.
 */
@Service
public class CommentCardManageSDK {

    @Autowired
    private RestTemplate restTemplate;

    private final static Logger logger = LoggerFactory.getLogger(CommentCardManageSDK.class);

    @Autowired
    private CommentCardManageUrl commentCardManageUrl;

    /**
     * 冻结老师
     * @param teacherId
     */
    public void freezeTeacherId(Long teacherId){
        logger.info("Accessing freezeTeacherId in CommentCardManageSDK......");
        Map<String, Object> requestBody = Maps.newHashMap();
        requestBody.put("teacherId", teacherId);
        requestBody.put("freezeFlag", TeacherInfo.FREEZE);
        restTemplate.postForObject(createUpdateFreezeTeacherURI(), requestBody, Object.class);
    }

    /**
     * 获取内部老师
     * @param commentCard
     * @return
     */
    public InnerTeacher getInnerTeacherId(CommentCard commentCard){
        JsonResultModel jsonResultModel = restTemplate.postForObject(
                getInnerTeacherURI(), getInnerTeacherParameterMap(commentCard), JsonResultModel.class);
        return jsonResultModel.getData(InnerTeacher.class);
    }

    /**
     * token验证
     * @param accessToken
     * @return
     */
    public boolean checkToken(String accessToken) {
        JsonResultModel jsonResultModel = restTemplate.getForObject(
                createTokenCheckURI(accessToken), JsonResultModel.class);
        return (StringUtils.equals("ok", jsonResultModel.getData().toString()));
    }

    /**
     * 老师解冻 0冻结 1解冻
     * @param  teacherId
     */
    public void unfreezeTeacherId(Long teacherId){
        logger.info("Accessing unfreezeTeacherId in CommentCardManageSDK......");
        Map<String, Object> requestBody = Maps.newHashMap();
        requestBody.put("teacherId", teacherId);
        requestBody.put("freezeFlag", TeacherInfo.UNFREEZE);
        restTemplate.postForObject(createUpdateFreezeTeacherURI(), requestBody, Object.class);
    }

    /**
     * 未点评老师
     * @param pageable
     * @param teacherForm
     * @return
     */
    public Page<NoCommentTeacherInfoDto> getNoCommentPage(Pageable pageable, TeacherForm teacherForm) {
        JsonResultModel jsonResultModel = restTemplate.getForObject(createNoCommentPageURI(
                pageable, teacherForm), JsonResultModel.class);
        return new PageImpl<>(
                JsonResultModuleUtils.getListFromPageResult(jsonResultModel, NoCommentTeacherInfoDto.class,
                        ((clazz, beanMap) -> ObjectUtils.convertObject(beanMap, clazz))),
                pageable, JsonResultModuleUtils.getTotalElements(jsonResultModel));
    }

    /**
     * 老师设置点评次数流水
     * @param pageable
     * @param teacherId
     * @return
     */
    public Page<CommentCountSetLog> getCommentCountSetLogPage(Pageable pageable, Long teacherId) {
        JsonResultModel jsonResultModel = restTemplate.postForObject(
                createCommentCountSetLogPageURI(pageable),
                Collections.singletonMap("teacherId", teacherId),
                JsonResultModel.class);
        return new PageImpl<>(
                JsonResultModuleUtils.getListFromPageResult(jsonResultModel, CommentCountSetLog.class,
                        ((clazz, beanMap) -> ObjectUtils.convertObject(beanMap, clazz))),
                pageable, JsonResultModuleUtils.getTotalElements(jsonResultModel));
    }

    public JsonResultModel getTeacherOperations(Long teacherId){
        logger.info("Accessing getTeacherOperations in CommentCardManageSDK......");
        return JsonResultModel.newJsonResultModel(restTemplate.exchange(createGetTeacherOperationURI(teacherId),HttpMethod.GET,
                HttpEntity.EMPTY,
                Object.class));
    }


    /**
     * 更换老师
     * @param changeTeacherForm
     */
    public void changeTeacherBatch(ChangeTeacherForm changeTeacherForm) {
        restTemplate.postForObject(createChangeTeacherURI(), changeTeacherForm, JsonResultModel.class);
    }

    /**
     * 获取单个用户
     * @param teacherId
     * @return
     */
    public TeacherInfo getTeacherInfoById(Long teacherId) {
        JsonResultModel jsonResultModel = restTemplate.getForObject(
                createGetTeacherInfoURI(teacherId), JsonResultModel.class);
        return jsonResultModel.getData(TeacherInfo.class);
    }

    /**
     * 获取老师列表
     * @param pageable
     * @param teacherForm
     * @return
     */
    public Page<TeacherInfo> getTeacherInfoPage(Pageable pageable, TeacherForm teacherForm) {
        JsonResultModel jsonResultModel = restTemplate.postForObject(
                createCanCommentTeacherPageURI(pageable), teacherForm, JsonResultModel.class);
        return new PageImpl<>(
                JsonResultModuleUtils.getListFromPageResult(jsonResultModel, TeacherInfo.class,
                        ((clazz, beanMap) -> ObjectUtils.convertObject(beanMap, clazz))),
                pageable, JsonResultModuleUtils.getTotalElements(jsonResultModel));
    }

    public Page<FreezeLogDto> getTeacherFreezeLongPage(Pageable pageable, Long teacherId) {
        JsonResultModel jsonResultModel = restTemplate.getForObject(
                createGetTeacherFreezeLogPage(pageable, teacherId), JsonResultModel.class);
        return new PageImpl<>(
                JsonResultModuleUtils.getListFromPageResult(jsonResultModel, FreezeLogDto.class,
                        ((clazz, beanMap) -> ObjectUtils.convertObject(beanMap, clazz))),
                pageable, JsonResultModuleUtils.getTotalElements(jsonResultModel));
    }

    public List<TeacherInfo> getTeacherListByName(String name) {
        JsonResultModel jsonResultModel = restTemplate.postForObject(
                createGetTeacherListByName(), Collections.singletonMap("teacherName", name), JsonResultModel.class);
        return jsonResultModel.getListData(TeacherInfo.class);
    }

    /************************************* 构建URI ********************************/


    private URI createUpdateFreezeTeacherURI(){
        return UriComponentsBuilder
                .fromUriString(commentCardManageUrl.getTeacherStudentBusinessUrl())
                .path("f_teacher_review/update_freeze_status")
                .queryParam("boxfish_key", commentCardManageUrl.getBoxfishKey())
                .build()
                .toUri();
    }

    private URI getInnerTeacherURI(){
        logger.info("Accessing getInnerTeacher in CommentCardSDK......");
        return UriComponentsBuilder.fromUriString(commentCardManageUrl.getTeacherStudentBusinessUrl())
                .path("/f_teacher_review/get_inner_f_review_teacher")
                .queryParam("boxfish_key", commentCardManageUrl.getBoxfishKey())
                .build()
                .toUri();
    }

    private URI createTokenCheckURI(String accessToken) {
        return UriComponentsBuilder.fromUriString(commentCardManageUrl.getAuthenticationUrl())
                .path("/backend/login/checktoken/" + accessToken + "/out")
                .build()
                .toUri();
    }

    private URI createNoCommentPageURI(Pageable pageable, TeacherForm teacherForm) {
        MultiValueMap<String, String> paramMap = teacherForm.createValueMap();
        paramMap.add("page", pageable.getPageNumber() + "");
        paramMap.add("size", pageable.getPageSize() + "");
        return UriComponentsBuilder.fromUriString(commentCardManageUrl.getTeacherStudentBusinessUrl())
                .path("f_teacher_review/get_review_todaycount")
                .queryParams(paramMap)
                .queryParam("boxfish_key", commentCardManageUrl.getBoxfishKey())
                .build()
                .toUri();
    }

    private URI createCommentCountSetLogPageURI(Pageable pageable){
        return UriComponentsBuilder
                .fromUriString(commentCardManageUrl.getTeacherStudentBusinessUrl())
                .path("/f_teacher_review/get_review_count")
                .queryParam("page", pageable.getPageNumber())
                .queryParam("size", pageable.getPageSize())
                .queryParam("boxfish_key", commentCardManageUrl.getBoxfishKey())
                .build()
                .toUri();
    }

    private URI createGetTeacherOperationURI(Long teacherId){
        return UriComponentsBuilder.fromUriString(commentCardManageUrl.getTeacherStudentBusinessUrl())
                .path("/" + teacherId)
                .queryParam("boxfish_key", commentCardManageUrl.getBoxfishKey())
                .build()
                .toUri();
    }

    private URI createCanCommentTeacherPageURI(Pageable pageable) {
        return UriComponentsBuilder.fromUriString(commentCardManageUrl.getTeacherStudentBusinessUrl())
                .path("/f_teacher_review/query_review_teacher_infos")
                .queryParam("page", pageable.getPageNumber())
                .queryParam("size", pageable.getPageSize())
                .queryParam("boxfish_key", commentCardManageUrl.getBoxfishKey())
                .build()
                .toUri();
    }

    private URI createChangeTeacherURI() {
        return UriComponentsBuilder.fromUriString(commentCardManageUrl.getTeacherStudentBusinessUrl())
                .path("/f_teacher_review/change_review_teacher_batch")
                .queryParam("boxfish_key", commentCardManageUrl.getBoxfishKey())
                .build()
                .toUri();
    }


    private URI createGetTeacherInfoURI(Long teacherId) {
        return UriComponentsBuilder.fromUriString(commentCardManageUrl.getTeacherStudentBusinessUrl())
                .path("/f_teacher_review/get_review_teacher_info/" + teacherId)
                .queryParam("boxfish_key", commentCardManageUrl.getBoxfishKey())
                .build()
                .toUri();
    }

    private URI createGetTeacherFreezeLogPage(Pageable pageable, Long teacherId) {
        return UriComponentsBuilder.fromUriString(commentCardManageUrl.getTeacherStudentBusinessUrl())
                .path("/f_teacher_review/get_freeze_log/" + teacherId)
                .queryParam("page", pageable.getPageNumber())
                .queryParam("size", pageable.getPageSize())
                .queryParam("boxfish_key", commentCardManageUrl.getBoxfishKey())
                .build()
                .toUri();
    }

    private URI createGetTeacherListByName() {
        return UriComponentsBuilder.fromUriString(commentCardManageUrl.getTeacherStudentBusinessUrl())
                .path("/f_teacher_review/query_teachers_byname")
                .queryParam("boxfish_key", commentCardManageUrl.getBoxfishKey())
                .build()
                .toUri();
    }

    private Map<String, Object> getInnerTeacherParameterMap(CommentCard commentCard) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("fishCardId",commentCard.getId());
        paramMap.put("studentId",commentCard.getStudentId());
        paramMap.put("courseId",commentCard.getCourseId());
        return paramMap;
    }
}
