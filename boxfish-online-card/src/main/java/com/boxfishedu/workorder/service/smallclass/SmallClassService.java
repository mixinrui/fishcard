package com.boxfishedu.workorder.service.smallclass;

import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;

/**
 * Created by hucl on 17/3/1.
 */
@SuppressWarnings("ALL")
@Service
public class SmallClassService {


    @Autowired
    SmallClassJpaRepository smallClassJpaRepository;

    Logger logger= LoggerFactory.getLogger(this.getClass());

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

    @Transactional
    public void testProxy(){
        SmallClass smallClass=new SmallClass();
        smallClass.setStartTime(new Date());
        smallClass.setStatus(10);
        smallClass.setEndTime(new Date());
        smallClass.setClassDate(new Date());
        smallClass.setClassNum(1l);
        smallClassJpaRepository.save(smallClass);
        smallClass.setCreateTime(new Date());

        logger.debug("&&&&&&&&&&&&&&&&&&&&&&proxy[{}]",smallClass.getId());

        smallClass.setClassNum(2l);
    }
}
