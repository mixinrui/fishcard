package com.boxfishedu.workorder.servicex.smallclass;

import com.boxfishedu.workorder.common.bean.instanclass.ClassTypeEnum;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.dao.jpa.WorkOrderJpaRepository;
import com.boxfishedu.workorder.dao.mongo.SmallStudentsRelationMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.SmallClassStudentsRelation;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.WorkOrderService;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by hucl on 17/1/16.
 */
@Service
public class SmallClassTimerServiceX {
    @Autowired
    private SmallStudentsRelationMorphiaRepository classelationMorphiaRepository;

    @Autowired
    private WorkOrderJpaRepository workOrderJpaRepository;

    @Autowired
    private SmallClassJpaRepository smallClassJpaRepository;

    //创建学生小班课的上课关系
    public void buildSmallCLassRelations() {
        List<SmallClass> smallClasses = getSmallClasses();
        List<SmallClassStudentsRelation> relations = Lists.newArrayList();

        if (CollectionUtils.isEmpty(smallClasses)) {
            return;
        }

        Map<Long, List<WorkOrder>> smallClasCards
                = smallClasses.parallelStream()
                              .collect(Collectors.toMap(SmallClass::getId, this::getCardsToBuildRelation));

        smallClasCards.forEach((k, v) -> v.forEach(master -> v.forEach(partner -> {
            if (!Objects.equals(master.getStudentId(), partner.getStudentId())) {
                SmallClassStudentsRelation smallClassStudentsRelation
                        = new SmallClassStudentsRelation();
                smallClassStudentsRelation.setSmallClassId(k);
                smallClassStudentsRelation.setMaster(master.getStudentId());
                smallClassStudentsRelation.setPartner(partner.getStudentId());
                smallClassStudentsRelation.setCreateTime(new Date());

                relations.add(smallClassStudentsRelation);
            }
        })));

        //保存relations
        classelationMorphiaRepository.save(relations);

    }

    private List<SmallClass> getSmallClasses() {
        Date deadEndTime = DateUtil.localDate2Date(LocalDateTime.now().minusMinutes(30));
        return smallClassJpaRepository
                .findByClassTypeAndStartTimeLessThan(ClassTypeEnum.SMALL.name(), deadEndTime);
    }

    private List<WorkOrder> getCardsToBuildRelation(SmallClass smallClass) {
        return workOrderJpaRepository.findBySmallClassId(smallClass.getId());
    }
}
