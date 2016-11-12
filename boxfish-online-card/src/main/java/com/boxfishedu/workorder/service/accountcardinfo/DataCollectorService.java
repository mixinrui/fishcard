package com.boxfishedu.workorder.service.accountcardinfo;

import com.boxfishedu.workorder.common.bean.AccountCourseBean;
import com.boxfishedu.workorder.common.bean.ComboTypeEnum;
import com.boxfishedu.workorder.common.bean.TutorTypeEnum;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.dao.mongo.ScheduleCourseInfoMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.ScheduleCourseInfo;
import com.boxfishedu.workorder.entity.mysql.CourseSchedule;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.CourseScheduleService;
import com.boxfishedu.workorder.service.ScheduleCourseInfoService;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.servicex.timer.SingleRecommendHandler;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by hucl on 16/9/26.
 */
@Component
public class DataCollectorService {
    @Autowired
    private ServeService serveService;

    @Autowired
    private WorkOrderService workOrderService;

    @Autowired
    private ScheduleCourseInfoMorphiaRepository scheduleCourseInfoMorphiaRepository;

    @Autowired
    private AccountCardInfoService accountCardInfoService;

    @Autowired
    private ScheduleCourseInfoService scheduleCourseInfoService;

    @Autowired
    private ThreadPoolManager threadPoolManager;

    @Autowired
    private CourseScheduleService courseScheduleService;

    @Autowired
    private SingleRecommendHandler singleRecommendHandler;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public WorkOrder getWorkOrderToStart(List<WorkOrder> workOrders) {
        if (CollectionUtils.isEmpty(workOrders)) {
            return null;
        }
        workOrders.sort((o1, o2) -> o1.getStartTime().after(o2.getStartTime()) ? 1 : -1);
        logger.debug("@getWorkOrderToStart#sort#end#[{}]", workOrders);
        return workOrders.get(0);
    }

    public Integer selectedLeftNum(List<WorkOrder> workOrders) {
        return CollectionUtils.isEmpty(workOrders) ? 0 : workOrders.size();
    }

    private Integer getAmountFromServices(List<Service> services) {
        return services.stream().filter(service -> service.getAmount() > 0).mapToInt(Service::getAmount).sum();
    }

    public Integer getChineseUnselectedServices(Long studentId) {
        List<Service> overAllServices = serveService.getUnselectedService(studentId, ComboTypeEnum.OVERALL, 0);
        List<Service> otherChineseServices = serveService.getUnselectedService(studentId,
                Lists.newArrayList(ComboTypeEnum.EXCHANGE,ComboTypeEnum.INTELLIGENT,ComboTypeEnum.EXPERIENCE), TutorTypeEnum.CN, 0);

        return (CollectionUtils.isEmpty(overAllServices) ? 0 : this.getAmountFromServices(overAllServices))
                + (CollectionUtils.isEmpty(otherChineseServices) ? 0 : this.getAmountFromServices(otherChineseServices));
    }

    public Integer getForeignUnselectedServices(Long studentId) {
        List<Service> communctionServices = serveService.getUnselectedService(studentId, Lists.newArrayList(ComboTypeEnum.FOREIGN,ComboTypeEnum.CHINESE), 0);
        List<Service> otherFrnServices = serveService.getUnselectedService(studentId,
                Lists.newArrayList(ComboTypeEnum.EXCHANGE,ComboTypeEnum.INTELLIGENT,ComboTypeEnum.EXPERIENCE), TutorTypeEnum.FRN, 0);
        return (CollectionUtils.isEmpty(communctionServices) ? 0 : this.getAmountFromServices(communctionServices))
                + (CollectionUtils.isEmpty(otherFrnServices) ? 0 : this.getAmountFromServices(otherFrnServices));
    }

    private List<WorkOrder> getOtherCards(com.boxfishedu.card.bean.TeachingType teachingType,Long studentId){
        switch (teachingType){
            case ZHONGJIAO:
                return workOrderService.getSelectedLeftAmount(studentId, Lists.newArrayList(ComboTypeEnum.INTELLIGENT,
                        ComboTypeEnum.EXCHANGE,ComboTypeEnum.EXPERIENCE), com.boxfishedu.card.bean.TeachingType.ZHONGJIAO);
            case WAIJIAO:
                return workOrderService.getSelectedLeftAmount(studentId, Lists.newArrayList(ComboTypeEnum.INTELLIGENT,
                        ComboTypeEnum.EXCHANGE,ComboTypeEnum.EXPERIENCE), com.boxfishedu.card.bean.TeachingType.WAIJIAO);
            default: return null;
        }
    }

    public List<WorkOrder> getChineseSelectedLeftWorkOrders(Long studentId) {
        logger.debug("#getChineseSelectedLeftWorkOrders#用户[{}]", studentId);
        //中教:核心素养
        List<WorkOrder> overallCards = workOrderService.getSelectedLeftAmount(studentId, Lists.newArrayList(ComboTypeEnum.OVERALL));
        List<WorkOrder> otherChineses = getOtherCards(com.boxfishedu.card.bean.TeachingType.ZHONGJIAO,studentId);

        List<WorkOrder> chineseCards = Lists.newArrayList(overallCards);
        chineseCards.addAll(otherChineses);

        return chineseCards;
    }

    public List<WorkOrder> getForeignSelectedLeftWorkOrders(Long studentId) {
        logger.debug("#getForeignSelectedLeftWorkOrders#用户[{}]", studentId);
        //外教+金币换课外教+终极梦想
        List<WorkOrder> communictionCards = workOrderService.getSelectedLeftAmount(studentId, Lists.newArrayList(ComboTypeEnum.FOREIGN,ComboTypeEnum.CHINESE));
        List<WorkOrder> otherForeigns = getOtherCards(com.boxfishedu.card.bean.TeachingType.WAIJIAO,studentId);

        List<WorkOrder> foreignCards = Lists.newArrayList(communictionCards);
        foreignCards.addAll(otherForeigns);

        return foreignCards;

    }

    public WorkOrder getCardToStart(Long studentId) {
        return workOrderService.getCardToStart(studentId);
    }

    public ScheduleCourseInfo getCourseByWorkOrder(Long workOrderId) {
        return scheduleCourseInfoMorphiaRepository.queryByWorkId(workOrderId);
    }

    //如果最近一节课没有课程,调用课程推荐接口更新
    public void getLatestRecommandCourse(WorkOrder latestWorkOrder) {
        if (StringUtils.isEmpty(latestWorkOrder.getCourseId())) {
            CourseSchedule latestCourseSchedule = courseScheduleService.findByWorkOrderId(latestWorkOrder.getId());
            singleRecommendHandler.singleRecommend(latestWorkOrder, latestCourseSchedule);
        }
    }

    public AccountCourseBean updateForeignItem(Long studentId) {
        AccountCourseBean accountCourseBean = new AccountCourseBean();
        List<WorkOrder> selectedLeftWorkOrders = getForeignSelectedLeftWorkOrders(studentId);
        int leftAmount = getForeignUnselectedServices(studentId) + selectedLeftNum(selectedLeftWorkOrders);
        accountCourseBean.setLeftAmount(leftAmount);
        WorkOrder latestWorkOrder = getWorkOrderToStart(selectedLeftWorkOrders);
        if (null == latestWorkOrder) {
            accountCourseBean.setCourseInfo(null);
            return accountCourseBean;
        }
        this.getLatestRecommandCourse(latestWorkOrder);
        ScheduleCourseInfo scheduleCourseInfo = scheduleCourseInfoService.queryByWorkId(latestWorkOrder.getId());

        accountCourseBean.setCourseInfo(scheduleCourseAdapter(scheduleCourseInfo, latestWorkOrder));

        return accountCourseBean;
    }

    public AccountCourseBean updateChineseItem(Long studentId) {
        AccountCourseBean accountCourseBean = new AccountCourseBean();
        List<WorkOrder> selectedWorkOrders = getChineseSelectedLeftWorkOrders(studentId);
        int leftAmount = getChineseUnselectedServices(studentId) + selectedLeftNum(selectedWorkOrders);
        accountCourseBean.setLeftAmount(leftAmount);
        WorkOrder latestWorkOrder = getWorkOrderToStart(selectedWorkOrders);
        if (null == latestWorkOrder) {
            accountCourseBean.setCourseInfo(null);
            return accountCourseBean;
        }
        this.getLatestRecommandCourse(latestWorkOrder);
        ScheduleCourseInfo scheduleCourseInfo = scheduleCourseInfoService.queryByWorkId(latestWorkOrder.getId());

        accountCourseBean.setCourseInfo(scheduleCourseAdapter(scheduleCourseInfo, latestWorkOrder));

        return accountCourseBean;
    }

    public void updateBothChnAndFnItemAsync(Long studentId) {
        threadPoolManager.execute(new Thread(() -> this.updateBothChnAndFnItem(studentId)));
    }

    public void updateBothChnAndFnItemForCardId(Long fishcardId){
        updateBothChnAndFnItem(workOrderService.findOne(fishcardId).getStudentId());
    }

    public void updateBothChnAndFnItem(Long studentId) {
        try {
            logger.debug("@updateBothChnAndFnItem#begin#user[{}]更新首页信息>>>", studentId);
            AccountCourseBean chineseCourseBean = updateChineseItem(studentId);
            AccountCourseBean foreignCourseBean = updateForeignItem(studentId);
            accountCardInfoService.saveOrUpdateChAndFrn(studentId, chineseCourseBean, foreignCourseBean);
        } catch (Exception ex) {
            logger.error("@updateBothChnAndFnItem#exception#user[{}]更新首页信息失败", studentId,ex);
        }
    }

    public AccountCourseBean.CardCourseInfo scheduleCourseAdapter(ScheduleCourseInfo scheduleCourseInfo, WorkOrder workOrder) {
        AccountCourseBean.CardCourseInfo cardCourseInfo = new AccountCourseBean.CardCourseInfo();
        cardCourseInfo.setThumbnail(scheduleCourseInfo.getThumbnail());
        cardCourseInfo.setCourseId(scheduleCourseInfo.getCourseId());
        cardCourseInfo.setCourseName(scheduleCourseInfo.getName());
        cardCourseInfo.setDifficulty(scheduleCourseInfo.getDifficulty());
        cardCourseInfo.setCourseType(scheduleCourseInfo.getCourseType());
        cardCourseInfo.setIsFreeze(workOrder.getIsFreeze());
        cardCourseInfo.setStatus(workOrder.getStatus());
        cardCourseInfo.setDateInfo(workOrder.getStartTime());
        return cardCourseInfo;
    }
}
