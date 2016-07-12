package com.boxfishedu.workorder.requester;

import com.boxfishedu.workorder.common.bean.TeachingNotificationEnum;
import com.boxfishedu.workorder.common.bean.TeachingOnlineMsg;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Created by hucl on 16/6/17.
 * 与在线授课相关接口
 */
@Component
public class CourseOnlineRequester {
    @Autowired
    private UrlConf urlConf;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ThreadPoolManager threadPoolManager;
    @Autowired
    private WorkOrderLogService workOrderLogService;

    private Logger logger= LoggerFactory.getLogger(this.getClass());
    public void releaseGroup(WorkOrder workOrder){
        String url=String.format("%s/teaching/destroy_group?work_order_id=%s",urlConf.getCourse_online_service(),workOrder.getId());
        logger.debug("<<<<<<<<<<<<<@[releaseGroup]向在线教育发起[[[[释放师生关系]]]],url[{}]",url);
        threadPoolManager.execute(new Thread(()->{restTemplate.getForObject(url,Object.class);}));
        workOrderLogService.saveWorkOrderLog(workOrder,"解散群组关系");
    }

    /**
     *向运营组发起通知请求
     */
    public void notifyTeachingOnlinePushMessage(Long workOrderId,TeachingNotificationEnum TeachingNotificationEnum){
        String url=String.format("%s/teaching/callback/push_notification?work_order_id=%s&notification_type=%s",
                urlConf.getCourse_online_service(),workOrderId,TeachingNotificationEnum.toString());
        logger.debug("<<<<<<<<<<<<<@[notifyTeachingOnlinePushMessage]向在线教育发起通知操作,[[[[通知在线教学中心推送消息]]]],url[{}]",url);
        threadPoolManager.execute(new Thread(()->{restTemplate.getForObject(url,Object.class);}));
    }

    /**
     * 发送封装好的消息
     */
    public void pushWrappedMsg(TeachingOnlineMsg teachingOnlineMsg){
        List<TeachingOnlineMsg> requestBody= Lists.newArrayList();
        requestBody.add(teachingOnlineMsg);
        String url=String.format("%s/teaching/callback/push",
                urlConf.getCourse_online_service());
        logger.debug("<<<<<<<<<<<<<@[pushWrappedMsg]向在线教育发起通知操作,[[[[通知用户[{}]推送消息[{}]]]]],url[{}],内容:{}",url,
                teachingOnlineMsg.getUser_id(),teachingOnlineMsg.getPush_title(), JacksonUtil.toJSon(requestBody));
        threadPoolManager.execute(new Thread(()->{restTemplate.postForObject(url,requestBody,Object.class);}));
    }
}
