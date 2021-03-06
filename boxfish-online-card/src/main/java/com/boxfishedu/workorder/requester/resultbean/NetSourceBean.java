package com.boxfishedu.workorder.requester.resultbean;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by hucl on 16/12/22.
 */
@Data
public class NetSourceBean {
    private List<ContentBean> content;


    private Boolean last;
    private Long totalPages;
    private Long totalElements;

    private Object sort;

    private Boolean first;
    private Long numberOfElements;
    private Long size;
    private Long number;

    @Data
    public static class ContentBean {
        private String id;
        private String appKey;
        private String userId;
        private String deviceId;
        private String eventId;
        private String type;
        private Map<String, Object> properties;
        private String event;
        private Long appTime;
        private Long sysTime;
    }
}
