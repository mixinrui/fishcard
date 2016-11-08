package com.boxfishedu.workorder.servicex.instantclass.timer;

import com.boxfishedu.workorder.dao.jpa.InstantClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by hucl on 16/11/8.
 */
@Component
public class InstantClassTimerDealer {
    @Autowired
    private InstantClassJpaRepository instantClassJpaRepository;

    @Transactional
    public void timerGetInstantTeachers(InstantClassCard instantClassCard){
        InstantClassCard dbInstantCard =instantClassJpaRepository.findOne(instantClassCard.getId());
    }
}
