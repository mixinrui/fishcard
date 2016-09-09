package com.boxfishedu.card.comment.manage.entity.dto.merger;

import com.boxfishedu.card.comment.manage.entity.dto.TeacherInfo;
import com.boxfishedu.card.comment.manage.service.sdk.CommentCardManageSDK;
import com.boxfishedu.card.comment.manage.util.ApplicationContextProvider;

import java.util.Objects;

/**
 * Created by LuoLiBing on 16/5/18.
 * 获取用户详情
 */
public class TeacherInfoRpcMerger extends BaseRpcMerger<TeacherInfo, Long> {

    private static CommentCardManageSDK commentCardManageSDK =
            ApplicationContextProvider.getBean(CommentCardManageSDK.class);

    @Override
    public TeacherInfo rpcCall(Long identify) {
        try {
            TeacherInfo teacherInfo = commentCardManageSDK.getTeacherInfoById(identify);
            return Objects.isNull(teacherInfo) ? TeacherInfo.UNKNOW : teacherInfo;
        } catch (Exception e) {
            return TeacherInfo.UNKNOW;
        }
    }
}
