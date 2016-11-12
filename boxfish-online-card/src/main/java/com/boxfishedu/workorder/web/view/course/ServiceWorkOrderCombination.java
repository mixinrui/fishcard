package com.boxfishedu.workorder.web.view.course;

import com.boxfishedu.mall.enums.TutorType;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.CourseType2TeachingTypeService;
import com.boxfishedu.workorder.web.param.TimeSlotParam;
import org.apache.http.util.Asserts;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by LuoLiBing on 16/8/17.
 */
public class ServiceWorkOrderCombination {

    private Service service;

    private List<WorkOrder> workOrderList;

    private Map<Integer, RecommandCourseView> recommendCourseViewMap;

    private List<CourseSchedule> courseScheduleList;

    public ServiceWorkOrderCombination(Service service) {
        this.service = service;
    }

    public Service getService() {
        return service;
    }

    public List<WorkOrder> getWorkOrderList() {
        return workOrderList;
    }

    /**
     * service验证
     * @param timeSlotParam
     * @param consumer
     * @return
     */
    public ServiceWorkOrderCombination validateService(TimeSlotParam timeSlotParam, BiConsumer<TimeSlotParam, Service> consumer) {
        Asserts.notNull(service, "对应的服务不存在");
        consumer.accept(timeSlotParam, service);
        return this;
    }

    /**
     * 唯一验证
     * @param timeSlotParam
     * @param consumer
     * @return
     */
    public ServiceWorkOrderCombination validateUniqueCourse(TimeSlotParam timeSlotParam, BiConsumer<TimeSlotParam, Service> consumer) {
        Asserts.notNull(service, "对应的服务不存在");
        consumer.accept(timeSlotParam, service);
        return this;
    }

    /**
     * 生成工单
     * @param timeSlotParam
     * @param action
     */
    public ServiceWorkOrderCombination generateWorkOrders(
            TimeSlotParam timeSlotParam, BiFunction<TimeSlotParam, Service, List<WorkOrder>> action) {
        Asserts.notNull(service, "对应的服务不存在");
        this.workOrderList = action.apply(timeSlotParam, this.service);
        return this;
    }

    /**
     * 课程推荐
     * @param action
     * @return
     */
    public ServiceWorkOrderCombination recommendCourses(Function<List<WorkOrder>, Map<Integer, RecommandCourseView>> action) {
        Asserts.notNull(workOrderList, "没有生成工单");
        this.recommendCourseViewMap = action.apply(workOrderList);
        for(WorkOrder workOrder : workOrderList) {
            RecommandCourseView recommandCourseView = recommendCourseViewMap.get(workOrder.getSeqNum());
            workOrder.initCourseInfo(recommandCourseView);
            workOrder.setSkuId( CourseType2TeachingTypeService.courseType2TeachingType2(
                    recommandCourseView.getCourseType(), TutorType.resolve(workOrder.getService().getTutorType())));
        }
        return this;
    }

    /**
     * 生成课表信息
     * @param action
     * @return
     */
    public ServiceWorkOrderCombination generateCourseSchedules(
            Bi3Function<Service,List<WorkOrder>,Map<Integer, RecommandCourseView>, List<CourseSchedule>> action) {
        this.courseScheduleList = action.apply(service, workOrderList, recommendCourseViewMap);
        return this;
    }

    /**
     * 分配教师
     * @return
     */
    public ServiceWorkOrderCombination assignTeacher(BiConsumer<Service, List<CourseSchedule>> consumer) {
        consumer.accept(service, courseScheduleList);
        return this;
    }

    /**
     * 发送通知给其他木块
     * @param consumer
     * @return
     */
    public ServiceWorkOrderCombination notifyOthers(BiConsumer<List<WorkOrder>, Service> consumer) {
        consumer.accept(workOrderList, service);
        return this;
    }

    /**
     * 平摊成WorkOrderList
     * @param serviceWorkOrderCombinationList
     * @return
     */
    public static List<WorkOrder> flatMapToWorkOrder(List<ServiceWorkOrderCombination> serviceWorkOrderCombinationList) {
        return serviceWorkOrderCombinationList.stream()
                .map(ServiceWorkOrderCombination::getWorkOrderList)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
