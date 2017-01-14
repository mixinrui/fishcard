package com.boxfishedu.workorder.servicex.smallclass.status.studentstatus;

import com.boxfishedu.workorder.common.bean.PublicClassInfoConstantStatus;
import com.boxfishedu.workorder.common.bean.PublicClassInfoStatusEnum;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEvent;
import com.boxfishedu.workorder.servicex.smallclass.status.event.SmallClassEventCustomer;
import com.boxfishedu.workorder.servicex.smallclass.initstrategy.GroupInitStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Created by hucl on 17/1/5.
 */
@Order(PublicClassInfoConstantStatus.STUDENT_VOICE_VIDEO_FAIL)
@Component
public class StudentVoiceVedioFailCustomer extends SmallClassEventCustomer {

    @Autowired
    Map<String, GroupInitStrategy> groupInitStrategyMap;

    @PostConstruct
    public void initEvent() {
        this.setSmallClassCardStatus(PublicClassInfoStatusEnum.STUDENT_VOICE_VIDEO_FAIL);
    }

    @Override
    public void exec(SmallClassEvent smallClassEvent) {
        logger.info("学生接入言视频失败...");
    }
}
