package com.boxfishedu.workorder.requester.resultbean;

import lombok.Data;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Map;

/**
 * Created by hucl on 16/8/4.
 * 获取数据组返回的时间集合信息
 */
@Data
public class EventResultBean{
    private Boolean last;
    private Boolean first;
    private Long totalPages;
    private Long totalElements;
    private Long size;
    private Long number;
    private Long numberOfElements;
    private List<EventResult> content;

    @Data
    public static class EventResult{
        private String id;
        private String userId;
        private String deviceId;
        private String eventId;
        private String type;
        private Long time;
        private Long msgReceiveTime;
        private Map<String, Object> properties;
    }
}
