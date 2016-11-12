package com.boxfishedu.workorder.web.filter;


import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.exception.UnauthorizedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Created by ansel on 16/5/10.
 */
@SuppressWarnings("ALL")
@Component
public class UserIdComparison {

    private final static Logger logger = LoggerFactory.getLogger(UserIdComparison.class);

    @Autowired
    private UrlConf urlConf;

    /**
     * @param accessToken
     * @return
     */
    public Long getUserId(String accessToken) throws IOException {
        if(StringUtils.isNotBlank(accessToken)) {
            Map<String, Object> map = getUserMap(accessToken);
            return MapUtils.getLong(map,"id");
        }
        throw new UnauthorizedException();
    }

    private Map<String, Object> getUserMap(String accessToken) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        String tempUrl = urlConf.getAuth_user() + "/user/me?access_token=" + accessToken;
        logger.debug("token_url: " + tempUrl);
        HttpGet httpGet = new HttpGet(tempUrl);
        HttpResponse response = httpClient.execute(httpGet);
        int status = response.getStatusLine().getStatusCode();
        if(status == HttpStatus.NOT_FOUND.value()) {
            throw new UnauthorizedException();
        } else if(status != HttpStatus.OK.value()) {
            throw new BusinessException();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(response.getEntity().getContent(), Map.class);
    }

}
