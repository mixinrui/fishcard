package com.boxfishedu.workorder.requester;

import com.boxfishedu.workorder.common.bean.MessagePushTypeEnum;
import com.boxfishedu.workorder.common.bean.TeachingNotificationEnum;
import com.boxfishedu.workorder.common.bean.TeachingOnlineMsg;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.dao.mongo.InstantCardLogMorphiaRepository;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.ServiceSDK;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.web.result.InstantGroupInfo;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.web.view.fishcard.FishCardGroupsInfo;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    @Autowired
    private ServiceSDK serviceSDK;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public void releaseGroup(WorkOrder workOrder) {
        String url = String.format("%s/teaching/destroy_group?work_order_id=%s", urlConf.getCourse_online_service(), workOrder.getId());
        logger.debug("<<<<<<<<<<<<<@[releaseGroup]向在线教育发起[[[[释放师生关系]]]],url[{}]", url);
        threadPoolManager.execute(new Thread(() -> {
            restTemplate.getForObject(url, Object.class);
        }));
        workOrderLogService.saveWorkOrderLog(workOrder, "解散群组关系");
    }

    public void instantReleaseGroup(WorkOrder workOrder) {
        String url = String.format("%s/teaching/destroy_group?work_order_id=%s&instance=true", urlConf.getCourse_online_service(), workOrder.getId());
        logger.debug("<<<<<<<<<<<<<@[releaseGroup]向在线教育发起[[[[释放师生关系]]]],url[{}]", url);
        restTemplate.getForObject(url, Object.class);
        ;
        workOrderLogService.saveWorkOrderLog(workOrder, "立即解散群组关系");
    }

    public void rebuildGroup(List<WorkOrder> typeUnchangedList) {
        if (CollectionUtils.isEmpty(typeUnchangedList)) {
            return;
        }
        threadPoolManager.execute(new Thread(() -> {
            typeUnchangedList.forEach(card -> {
                workOrderLogService.saveWorkOrderLog(card
                        , String.format("实时上课重建鱼卡群组关系,teacher[%s],student[%s]"
                                , card.getTeacherId(), card.getStudentId()));
                // 创建群组
                serviceSDK.createGroup(card);
            });
        }));
    }

    /**
     * 向运营组发起通知请求
     */
    public void notifyTeachingOnlinePushMessage(Long workOrderId, TeachingNotificationEnum TeachingNotificationEnum) {
        String url = String.format("%s/teaching/callback/push_notification?work_order_id=%s&notification_type=%s",
                                   urlConf.getCourse_online_service(), workOrderId, TeachingNotificationEnum.toString());
        logger.debug("<<<<<<<<<<<<<@[notifyTeachingOnlinePushMessage]向在线教育发起通知操作,[[[[通知在线教学中心推送消息]]]],url[{}]", url);
        threadPoolManager.execute(new Thread(() -> {
            restTemplate.getForObject(url, Object.class);
        }));
    }

    public void notifyInstantClassMsg(InstantClassCard instantClassCard, List<Long> teacherIds) {
        String url = String.format("%s/teaching/callback/push",
                                   urlConf.getCourse_online_service());
//        instantCardLogMorphiaRepository.saveInstantLog(instantClassCard,teacherIds,"向教师发起推送");
        List<TeachingOnlineMsg> teachingOnlineMsgList = Lists.newArrayList();
        teacherIds.forEach(teacherId -> {
            TeachingOnlineMsg teachingOnlineMsg = new TeachingOnlineMsg();
            //TODO:即时上课,推送给教师的信息;需要到配置项中去
            teachingOnlineMsg.setPush_title("Many a student calls in for online LIVE teaching. Click and get prepared.");
            teachingOnlineMsg.setUser_id(teacherId);
            teachingOnlineMsg.setPush_type(MessagePushTypeEnum.SEND_INSTANT_CLASS_TYPE.toString());

            TeachingOnlineMsg.TeachingOnlineMsgAttach teachingOnlineMsgAttach = new TeachingOnlineMsg.TeachingOnlineMsgAttach();
            teachingOnlineMsgAttach.setType(MessagePushTypeEnum.SEND_INSTANT_CLASS_TYPE.toString());
            teachingOnlineMsgAttach.setCardId(instantClassCard.getId());
            teachingOnlineMsgAttach.setDay(DateUtil.simpleDate2String(instantClassCard.getClassDate()));
            teachingOnlineMsgAttach.setSlotId(instantClassCard.getSlotId());
            teachingOnlineMsgAttach.setCount(teacherIds.size());
            teachingOnlineMsgAttach.setStudentId(instantClassCard.getStudentId());

            teachingOnlineMsg.setData(teachingOnlineMsgAttach);

            teachingOnlineMsgList.add(teachingOnlineMsg);
        });
        logger.debug(">>>>>>@notifyInstantClassMsg, IIIIIIIIIIIIIII 向教师发起推送实时上课请求,courseInfo[{}],教师信息[{}]"
                , JacksonUtil.toJSon(teachingOnlineMsgList), teacherIds);
        threadPoolManager.execute(new Thread(() -> {
            restTemplate.postForObject(url, teachingOnlineMsgList, Object.class);
        }));
    }

    /**
     * 发送封装好的消息
     */
    public void pushWrappedMsg(TeachingOnlineMsg teachingOnlineMsg) {
        List<TeachingOnlineMsg> requestBody = Lists.newArrayList();
        requestBody.add(teachingOnlineMsg);
        String url = String.format("%s/teaching/callback/push",
                                   urlConf.getCourse_online_service());
        logger.debug("<<<<<<<<<<<<<@[pushWrappedMsg]向在线教育发起通知操作,[[[[通知用户[{}]推送消息[{}]]]]],url[{}],内容:{}", url,
                     teachingOnlineMsg.getUser_id(), teachingOnlineMsg.getPush_title(), JacksonUtil.toJSon(requestBody));
        threadPoolManager.execute(new Thread(() -> {
            restTemplate.postForObject(url, requestBody, Object.class);
        }));
    }

    public InstantGroupInfo instantCreateGroup(WorkOrder workOrder) {
        String url = String.format("%s/teaching/group/create?work_order_id=%s&stu_id=%s&tea_id=%s",
                                   urlConf.getCourse_online_service(), workOrder.getId(), workOrder.getStudentId(), workOrder.getTeacherId());
        logger.debug("@>>>>>>>>>>>instantCreateGroup#begin向在线上课发起实时上课发起建群请求,url:[{}]", url);
        JsonResultModel jsonResultModel = restTemplate.postForObject(url, null, JsonResultModel.class);
        logger.debug("@>>>>>>>>>>>instantCreateGroup#end#获取师生运营实时建群结果,result:[{}]", jsonResultModel);
        return jsonResultModel.getData(InstantGroupInfo.class);

    }

    public FishCardGroupsInfo buildsmallClassChatRoom(SmallClass smallClass) {
        return this.buildsmallClassChatRoom(
                smallClass.getId(), smallClass.getClassType(), smallClass.getAllStudentIds(), Arrays.asList(smallClass.getTeacherId()));
    }

    public FishCardGroupsInfo buildsmallClassChatRoom(Long smallClassId, String classType, List<Long> studentIds, List<Long> teacherIds) {
        String url = String.format("%s/teaching/smallclass/group",
                                   urlConf.getCourse_online_service());
        Map param = Maps.newHashMap();
        param.put("smallClassId", smallClassId);
        param.put("teacherId", teacherIds);
        param.put("studentId", studentIds);
        param.put("classType", classType);

        JsonResultModel jsonResultModel = null;
        try {
            jsonResultModel = restTemplate.postForObject(url, param, JsonResultModel.class);
            logger.debug("@buildsmallClassChatRoom#创建群组,聊天室成功,url[{}],参数[{}],结果[{}]", url, param, JacksonUtil.toJSon(jsonResultModel));
        } catch (Exception ex) {
            logger.error("@buildsmallClassChatRoom#创建群组,聊天室失败,url[{}],参数[{}]", url, param, ex);
            throw new BusinessException("创建聊天室失败");
        }
        FishCardGroupsInfo fishCardGroupsInfo = jsonResultModel.getData(FishCardGroupsInfo.class);
        return fishCardGroupsInfo;


    }

    // 小班课 换老师解散群组关系
    public void instantReleaseGroupForSmallClass(Long smallClassId,List<WorkOrder> workOrders) {
        String url = String.format("%s/teaching/smallclass/group?%s", urlConf.getCourse_online_service(),"small_class_id="+smallClassId);
        logger.debug("<<<<<<<<<<<<<@[instantReleaseGroupForSmallClass]小班课向在线教育发起[[[[释放师生关系]]]],url[{}]", url);
        restTemplate.delete(url);
        workOrders.stream().forEach(workOrder -> {
            workOrderLogService.saveWorkOrderLog(workOrder, "小班课立即解散群组关系");
        });

    }
}
