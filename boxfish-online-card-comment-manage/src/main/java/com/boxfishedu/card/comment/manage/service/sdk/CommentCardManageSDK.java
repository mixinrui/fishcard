package com.boxfishedu.card.comment.manage.service.sdk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.card.comment.manage.entity.form.ToTeacherStudentForm;
import org.springframework.stereotype.Component;

/**
 * Created by ansel on 16/9/2.
 */
@Service
public class CommentCardManageSDK {

    @Autowired
    private RestTemplate restTemplate;

    public void teacherDecrementCount(Long teacherId) {
        restTemplate.exchange(
                createTeacherDecrementCountURI(teacherId),
                HttpMethod.POST,
                HttpEntity.EMPTY,
                Object.class);
    }

    private URI createTeacherDecrementCountURI(Long teacherId) {
        return UriComponentsBuilder
                .fromUriString("")
                .path("/" + teacherId)
                .build()
                .toUri();
    }

    public JsonResultModel freezeTeacherId(Long teacherId){
        return null;
    }

    public JsonResultModel unfreezeTeacherId(Long teacherId){
        return null;
    }

    public JsonResultModel getTeacherIdList(ToTeacherStudentForm toTeacherStudentForm){
        return null;
    }
}
