package com.boxfishedu.workorder.web.filter;


import com.boxfishedu.workorder.common.config.UrlConf;
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
    public Long getUserId(String accessToken){
        if(StringUtils.isNotBlank(accessToken)) {
            Map<String, Object> map = null;
            try {
                map = getUserMap(accessToken);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return MapUtils.getLong(map,"id");
        }
        return null;
    }

    private Map<String, Object> getUserMap(String accessToken) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        String tempUrl = urlConf.getAuth_user() + "/user/me?access_token=" + accessToken;
        logger.debug("token_url: " + tempUrl);
        HttpGet httpGet = new HttpGet(tempUrl);
        HttpResponse response = httpClient.execute(httpGet);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.readValue(response.getEntity().getContent(), Map.class);
        return map;
    }

}
