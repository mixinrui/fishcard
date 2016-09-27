package com.boxfishedu.workorder.service.accountcardinfo;

import com.boxfishedu.workorder.common.bean.AccountCourseBean;
import com.boxfishedu.workorder.common.bean.AccountCourseEnum;
import com.boxfishedu.workorder.common.bean.ComboTypeEnum;
import com.boxfishedu.workorder.dao.mongo.AcountCardInfoMorphiaRepository;
import com.boxfishedu.workorder.entity.mongo.AccountCardInfo;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.ServeService;
import com.boxfishedu.workorder.service.WorkOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by hucl on 16/9/24.
 */
@Component
public class AccountCardInfoService {
    @Autowired
    private AcountCardInfoMorphiaRepository acountCardInfoMorphiaRepository;

    private Logger logger= LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ServeService serveService;

    @Autowired
    private WorkOrderService workOrderService;

    public AccountCardInfo queryByStudentId(Long studentId){
        logger.debug("@queryByStudentId#userId[{}]",studentId);
        return acountCardInfoMorphiaRepository.queryByStudentId(studentId);
    }



    public void save(AccountCardInfo accountCardInfo){
        acountCardInfoMorphiaRepository.save(accountCardInfo);
    }

    public void saveOrUpdate(Long studentId, AccountCourseBean accountCourseBean, AccountCourseEnum accountCourseEnum){
        acountCardInfoMorphiaRepository.saveOrUpdate(studentId,accountCourseBean,accountCourseEnum);
    }
}
