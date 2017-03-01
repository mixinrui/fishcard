package com.boxfishedu.workorder.service.smallclass;

import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

/**
 * Created by hucl on 17/3/1.
 */
@Service
public class SmallClassService {

    @Autowired
    SmallClassJpaRepository smallClassJpaRepository;

    public void persistIntoDb(SmallClass smallClass, PublicClassInfoStatusEnum publicClassInfoStatusEnum) {
        SmallClass dbSmallClass = smallClassJpaRepository.findOne(smallClass.getId());
        dbSmallClass.setStatus(publicClassInfoStatusEnum.getCode());

        switch (publicClassInfoStatusEnum) {
            case TEACHER_CLASSING:
                if (Objects.isNull(dbSmallClass.getActualStartTime())) {
                    dbSmallClass.setActualStartTime(new Date());
                }
                break;

            case TEACHER_COMPLETED:
            case TEACHER_COMPLETED_FORCE:
                if (Objects.isNull(dbSmallClass.getActualEndTime())) {
                    dbSmallClass.setActualEndTime(new Date());
                }
                break;

            default:
                break;
        }

        smallClassJpaRepository.save(dbSmallClass);
    }
}
