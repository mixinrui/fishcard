package com.boxfishedu.workorder.servicex.fishcardcenter;

import com.alibaba.fastjson.JSONObject;
import com.boxfishedu.card.bean.CourseTypeEnum;
import com.boxfishedu.online.order.entity.TeacherForm;
import com.boxfishedu.workorder.common.bean.MessagePushTypeEnum;
import com.boxfishedu.workorder.common.bean.TeachingType;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.WorkOrderConstant;
import com.boxfishedu.workorder.entity.mysql.HolidayDay;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.entity.mysql.WorkOrderGrab;
import com.boxfishedu.workorder.entity.mysql.WorkOrderGrabHistory;
import com.boxfishedu.workorder.requester.TeacherStudentRequester;
import com.boxfishedu.workorder.service.HolidayDayService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.fishcardcenter.FishCardQueryService;
import com.boxfishedu.workorder.service.graborder.MakeWorkOrderService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.servicex.bean.WorkOrderView;
import com.boxfishedu.workorder.web.param.FishCardFilterParam;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 自动确认状态订单
 * <p/>
 * Created by jiaozijun on 16/7/11.
 */
@Component
public class AutuConfirmFishCardServiceX {

    //本地异常日志记录对象
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private HolidayDayService  holidayDayService;

    @Autowired
    private ThreadPoolManager threadPoolManager;

    @Autowired
    private FishCardQueryService fishCardQueryService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private MakeUpLessionServiceX makeUpLessionServiceX;


    /**
     * 自动 更新鱼卡状态
     */
    public void autoConfirmFishCard(){
        threadPoolManager.execute(new Thread(() -> {
                    this.autoConfirm();
                })
        );
    }


    @Transactional
    public void autoConfirm(){
        logger.info("autoConfirm :begin");

        //1  查询需要自动确认的鱼卡
        FishCardFilterParam fishCardFilterParam =new FishCardFilterParam();
        fishCardFilterParam.setConfirmFlag("1");
        Date date = new Date();
        date =DateUtil.addMinutes(date,-(30));
        logger.info("autoConfirm : date [{}]",date);
        fishCardFilterParam.setEndDate(DateUtil.Date2String(date));
        fishCardFilterParam.setStatuses("40,41,42,50,51,52,53,60");

        Pageable pageable = new QPageRequest(0,999999);
        workOrderService.processDateParam(fishCardFilterParam);
        List<WorkOrder> workOrderList=fishCardQueryService.filterFishCards(fishCardFilterParam,pageable);

        if(CollectionUtils.isEmpty(workOrderList)){
            logger.info("autoConfirm : 没有匹配到应该确认状态的订单");
            return;
        }else {
            logger.info("autoConfirm : 鱼卡数量 [{}]",workOrderList.size());
        }



        //2  获取节假日
        List<HolidayDay> holidayDays  = holidayDayService.findAll();


        List<WorkOrder> workOrderListchange = Lists.newArrayList();

        //3  鱼卡自动确认状态(鱼卡结束48小时后(不含节假日),进行鱼卡状态状态)
        for(WorkOrder wo:workOrderList){
            Date baseDate = DateUtil.addMinutes(wo.getEndTime(),60*24*2);
            if(CollectionUtils.isEmpty(holidayDays)){

                // 如果课程结束 48小时 后 ,在现在时间之前,鱼卡进行自动确认
                if(baseDate.before(new Date())){


                    wo.setConfirmFlag("0");
                    workOrderListchange.add(wo);
                }

            }else {
                Date endHoliday = getHoliday(holidayDays,wo.getEndTime(),baseDate);
                if( endHoliday.before(new Date())){
                    wo.setConfirmFlag("0");
                    workOrderListchange.add(wo);
                }

            }
        }

        logger.info("autoConfirm  需要自动完成的鱼卡数量 [{}]",workOrderListchange.size());
        if(!CollectionUtils.isEmpty(workOrderListchange)){
            makeUpLessionServiceX.fishcardStatusRechargeChangeLast(workOrderListchange);
        }

        logger.info("autoConfirm :end");
    }

    /**
     * 获取期间假期
     * @param holidays
     * @param endTime1
     * @param endTime2
     * @return
     */
    public Date getHoliday(List<HolidayDay> holidays,Date endTime1,Date endTime2){
        Date confirmDate = null;// 能够自动确认的日期
        for (HolidayDay holidayDay:holidays){
            //1  鱼卡时间段 在 holiday之间
            if(endTime1.after(holidayDay.getStartTime()) && endTime2.before(holidayDay.getEndTime())){
                confirmDate = DateUtil.addMinutes(holidayDay.getEndTime(),60*24*2);
            }
            //2  鱼卡时间段 包含holiday
            if(holidayDay.getStartTime().after(endTime1) && holidayDay.getEndTime().before(endTime2)){
                confirmDate = DateUtil.addMinutes(endTime2,60*24*2);
            }
            //3  鱼卡时间段 holiday区间包含 开始时间
            if(holidayDay.getEndTime().after(endTime1) && holidayDay.getEndTime().before(endTime2) && holidayDay.getStartTime().before(endTime1)){
                confirmDate = DateUtil.addMinutes(endTime2,DateUtil.getBetweenMinus(endTime1,holidayDay.getEndTime()));
            }
            //4  鱼卡时间段 holiday区间包含 结束时间
            if(holidayDay.getStartTime().after(endTime1) && holidayDay.getStartTime().before(endTime2) &&  holidayDay.getEndTime().after(endTime2)){
                confirmDate = DateUtil.addMinutes(holidayDay.getEndTime(),  DateUtil.getBetweenMinus(holidayDay.getStartTime(),endTime2) );
            }
            if(null != confirmDate){
                return confirmDate;
            }
        }

        return endTime2;

    }
}
