package com.boxfishedu.workorder.servicex.orderrelated;


import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.web.view.base.JsonResultModel;
import com.boxfishedu.workorder.common.rabbitmq.RabbitMqSender;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.ServiceJpaRepository;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.CourseOnlineRequester;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.*;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.servicex.courseonline.CourseOnlineServiceX;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import java.util.Date;
import java.util.List;
import java.util.Map;

/** 
 * 退单相关操作
 *   退单的前提  是查看含有能够补课的鱼卡
 *     ******   补课 逻辑  把现有鱼卡状态变成补课 ,新增一条 待分配课状态的数据  ****
 *     退单 和 补课 都退补 明天之后的鱼卡
 * Created by zijun.jiao on 16/6/16.
 */
@Component
public class BackOrderServiceX{
    @Autowired
    private RabbitMqSender rabbitMqSender;

    @Autowired
    private ServiceJpaRepository serviceJpaRepository;


    @Autowired
    private CourseOnlineServiceX courseOnlineServiceX;

    @Autowired
    private ServeService serveService;


    @Autowired
    private BackOrderService backOrderService;

    @Autowired
    private CourseType2TeachingTypeService courseType2TeachingTypeService;

    @Autowired
    private CourseOnlineService courseOnlineService;

    @Autowired
    private CourseOnlineRequester courseOnlineRequester;

    @Autowired
    private TeacherStudentRequester teacherStudentRequester;

    @Autowired
    private WorkOrderLogService workOrderLogService;

    /** 能够排课的课程记录的时间间隔 --> 天数   **/
    private final static int DAYS_RLUE = 14;

    /** 日志记录器 **/
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 判断该订单对应的鱼卡 是否含有可以 补课 退单 的情况  (非 补课 既 退单)
     * 依据:每条鱼卡(最原始记录)的信息 当前时间  在计划开始时间 和  计划开始时间 + 14 天 范围内
     *     鱼卡状态为  异常、老师旷课、老师早退
     * @param orderId
     * @return
     */
    public JsonResultModel judgeCanBackOrderInfo(Long orderId){
        logger.info("requestOrderId={}",orderId);
        List<WorkOrder> workOrderUseList = null;
        Map<String,Object> mapMessage = Maps.newHashMap();
        mapMessage.put("orderId",orderId);
        boolean flag = true;

        flag =getNeedaddCourseWorkOrderFlag(orderId);

        /** 含有补课的鱼卡 **/
        if(flag){
            mapMessage.put("code",1);
            mapMessage.put("msg","有需要补课的鱼卡，退单前请先补课");
        }else {
            workOrderUseList =  getBackWorkOrders(orderId);
            Map<String,Object> detail = Maps.newHashMap();
            if(null!=workOrderUseList && workOrderUseList.size()>0){
                mapMessage.put("code",0);
                mapMessage.put("msg","");

                int teacher_c=0 ,teacher_f =0;//国内教师   外国教师
                for (WorkOrder workOrder: workOrderUseList){

                    if(TeachingType.WAIJIAO.getCode() ==  courseType2TeachingTypeService.courseType2TeachingType ( workOrder.getCourseType().toUpperCase()) ) {
                        teacher_f++;
                    }else {
                        teacher_c++;
                    }
                }
                detail.put("teacher_c",teacher_c);
                detail.put("teacher_f",teacher_f);
                mapMessage.put("teacherdetail",detail);
            }else {
                mapMessage.put("code",0); //
                mapMessage.put("msg","不含有进行退单的课程");
                detail.put("teacher_c",0);
                detail.put("teacher_f",0);
                mapMessage.put("teacherdetail",detail);
            }
        }

        return JsonResultModel.newJsonResultModel(mapMessage);
    }


    /**
     * 根据订单id进行退单
     * @param orderId
     * @return
     */

    @Transactional
    public JsonResultModel  backForOrderByOrderId(Long orderId) throws Exception{
        logger.info("requestOrderId={}",orderId);
        Map<String,Object>  mapMessage = Maps.newHashMap();

        List<WorkOrder> workOrderUseList = this.getBackWorkOrders(orderId);

        if(null == workOrderUseList || workOrderUseList.size() <1){
            mapMessage.put("code",1);
            mapMessage.put("msg","不含有退单的课程");
            return JsonResultModel.newJsonResultModel(mapMessage);
        }else {
            workOrderUseList.stream().forEach(workOrder -> {
                workOrder.setUpdateTime(new Date());
                workOrder.setStatus(FishCardStatusEnum.FISHCARD_CANCELED.getCode());//退课
                workOrder.setTeacherId(null);
                workOrder.setTeacherName(null);
                logger.info("退单发送向教学中心发送 解除师生关系workorderid-->{}",workOrder.getId());

                        // 向教学中心发送 解除师生关系??????????????????????
                        logger.info("退单解除师生关系鱼卡id{}",workOrder.getId());
                        courseOnlineRequester.releaseGroup(workOrder);

                // 释放教师资源
                logger.info("退单发送释放教师资源消息workorderid-->{}",workOrder.getId());
                teacherStudentRequester.releaseTeacher(workOrder);

            });
        }

        // 更新服务  service
        Service service  = workOrderUseList.get(0).getService();
        service.setUpdateTime(new Date());
        service.setAmount( (service.getAmount() - workOrderUseList.size())<0 ?0: (service.getAmount() - workOrderUseList.size()));// 剩余课程设为 0

        if(service.getAmount()<=0){
            // 向订单系统发送退单消息
            serveService.notifyOrderUpdateStatus(orderId,FishCardStatusEnum.FISHCARD_CANCELED.getCode());
        }
        serviceJpaRepository.save(service);

        // 更新鱼卡
        backOrderService.save(workOrderUseList);

        // 记录日志
        workOrderLogService.batchSaveWorkOrderLogs(workOrderUseList);

        // 需要考虑推荐课程的接口调用 ?????????????????????????????????????????????????????????????

        mapMessage.put("code",0);
        mapMessage.put("msg","SUCCESS");
        return JsonResultModel.newJsonResultModel(mapMessage);
    }


    /**
     * 获取退单详情
     * @param orderId
     * @return 中教数量
     *         外教数量
     */
    public JsonResultModel getBackOrderDetail(Long orderId){
        logger.info("requestOrderId={}",orderId);
        Map<String,Object> mapInfo = Maps.newHashMap();
        List<WorkOrder>  backWorkOrderList =  backOrderService.findByOrderIdAndStatus(orderId,FishCardStatusEnum.FISHCARD_CANCELED.getCode() );
        int teacher_c =0,teacher_f=0;
        if(null != backWorkOrderList && backWorkOrderList.size()>0){

            for(WorkOrder workOrder : backWorkOrderList){
                if(TeachingType.WAIJIAO.getCode() ==  courseType2TeachingTypeService.courseType2TeachingType ( workOrder.getCourseType().toUpperCase()) ) {
                    teacher_f +=1;
                }else {
                    teacher_c +=1;
                }
            }

        }

        mapInfo.put("teacher_c",teacher_c);
        mapInfo.put("teacher_f",teacher_f);

        return JsonResultModel.newJsonResultModel(mapInfo);
    }


    /**
     * 获取可以退卡的工单
     *   鱼卡 :计划开始日期 在明天凌晨之后  并且状态不是退课状态的鱼卡
     * @param orderId
     * @return
     */
    private List<WorkOrder>  getBackWorkOrders(Long orderId){
        // 1 获取所有的该订单对应的鱼卡
        List<WorkOrder> workOrderList = backOrderService.findByOrderId(orderId);
        List<WorkOrder> workOrderUseList = Lists.newArrayList();
        if(null!=workOrderList && workOrderList.size()>0){
            workOrderList.stream().forEach(workOrder -> {
                if(workOrder.getStartTime().after(  DateUtil.getTheTomrrowLC(new Date()))   &&  ( FishCardStatusEnum.FISHCARD_CANCELED.getCode() !=workOrder.getStatus()) ){
                    workOrderUseList.add(workOrder);
                }
            });
            return workOrderUseList;
        }
        return null;
    }

    /**
     * 判断 是否含有能够补课的 鱼卡
     * @param orderId
     * @return
     */
    private boolean  getNeedaddCourseWorkOrderFlag(Long orderId){
        // 1 获取所有的该订单对应的鱼卡
        List<WorkOrder> workOrderList = backOrderService.findByOrderId(orderId);

        if(null == workOrderList || workOrderList.size()<1){
            return false;
        }

        List<WorkOrder> workOrderListLasteOne = Lists.newArrayList();//过滤后的有效鱼卡信息

        // 1)去除   parent_id 不含有  鱼卡id 的记录
       for ( WorkOrder vow: workOrderList){
           boolean f = true;
           for(WorkOrder von :workOrderList){
               if(vow.getId() .equals(von.getParentId())){
                   f=false;
                   break;
               }
           }
           if(f){
               workOrderListLasteOne.add(vow);
           }
       }

        if(null == workOrderListLasteOne || workOrderListLasteOne.size()<1){
            return false;
        }

        boolean flag = false;
        // 2 循环该订单所对应的鱼卡  如果含有任何一条 不符合条件的数据 返回异常信息
        for (WorkOrder workOrder: workOrderListLasteOne){
            //1)  判断时间是否在时间范围内
            if( judgeInTheScopeTimes(workOrder, getFirstFishCard(workOrder,workOrderList))){
                //2)   判断鱼卡状态
                if( judeWorkOrderState(workOrder)){
                    flag =true;
                    break;
                }
            }

        }
        return flag;
    }

    /**
     * 在存在排课的情况下:
     *  获取该鱼卡的首次记录
     * @param workOrder
     * @param workOrderList
     * @return
     */
    private WorkOrder getFirstFishCard(WorkOrder workOrder,List<WorkOrder> workOrderList){
        if(workOrder.getParentId()==null || 0L == workOrder.getParentId())
            return workOrder;
        for(WorkOrder wo : workOrderList){
            if(wo.getId() .equals( workOrder.getParentId() )){
                return getFirstFishCard(wo,workOrderList);
            }
        }

        logger.info("数据异常情况,请尽快修复 订单id {}",workOrder.getOrderId());
        return null;
    }

    /**
     * 判断  状态
     *          教师旷课   FishCardStatusEnum.TEACHER_ABSENT
     *          老师早退   FishCardStatusEnum.TEACHER_LEAVE_EARLY
     *          系统异常   FishCardStatusEnum.EXCEPTION
     * @param workOrder
     * @return
     */
    private boolean judeWorkOrderState(WorkOrder workOrder){
        Integer state  = workOrder.getStatus();

        if(
                FishCardStatusEnum.TEACHER_ABSENT.getCode()  == state
                        ||
                        FishCardStatusEnum.TEACHER_LEAVE_EARLY.getCode()  == state
                        ||
                        FishCardStatusEnum.EXCEPTION.getCode() == state

                ){

            return  true;
        }else{
            return false;
        }
    }

    /**
     * 判断计划开始日期是否在该时间段内   当前 ~  当前+14天 内
     *    最近记录必须在当前时间之前
     * @param workOrdernew 首次记录
     * @param workOrderfirst 最新记录
     * @return
     */
    private boolean  judgeInTheScopeTimes(WorkOrder workOrdernew,     WorkOrder  workOrderfirst){
        if( null != workOrdernew.getStartTime() && null != workOrderfirst.getStartTime()){

            Date  planStudyDate      = workOrderfirst.getStartTime();

            Date  currentDate = new Date();

            Date  currentBefore14Days         = DateUtil.getBeforeDays(currentDate,DAYS_RLUE);

            if(planStudyDate.after(currentBefore14Days) && planStudyDate.before(currentDate)){

                if(workOrdernew.getStartTime().after(currentDate)){
                    return false;
                }else {
                    return true;
                }
            } else {
                return  false;
            }
        }else {
            return false;
        }

    }
}
