package com.boxfishedu.workorder.service.graborder;

import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.WorkOrderGrabHistoryJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderGrabJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.entity.mysql.WorkOrderGrab;
import com.boxfishedu.workorder.entity.mysql.WorkOrderGrabHistory;
import com.boxfishedu.workorder.service.base.BaseService;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 抢单服务层
 * Created by jiaozijun on 16/7/11.
 */
@Component
public class MakeWorkOrderService extends BaseService<WorkOrderGrab, WorkOrderGrabJpaRepository, Long> {

    //本地异常日志记录对象
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    private WorkOrderGrabJpaRepository workOrderGrabJpaRepository;

    @Autowired
    private WorkOrderGrabHistoryJpaRepository workOrderGrabHistoryJpaRepository;

    /**
     * 获取未来两天内 为匹配教师的鱼卡信息
     * @return
     */
    public List<WorkOrder> findByTeacherIdAndStartTimeBetweenOrderByStartTime(){
        Date begin = DateUtil.parseTime( DateUtil.getBeforeDays(  new Date(),-1),0);
        Date end = DateUtil.parseTime( DateUtil.getBeforeDays(  new Date(),-2),1);
       return workOrderJpaRepository.findByTeacherIdAndStartTimeBetweenOrderByStartTime(0L,begin,end);
    }


    /**
     * 获取未来两天内 已经匹配教师的鱼卡信息
     * @return
     */
    public List<WorkOrder> findWorkOrderContainTeachers(){
        Date begin = DateUtil.parseTime( DateUtil.getBeforeDays(  new Date(),-1),0);
        Date end = DateUtil.parseTime( DateUtil.getBeforeDays(  new Date(),-2),1);
        return workOrderJpaRepository.findByTeacherIdGreaterThanAndStartTimeBetween(0L,begin,end);
    }


    /**
     * 保存
     * @param map
     */
    @Transactional
    public void saveCurrentworkOrderMap(Map<Long,List<WorkOrder>> map){
        List<WorkOrderGrab> wograb = Lists.newArrayList();
        for(Long key :map.keySet()){
            List<WorkOrder> woList = map.get(key);
            for (WorkOrder wo:woList){
                WorkOrderGrab wg = new WorkOrderGrab();
                wg.setTeacherId(key);
                wg.setUpdateTime(new Date());
                wg.setCreateTime(new Date());
                wg.setStartTime(wo.getStartTime());
                wg.setWorkorderId(wo.getId());
                wg.setFlag("0");
                wograb.add(wg);
            }
        }

        workOrderGrabJpaRepository.save(wograb);
        logger.info("insert WorkOrderGrab 成功");

    }


    /**
     * 获取今天之前的数据
     * @return
     */
    public List<WorkOrderGrab>   getGrabDataBeforeDay(){
        //今天的  00:00:00
        Date date = DateUtil.parseTime( DateUtil.getBeforeDays(  new Date(),0),0);
        return  workOrderGrabJpaRepository.findByCreateTimeLessThan(new Date());
    }

    public void  deleteGrabData(List<WorkOrderGrab> list){
        logger.info("删除grab数据");
        workOrderGrabJpaRepository.delete(list);
    }

    public void initGrabOrderHistory(List<WorkOrderGrabHistory> list){
        logger.info("新增grabHistory数据");
        workOrderGrabHistoryJpaRepository.save(list);
    }





}