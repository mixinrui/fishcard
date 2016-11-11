package com.boxfishedu.workorder.servicex.instantclass.classdatagenerator;

import com.boxfishedu.workorder.common.bean.FishCardStatusEnum;
import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.config.UrlConf;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.RecommandCourseRequester;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.service.accountcardinfo.DataCollectorService;
import com.boxfishedu.workorder.service.workorderlog.WorkOrderLogService;
import com.boxfishedu.workorder.servicex.studentrelated.TimePickerServiceXV1;
import com.boxfishedu.workorder.servicex.studentrelated.selectmode.SelectMode;
import com.boxfishedu.workorder.servicex.studentrelated.selectmode.TemplateSelectMode;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import com.google.common.collect.Maps;
import com.sun.javafx.collections.MappingChange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Created by hucl on 16/11/9.
 */
@Component
public class OtherEntranceDataGenerator implements IClassDataGenerator{

    @Autowired
    private TimePickerServiceXV1 timePickerServiceXV1;

    @Autowired
    private TemplateSelectMode templateSelectMode;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private RecommandCourseRequester recommandCourseRequester;

    @Autowired
    private WorkOrderLogService workOrderLogService;

    @Autowired
    private DataCollectorService dataCollectorService;


    @Override
    public List<WorkOrder> initCardAndSchedule(InstantClassCard instantClassCard) {
        TimeSlotParam timeSlotParam=TimeSlotParam.instantCard2TimeParam(instantClassCard);
        //根据订单id,type获取对应的服务
        List<Service> serviceList = timePickerServiceXV1.ensureConvertOver(timeSlotParam);
        WorkOrder workOrder=templateSelectMode
                .initWorkOrder(serviceList.get(0),1,instantClassCard.getSlotId().intValue());

        LocalDateTime localDateTime=LocalDateTime.now(ZoneId.systemDefault());
        workOrder.setStartTime(DateUtil.localDate2Date(localDateTime));
        workOrder.setEndTime(DateUtil.localDate2Date(localDateTime.plusMinutes(25)));
        workOrder.setStudentId(instantClassCard.getStudentId());
        workOrder.setTeacherId(instantClassCard.getTeacherId());
        workOrder.setTeacherName(instantClassCard.getTeacherName());
        workOrder.setStatus(FishCardStatusEnum.TEACHER_ASSIGNED.getCode());
        workOrder.setClassType(ClassTypeEnum.INSTNAT.toString());

        java.util.Map<Integer, RecommandCourseView> recommandCourseViewMap = wrapCourseViewMap(instantClassCard);

        workOrderService.batchSaveCoursesIntoCard(Arrays.asList(workOrder),recommandCourseViewMap);
        //入库,workorder和coursechedule的插入放入一个事务中,保证数据的一致性
        List<CourseSchedule> courseSchedules=workOrderService.persistCardInfos(
                serviceList.get(0),Arrays.asList(workOrder),recommandCourseViewMap);

        workOrderLogService.saveWorkOrderLog(workOrder,"生成实时上课鱼卡");

        dataCollectorService.updateBothChnAndFnItemAsync(serviceList.get(0).getStudentId());

        // 通知其他模块
        timePickerServiceXV1.notifyOtherModules(Arrays.asList(workOrder), serviceList.get(0));

        instantClassCard.setWorkorderId(workOrder.getId());

        return Collections.emptyList();
    }

    private Map<Integer, RecommandCourseView> wrapCourseViewMap(InstantClassCard instantClassCard) {
        RecommandCourseView recommandCourseView=recommandCourseRequester.getCourseViewDetail(instantClassCard.getCourseId());
        Map<Integer,RecommandCourseView> recommandCourseViewMap= Maps.newHashMap();
        recommandCourseViewMap.put(0,recommandCourseView);
        recommandCourseViewMap.put(1,recommandCourseView);
        return recommandCourseViewMap;
    }

    @Override
    public void initCourses(WorkOrder workOrder) {

    }



}
