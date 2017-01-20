package com.boxfishedu.workorder.servicex.studentrelated;

import com.boxfishedu.workorder.common.config.CommentCardUrlConf;
import com.boxfishedu.workorder.common.util.RestTemplateUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

/**
 * Created by LuoLiBing on 17/1/12.
 */
@Data
@Component
public class PublicClassRoomNotification {

    private final static Logger logger = LoggerFactory.getLogger(PublicClassRoomNotification.class);

    private static RestTemplate restTemplate = RestTemplateUtil.getTemplate();

    private static CommentCardUrlConf commentCardUrlConf;

    private String production = "release:student::ios";

    private Set<String> tags = new HashSet<>();

    private String push_title = "今天的外教公开课马上开始啦,快点准备好吧~";

    private final Map<String, String> data;

    public PublicClassRoomNotification() {
        data = new HashMap<>();
        data.put("type", "PUBLIC_CLASS_NOTIFY");
    }

    @Autowired
    private void init(CommentCardUrlConf c) {
        commentCardUrlConf = c;
    }

    public PublicClassRoomNotification addDataProperty(String key, String val) {
        data.put(key, val);
        return this;
    }

    public PublicClassRoomNotification addTag(String tag) {
        tags.add(tag);
        return this;
    }

    public PublicClassRoomNotification addTag(Set<String> tag) {
        tags.addAll(tag);
        return this;
    }



    public void notifyPush() {
        restTemplate.postForObject(createPublicClassNotifyPushURI(), this, Object.class);
    }

    private URI createPublicClassNotifyPushURI() {
        logger.info("Accessing createPublicClassNotifyPushURI in ServiceSDK......");
        return UriComponentsBuilder.fromUriString(commentCardUrlConf.getPushInfoIrl())
                .path("/notification/push/tag")
                .build()
                .toUri();
    }
}