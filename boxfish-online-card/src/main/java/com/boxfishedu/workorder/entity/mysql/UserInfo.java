package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

/**
 * Created by ansel on 16/8/9.
 */
@Data
public class UserInfo {
    Long userId;
    String username;
    Long score;
    Long gold;
    String access_token;
    String figure_url;
}
