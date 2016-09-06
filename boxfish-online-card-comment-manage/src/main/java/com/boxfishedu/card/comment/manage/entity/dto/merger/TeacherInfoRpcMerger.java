package com.boxfishedu.card.comment.manage.entity.dto.merger;

import com.boxfishedu.card.comment.manage.entity.dto.TeacherInfo;

/**
 * Created by LuoLiBing on 16/5/18.
 * 获取用户详情
 */
public class TeacherInfoRpcMerger extends BaseRpcMerger<TeacherInfo, Long> {

    @Override
    public TeacherInfo rpcCall(Long identify) {
//        return financeSDK.getUserInfo(identify);
        return mock();
    }

    private TeacherInfo mock() {
        TeacherInfo userInfo = new TeacherInfo();
        userInfo.setStatus(100);
        userInfo.setStatusDesc("冻结");
        return userInfo;
    }
}
