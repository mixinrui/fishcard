package com.boxfishedu.mall.permission;

import lombok.Data;

/**
 * 用于接收鉴权接口的bean
 * Created by lauzhihao on 2016/06/08.
 */
@Data
public class UserInfo {

    private Long id;

    private String username;

    private Integer score;

    private String access_token;

    public static UserInfo createUserInfo() {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(12345L);
        userInfo.setAccess_token("liuzhihao1");
        userInfo.setUsername("liuzhihao@boxfish.cn");
        userInfo.setScore(1);

        return userInfo;
    }
}
