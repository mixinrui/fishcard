package com.boxfishedu.workorder.requester;

import com.boxfishedu.workorder.common.bean.MessagePushTypeEnum;
import com.boxfishedu.workorder.common.bean.TeachingNotificationEnum;
import com.boxfishedu.workorder.common.bean.TeachingOnlineGroupMsg;
import com.boxfishedu.workorder.common.bean.TeachingOnlineMsg;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.dao.mongo.InstantCardLogMorphiaRepository;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.web.result.InstantGroupInfo;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
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
    @Autowired
    private InstantCardLogMorphiaRepository instantCardLogMorphiaRepository;

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
        String url=String.format("%s/teaching/callback/push/group",
                urlConf.getCourse_online_service());
        instantCardLogMorphiaRepository.saveInstantLog(instantClassCard,teacherIds,"向教师发起推送");
        TeachingOnlineGroupMsg teachingOnlineGroupMsg=new TeachingOnlineGroupMsg();
        teachingOnlineGroupMsg.setPush_title("Many a student calls in for online LIVE teaching. Click and get prepared.");

        TeachingOnlineGroupMsg.TeachingOnlineMsgAttach teachingOnlineMsgAttach=new  TeachingOnlineGroupMsg.TeachingOnlineMsgAttach();
        teachingOnlineMsgAttach.setType(MessagePushTypeEnum.SEND_INSTANT_CLASS_TYPE.toString());
        teachingOnlineMsgAttach.setCardId(instantClassCard.getId());
        teachingOnlineMsgAttach.setDay(DateUtil.simpleDate2String(instantClassCard.getClassDate()));
        teachingOnlineMsgAttach.setSlotId(instantClassCard.getSlotId());
        teachingOnlineMsgAttach.setCount(teacherIds.size());
        teachingOnlineMsgAttach.setStudentId(instantClassCard.getStudentId());
        teachingOnlineGroupMsg.setData(teachingOnlineMsgAttach);

        teacherIds.forEach(teacherId-> teachingOnlineGroupMsg.getAlias().add(teacherId.toString()));

        logger.debug(">>>>>>@notifyInstantClassMsg, IIIIIIIIIIIIIII 向教师发起推送实时上课请求,courseInfo[{}],教师信息[{}]"
                ,JacksonUtil.toJSon(teachingOnlineGroupMsg),teacherIds);
        threadPoolManager.execute(new Thread(()->{restTemplate.postForObject(url,teachingOnlineGroupMsg,Object.class);}));
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

    public InstantGroupInfo instantCreateGroup(WorkOrder workOrder){
        String url=String.format("%s/teaching/group/create?work_order_id=%s&stu_id=%s&tea_id=%s",
                urlConf.getCourse_online_service(),workOrder.getId(),workOrder.getStudentId(),workOrder.getTeacherId());
        logger.debug("@>>>>>>>>>>>instantCreateGroup#begin向在线上课发起实时上课发起建群请求,url:[{}]",url);
        JsonResultModel jsonResultModel=restTemplate.postForObject(url,null,JsonResultModel.class);
        logger.debug("@>>>>>>>>>>>instantCreateGroup#end#获取师生运营实时建群结果,result:[{}]",jsonResultModel);
        return jsonResultModel.getData(InstantGroupInfo.class);

    }
}
