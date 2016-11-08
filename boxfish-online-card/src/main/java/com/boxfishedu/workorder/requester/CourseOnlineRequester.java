package com.boxfishedu.workorder.requester;

import com.boxfishedu.workorder.common.bean.MessagePushTypeEnum;
import com.boxfishedu.workorder.common.bean.TeachingNotificationEnum;
import com.boxfishedu.workorder.common.bean.TeachingOnlineMsg;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
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

    public void notifyInstantClassMsg(InstantClassCard instantClassCard,List<Long> teacherIds){
        String url=String.format("%s/teaching/callback/push",
                urlConf.getCourse_online_service());
        List<TeachingOnlineMsg> teachingOnlineMsgList=Lists.newArrayList();
        teacherIds.forEach(teacherId->{
            TeachingOnlineMsg teachingOnlineMsg=new TeachingOnlineMsg();
            //TODO:即时上课,推送给教师的信息;需要到配置项中去
            teachingOnlineMsg.setPush_title("Received a call from a student for online LIVE teaching. Click and start teaching now");
            teachingOnlineMsg.setUser_id(teacherId);
            teachingOnlineMsg.setPush_type(MessagePushTypeEnum.SEND_INSTANT_CLASS_TYPE.toString());

            TeachingOnlineMsg.TeachingOnlineMsgAttach teachingOnlineMsgAttach=new  TeachingOnlineMsg.TeachingOnlineMsgAttach();
            teachingOnlineMsgAttach.setType(MessagePushTypeEnum.SEND_INSTANT_CLASS_TYPE.toString());
            teachingOnlineMsgAttach.setCardId(instantClassCard.getId());
            teachingOnlineMsgAttach.setDay(DateUtil.simpleDate2String(instantClassCard.getClassDate()));
            teachingOnlineMsgAttach.setSlotId(instantClassCard.getSlotId());
            teachingOnlineMsgAttach.setCount(teacherIds.size());

            teachingOnlineMsg.setData(teachingOnlineMsgAttach);

            teachingOnlineMsgList.add(teachingOnlineMsg);
        });
        logger.debug(">>>>>>@notifyInstantClassMsg,向教师发起推送实时上课请求,courseInfo[{}],教师信息[{}]"
                ,JacksonUtil.toJSon(teachingOnlineMsgList),teacherIds);
        threadPoolManager.execute(new Thread(()->{restTemplate.postForObject(url,teachingOnlineMsgList,Object.class);}));
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
