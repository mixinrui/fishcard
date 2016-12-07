package com.boxfishedu.workorder.servicex.coursenotify;

import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.workorder.common.bean.MessagePushTypeEnum;
import com.boxfishedu.workorder.common.bean.QueueTypeEnum;
import com.boxfishedu.workorder.common.exception.BusinessException;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.ShortMessageCodeConstant;
import com.boxfishedu.workorder.common.util.WorkOrderConstant;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.servicex.graborder.CourseChangeServiceX;
import com.boxfishedu.workorder.web.param.StudentNotifyParam;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * 关于节假日通知学生 换时间功能
 * Created by jiaozijun on 16/12/2.
 */
@Component
public class CourseChangeTimeNotifySerceX {

    //本地异常日志记录对象
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private ThreadPoolManager threadPoolManager;

    @Autowired
    private RabbitMqSender rabbitMqSender;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    public void notifyStu(StudentNotifyParam studentNotifyParam){
        if(null==studentNotifyParam  )
            throw new BusinessException("参数有错误");
        if(null ==studentNotifyParam.getStudentIds()){
            notiFyStudentChangeTime(studentNotifyParam.getAppreceiveFlag());
        }else{
            notifyPersons(studentNotifyParam.getStudentIds(),studentNotifyParam.getAppreceiveFlag());
        }
    }

    /**
     * 短信通知学生换时间+推送
     * appReceiveFlag app 是否接受推送
     */
    public void notiFyStudentChangeTime(Boolean appReceiveFlag) {

        logger.info("notiFyStudentChangeTime--->开始检索老师今天有课的记录");
        Map<Long,List<WorkOrder>> studentNotifyMap  = workOrderService.getNotifyMessage();
        if (CollectionUtils.isEmpty(studentNotifyMap)) {
            logger.info("notiFyStudentChangeTime--->没有提醒的课程");
            return;
        }

        try {

            logger.info("notiFyStudentChangeTime:::::::匹配_studentNotifyMap ,size=[{}]::::::::::::::::::::::::::::::::", studentNotifyMap == null ? 0 : studentNotifyMap.size());
            logger.info("notiFyStudentChangeTime:::::::::studentNotifyMap [{}]::::::::::::::::::::::::::::::::", studentNotifyMap);

        } catch (Exception e) {
            logger.error("lazyLoadError");
            e.printStackTrace();
        }


        /**  begin  发送短信 **/

        for(Long studentId:studentNotifyMap.keySet()){
            sendShortMessage(studentNotifyMap.get(studentId));
            if(null!=appReceiveFlag && appReceiveFlag){
                sendAPPMessage(studentNotifyMap.get(studentId));
            }
        }
        /**  end    发送短信 **/

        logger.info("studentNotifyMap:::::通知完成");

    }



    /**
     * 指定学生发通知
     * @param studentIds
     */
    public void notifyPersons(Long[] studentIds,Boolean appReceive){
        if(null==studentIds || studentIds.length<1)
            throw new BusinessException("参数有误");

        studentIds = trimArray(studentIds);
        logger.info("notiFyStudentChangeTime@notifyPersons->studentInfo:[{}]",studentIds);
        for(Long stuId:studentIds){
            List<WorkOrder> workOrders = workOrderService.getNotifyMessageByStudentId(stuId);
            if(!CollectionUtils.isEmpty(workOrders)){
                sendShortMessage(workOrders);
                if(null!=appReceive && appReceive){
                     sendAPPMessage(workOrders);
                }
            }
        }
    }

    /**
     * 课程列表获取提醒是否还有提醒修改时间信息
     * @param studentId
     * @return
     */
    public JsonResultModel getNotifyByStudentId(Long studentId){
        List<WorkOrder> workOrders = workOrderService.getNotifyMessageByStudentId(studentId);
        if(CollectionUtils.isEmpty(workOrders)){
            return JsonResultModel.newJsonResultModel("ok");
        }
        String message = getAppNotices(workOrders);
        JSONObject jo = new JSONObject();
        jo.put("title","课程调整");
        jo.put("message",message);

        return JsonResultModel.newJsonResultModel(jo);
    }


    public Long []  trimArray(Long [] studentIds){
        Set<Long> set = new TreeSet<Long>();//新建一个set集合
        for (Long i : studentIds) {
            set.add(i);
        }
        return  set.toArray(new Long[0]);
    }

    /**
     * 给学生发送短信
     *
     * @param myClass  提醒学生换时间
     */
    private void sendShortMessage( List<WorkOrder> myClass) {
        long studentId =myClass.get(0).getStudentId();

        Object messageStuct = getMessageStu(studentId,  myClass);

            threadPoolManager.execute(new Thread(() -> {
                logger.info("notiFyStudentChangeTime@sendShortMessage,studentId=[{}]",studentId);
                // 发送短信 向短信队列发送q消息
                    rabbitMqSender.send(messageStuct, QueueTypeEnum.SHORT_MESSAGE);
            }));

    }


    /**
     * app消息推送
     * @param myClass
     */
    private void sendAPPMessage( List<WorkOrder> myClass) {
        threadPoolManager.execute(new Thread(() -> {
            // 发送短信 向短信队列发送q消息
            this.pushStudentMessage(myClass);
        }));

    }


    /**
     * app 推送消息
     * @param myClass
     */
    public void pushStudentMessage(List<WorkOrder> myClass) {
        long studentId =myClass.get(0).getStudentId();
        logger.info("notiFyStudentChangeTime@pushStudentMessage,studentId=[{}]",studentId);
        String pushTitle = "";// WorkOrderConstant.SEND_GRAB_ORDER_MESSAGE_FOREIGH;
        Map map1 = Maps.newHashMap();
        map1.put("user_id", studentId);
        map1.put("push_title", pushTitle);

        String jo = getAppNotices(myClass);
        map1.put("data", jo);
        teacherStudentRequester.pushTeacherListOnlineMsg(map1);
    }

    /**
     * 学生消息体
     *
     * @param userId
     * @param list
     * @return
     */
    private Object getMessageStu(Long userId, List<WorkOrder> list) {
        Map map = Maps.newHashMap();
        map.put("user_id", userId);
        map.put("template_code", ShortMessageCodeConstant.SMS_STU_NOTITY_CHANGE_CODE);

        JSONObject jo = new JSONObject();
        jo.put("date", getDate(list));
        jo.put("mount",String.valueOf(list.size()));
        jo.put("reason","恰逢节假日");

        map.put("data", jo.toJSONString());
        return map;

    }


    /**
     * app课表页面提示
     * @param workOrders
     * @return
     */
    private  String getAppNotices(List<WorkOrder> workOrders){
        String begin = "同学,你在 ";
        String date = getDate(workOrders);
        String end = " 共计 "+workOrders.size()+" 节课,由于恰逢节假日不能上课,请尽快修改上课时间吧~";
        return begin+date+end;
    }



    public String getDate(List<WorkOrder> list){
        List<String> listdate = Lists.newArrayList();
        for(WorkOrder workOrder:list){
            if(!listdate.contains( DateUtil.formatMonthDay2String(workOrder.getStartTime()))){
                listdate.add(DateUtil.formatMonthDay2String(workOrder.getStartTime()));
            }
        }
//        Map<String,Long> map = list.stream().collect(Collectors.groupingBy( (workOrder) ->  DateUtil.formatMonthDay2String(workOrder.getStartTime()) ,Collectors.counting()));
//        Set<String> selectedSet = map.entrySet().stream().filter(entry -> entry.getValue() > 0).map(entry -> entry.getKey()).sorted().collect(Collectors.toSet());

        return  StringUtils.collectionToDelimitedString(listdate,",");
    }

    public static void main(String[] args) {

        WorkOrder w1 = new WorkOrder();
        w1.setStartTime(DateUtil.String2Date("2016-09-27 07:00:00"));

        WorkOrder w2 =  w1.clone();
        w2.setStartTime(DateUtil.String2Date("2016-10-27 07:00:00"));

        WorkOrder w3 =  w1.clone();
        w3.setStartTime(DateUtil.String2Date("2016-10-28 07:00:00"));

        WorkOrder w4 =  w1.clone();
        w4.setStartTime(DateUtil.String2Date("2016-10-29 07:00:00"));

        WorkOrder w5 =  w1.clone();
        w5.setStartTime(DateUtil.String2Date("2016-10-29 07:00:00"));

        WorkOrder w6 =  w1.clone();
        w6.setStartTime(DateUtil.String2Date("2016-11-27 07:00:00"));

        List<WorkOrder> wk = Lists.newArrayList();
        wk.add(w1);wk.add(w2);wk.add(w3);wk.add(w4);wk.add(w5);wk.add(w6);

        CourseChangeTimeNotifySerceX courseChangeServiceX = new CourseChangeTimeNotifySerceX();
        System.out.println(courseChangeServiceX.getDate(wk));

        Long [] s = {323l,323l,323l,322323l};
        System.out.println(courseChangeServiceX.trimArray(s));
    }

}
