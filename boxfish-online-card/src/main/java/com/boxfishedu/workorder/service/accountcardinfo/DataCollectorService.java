package com.boxfishedu.workorder.service.accountcardinfo;

import com.boxfishedu.workorder.common.bean.AccountCourseBean;
import com.boxfishedu.workorder.common.bean.ComboTypeEnum;
import com.boxfishedu.workorder.common.bean.TutorTypeEnum;
import com.boxfishedu.workorder.common.threadpool.ThreadPoolManager;
import com.boxfishedu.workorder.dao.mongo.ScheduleCourseInfoMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.ScheduleCourseInfo;
import com.boxfishedu.workorder.entity.mysql.Service;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.ScheduleCourseInfoService;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.boxfishedu.workorder.web.param.Student2TeacherCommentParam;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
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

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    public Integer getUnSelectedAmount(Long studentId) {
        return 0;
    }

    public WorkOrder getWorkOrderToStart(List<WorkOrder> workOrders) {
        if (CollectionUtils.isEmpty(workOrders)) {
            return null;
        }
        Collections.sort(workOrders, new SortByStartTime());
        return workOrders.get(0);
    }

    public Integer selectedLeftNum(List<WorkOrder> workOrders) {
        return CollectionUtils.isEmpty(workOrders)?0: workOrders.size();
    }

    public Integer getChineseUnselectedServices(Long studentId) {
        List<Service> overAllServices = serveService.getUnselectedService(studentId, ComboTypeEnum.OVERALL, 0);
        List<Service> exchangeChineseServices = serveService.getUnselectedService(studentId, ComboTypeEnum.EXCHANGE, TutorTypeEnum.CN ,0);
        return (CollectionUtils.isEmpty(overAllServices) ? 0 : overAllServices.size())
                + (CollectionUtils.isEmpty(exchangeChineseServices) ? 0 : exchangeChineseServices.size());
    }

    public Integer getForeignUnselectedServices(Long studentId) {
        List<Service> communctionServices = serveService.getUnselectedService(studentId, ComboTypeEnum.FOREIGN, 0);
        List<Service> finalDreamServices = serveService.getUnselectedService(studentId, ComboTypeEnum.CHINESE, 0);
        List<Service> exchangeFrnServices = serveService.getUnselectedService(studentId, ComboTypeEnum.EXCHANGE, TutorTypeEnum.FRN, 0);
        return (CollectionUtils.isEmpty(communctionServices) ? 0 : communctionServices.size())
                + (CollectionUtils.isEmpty(finalDreamServices) ? 0 : finalDreamServices.size())
                + (CollectionUtils.isEmpty(exchangeFrnServices) ? 0 : exchangeFrnServices.size());
    }

    public List<WorkOrder> getChineseSelectedLeftWorkOrders(Long studentId) {
        //中教:核心素养+金币换课中教
        List<WorkOrder> overallCards = workOrderService.getSelectedLeftAmount(studentId, ComboTypeEnum.OVERALL);
        List<WorkOrder> exchangeChineses = workOrderService.getSelectedLeftAmount(studentId, ComboTypeEnum.EXCHANGE, com.boxfishedu.card.bean.TeachingType.ZHONGJIAO);

        List<WorkOrder> chineseCards = Lists.newArrayList();

        chineseCards.addAll(overallCards);
        chineseCards.addAll(exchangeChineses);

        return chineseCards;
    }

    public List<WorkOrder> getForeignSelectedLeftWorkOrders(Long studentId) {
        //外教+金币换课外教+终极梦想
        List<WorkOrder> communictionCards = workOrderService.getSelectedLeftAmount(studentId, ComboTypeEnum.FOREIGN);
        List<WorkOrder> finalDreamCards = workOrderService.getSelectedLeftAmount(studentId, ComboTypeEnum.CHINESE);
        List<WorkOrder> exchangeForeigns = workOrderService.getSelectedLeftAmount(studentId, ComboTypeEnum.EXCHANGE, com.boxfishedu.card.bean.TeachingType.WAIJIAO);

        List<WorkOrder> foreignCards = Lists.newArrayList();
        foreignCards.addAll(communictionCards);
        foreignCards.addAll(finalDreamCards);
        foreignCards.addAll(exchangeForeigns);

        return foreignCards;

    }

    public WorkOrder getCardToStart(Long studentId) {
        return workOrderService.getCardToStart(studentId);
    }

    public ScheduleCourseInfo getCourseByWorkOrder(Long workOrderId) {
        return scheduleCourseInfoMorphiaRepository.queryByWorkId(workOrderId);
    }

    public AccountCourseBean updateForeignItem(Long studentId){
        AccountCourseBean accountCourseBean=new AccountCourseBean();
        List<WorkOrder> selectedLeftWorkOrders = getForeignSelectedLeftWorkOrders(studentId);
        int leftAmount=getForeignUnselectedServices(studentId)+selectedLeftNum(selectedLeftWorkOrders);
        accountCourseBean.setLeftAmount(leftAmount);
        WorkOrder latestWorkOrder=getWorkOrderToStart(selectedLeftWorkOrders);
        if(null==latestWorkOrder){
            accountCourseBean.setCourseInfo(null);
            return accountCourseBean;
        }
        ScheduleCourseInfo scheduleCourseInfo= scheduleCourseInfoService.queryByWorkId(latestWorkOrder.getId());

        accountCourseBean.setCourseInfo(scheduleCourseAdapter(scheduleCourseInfo,latestWorkOrder));

        return accountCourseBean;
    }

    public AccountCourseBean updateChineseItem(Long studentId){
        AccountCourseBean accountCourseBean=new AccountCourseBean();
        List<WorkOrder> selectedWorkOrders=getChineseSelectedLeftWorkOrders(studentId);
        int leftAmount=getChineseUnselectedServices(studentId)+selectedLeftNum(selectedWorkOrders);
        accountCourseBean.setLeftAmount(leftAmount);
        WorkOrder latestWorkOrder=getWorkOrderToStart(selectedWorkOrders);
        if(null==latestWorkOrder){
            accountCourseBean.setCourseInfo(null);
            return accountCourseBean;
        }
        ScheduleCourseInfo scheduleCourseInfo= scheduleCourseInfoService.queryByWorkId(latestWorkOrder.getId());

        accountCourseBean.setCourseInfo(scheduleCourseAdapter(scheduleCourseInfo,latestWorkOrder));

        return accountCourseBean;
    }

    public void updateBothChnAndFnItemAsync(Long studentId){
        threadPoolManager.execute(new Thread(()->this.updateBothChnAndFnItem(studentId)));
    }

    public void updateBothChnAndFnItem(Long studentId){
        try {
            logger.error("@updateBothChnAndFnItem#begin用户[{}]更新首页信息失败",studentId);
            AccountCourseBean chineseCourseBean=updateChineseItem(studentId);
            AccountCourseBean foreignCourseBean=updateForeignItem(studentId);
            accountCardInfoService.saveOrUpdateChAndFrn(studentId,chineseCourseBean,foreignCourseBean);
        }
        catch (Exception ex){
            logger.error("@updateBothChnAndFnItem#exception用户[{}]更新首页信息失败",studentId);
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


    class SortByStartTime implements Comparator {
        public int compare(Object o1, Object o2) {
            WorkOrder s1 = (WorkOrder) o1;
            WorkOrder s2 = (WorkOrder) o2;
            if (s1.getStartTime().after(s2.getStartTime()))
                return 1;
            return 0;
        }
    }


}
