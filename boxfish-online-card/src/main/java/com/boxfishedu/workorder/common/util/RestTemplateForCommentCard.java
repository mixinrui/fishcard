package com.boxfishedu.workorder.common.util;

import com.boxfishedu.workorder.common.exception.NotFoundException;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Objects;

/**
 * Created by ansel on 16/11/22.
 */
@Component
public class RestTemplateForCommentCard {

    @Bean(name = "comment_card")
    public  RestTemplate getTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if(Objects.equals(response.getStatusCode(), HttpStatus.NOT_FOUND)){
                    throw new NotFoundException();
                }
            }
        });
        return restTemplate;
    }
}
