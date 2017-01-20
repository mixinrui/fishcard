package com.boxfishedu.workorder.servicex.smallclass;

import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.common.util.JacksonUtil;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEvent;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEventDispatch;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Created by hucl on 17/1/14.
 */
@Service
public class SmallClassStudentStatusServiceX {
    @Autowired
    private SmallClassJpaRepository smallClassJpaRepository;

    @Autowired
    private SmallClassEventDispatch smallClassEventDispatch;

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());


    public void status(Long smallClassId, Long userId, Map<String, String> statusReport) {

        Integer status = Integer.parseInt(statusReport.get("status"));

        Date reportTime = DateUtil.String2Date(
                statusReport.get("reportTime").toString());

        SmallClass smallClass = smallClassJpaRepository.findOne(smallClassId);

        if (Objects.isNull(smallClass)) {
            logger.error("@status#无对应的小班课id[{}]", smallClassId);
        }

        smallClass.setStatusReporter(userId);
        smallClass.setStatus(status);
        smallClass.setReportTime(reportTime);

        PublicClassInfoStatusEnum publicClassInfoStatusEnum = PublicClassInfoStatusEnum.getByCode(status);

        new SmallClassEvent(smallClass, smallClassEventDispatch, publicClassInfoStatusEnum);
    }
}
