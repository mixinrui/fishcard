package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

import java.util.Date;

/**
 * Created by ansel on 2017/3/21.
 */
@Data
public class MonitorUserRequestForm {

    private Long userId;

    private String userName;

    private String passWord;

    private String accessToken;

    private String userType;
}
