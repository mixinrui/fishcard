package com.boxfishedu.mall.permission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
public class UserInfoValidator {

    @Value("${permission_host}")
    private String token_validate_api;

    public Long getUserId(String accessToken) {
        UserInfo userInfo = this.getUserInfo(accessToken);
        if (isNull(userInfo)) {
            return null;
        }
        return userInfo.getId();
    }

    @Autowired
    private RestTemplate restTemplate;

    public UserInfo getUserInfo(String accessToken) {
        if (isEmpty(accessToken)) {
            return null;
        }
//        //FIXME 调试期间留的后门
//        if (accessToken.contentEquals("liuzhihao1")) {
//            return UserInfo.createUserInfo();
//        }
        try {
            String url = token_validate_api + accessToken;

            return this.restTemplate.getForObject(url, UserInfo.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
