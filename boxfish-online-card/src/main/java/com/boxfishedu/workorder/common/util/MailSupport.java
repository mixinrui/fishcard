package com.boxfishedu.workorder.common.util;

import com.boxfishedu.workorder.common.config.CommentCardUrlConf;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by LuoLiBing on 17/1/6.
 */
@Component
public class MailSupport {

    @Autowired
    private UrlConf urlConf;

    @Autowired
    private CommentCardUrlConf commentCardUrlConf;

    @Autowired
    private RestTemplate restTemplate;

    private final static ObjectMapper objectMapper = new ObjectMapper();

    private ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public void reportError(String subject, Throwable e) {
        reportError(subject, ExceptionUtils.getStackTrace(e));
    }

    public void reportError(String subject, String content) {
        reportError(subject, content, null);
    }

    public void reportError(String subject, Throwable e, Object data) {
        String dataStr = null;
        if(!Objects.isNull(data)) {
            try {
                dataStr = objectMapper.writeValueAsString(data);
            } catch (JsonProcessingException e1) {
            }
        }
        reportError(subject, ExceptionUtils.getStackTrace(e), dataStr);
    }

    private void reportError(String subject, String content, String data) {
        // 如果没有配置邮箱url, 默认不发送
        if(commentCardUrlConf.getErrorReportMailUrl() == null) {
            return;
        }

        if(!StringUtils.isEmpty(data)) {
            content = String.format(", data: [%s] \n", data) + content;
        }
        final String finalContent = content;
        exec.execute(() -> {
            String sign = sign(urlConf.getMailToken(), urlConf.getRecipients(), finalContent, subject);
            restTemplate.postForObject(
                    createErrorReportMailURI(sign),
                    createRequestBody(subject, finalContent),
                    Object.class);
        });
    }


    private URI createErrorReportMailURI(String sign) {
        return UriComponentsBuilder.fromUriString(commentCardUrlConf.getErrorReportMailUrl())
                .path("/send")
                .queryParam("token", sign)
                .build()
                .toUri();
    }

    // 签名顺序 recipients, content, subject
    private static String sign(String token, Object...params) {
        StringBuilder builder = new StringBuilder(100);
        builder.append(token);
        for(int i = 0; i < params.length; i++) {
            builder.append(",");
            builder.append(params[i]);
        }
        return DigestUtils.md5Hex(builder.toString().getBytes());
    }

    private Map<String, Object> createRequestBody(String subject, String content) {
        Map<String, Object> body = new HashMap<>();
        body.put("recipients", urlConf.getRecipients());
        body.put("content", content);
        body.put("subject", subject);
        return body;
    }
}

