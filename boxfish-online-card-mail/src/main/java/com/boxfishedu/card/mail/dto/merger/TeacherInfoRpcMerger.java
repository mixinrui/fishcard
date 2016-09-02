package com.boxfishedu.card.mail.dto.merger;

import com.boxfishedu.card.mail.dto.NotFoundTeacherInfo;
import com.boxfishedu.card.mail.dto.TeacherInfo;
import com.boxfishedu.card.mail.service.sdk.TeacherSDK;
import com.boxfishedu.card.mail.utils.ApplicationContextProvider;

/**
 * Created by LuoLiBing on 16/9/1.
 */
public class TeacherInfoRpcMerger extends BaseRpcMerger<TeacherInfo, Long> {

    private TeacherSDK teacherSDK = ApplicationContextProvider.getBean(TeacherSDK.class);

    @Override
    public TeacherInfo rpcCall(Long identify) {
        try {
            TeacherInfo teacherInfo = teacherSDK.teacherInfo(identify);
            return teacherInfo == null? new NotFoundTeacherInfo(identify) : teacherInfo;
        } catch (Exception e) {
            return new NotFoundTeacherInfo(identify);
        }
    }
}
