package com.boxfishedu.workorder.common.login;

import lombok.Data;

/**
 * 鱼卡登陆后台
 * Created by jiaozijun on 16/6/22.
 */

@Data
public class UserInfo {
    private String userName;//用户名
    private String passWord;//密码
    private String realName;//姓名
    private String newPassWord;//新密码
}
