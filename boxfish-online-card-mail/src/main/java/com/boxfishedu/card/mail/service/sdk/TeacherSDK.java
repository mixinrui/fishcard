package com.boxfishedu.card.mail.service.sdk;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.card.mail.dto.TeacherInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Created by LuoLiBing on 16/9/1.
 */
@Service
public class TeacherSDK {

    @Value(value = "${service.sdk.teacherAbsenceUrl}")
    private String teacherUrl;

    private RestTemplate restTemplate = new RestTemplate();

    public TeacherInfo teacherInfo(Long teacherId) {
        JsonResultModel jsonResultModel = restTemplate.getForObject(createTeacherInfoUri(teacherId), JsonResultModel.class);
        return jsonResultModel.getData(TeacherInfo.class);
    }

    private URI createTeacherInfoUri(Long teacherId) {
        return UriComponentsBuilder
                .fromUriString(teacherUrl)
                .path("/teacher/" + teacherId)
                .build()
                .toUri();

    }
}
