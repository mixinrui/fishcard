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
 * Created by hucl on 16/11/30.
 */
@SuppressWarnings("ALL")
@Component
public class ParentRelationGetter {

    private final static Logger logger = LoggerFactory.getLogger(UserIdComparison.class);

    @Autowired
    private UrlConf urlConf;

    public ParentAuthBean getRelation(Long studentId,String accessToken) throws IOException {
      return getUserMap(studentId,accessToken);
    }


    private ParentAuthBean getUserMap(Long studentId,String accessToken) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        String authUrl=String.format("%s/relation/user/%s?access_token=%s",urlConf.getAuth_user(),studentId,accessToken);
        logger.debug("token_url: " + authUrl);
        HttpGet httpGet = new HttpGet(authUrl);
        HttpResponse response = httpClient.execute(httpGet);
        int status = response.getStatusLine().getStatusCode();
        if(status == HttpStatus.NOT_FOUND.value()) {
            throw new UnauthorizedException();
        } else if(status != HttpStatus.OK.value()) {
            throw new BusinessException();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(response.getEntity().getContent(), ParentAuthBean.class);
    }
}
