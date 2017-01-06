package com.boxfishedu.workorder.servicex.multiteaching.statusdealer;

import com.boxfishedu.workorder.common.bean.multiteaching.SmallClassCardStatus;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import com.boxfishedu.workorder.requester.SmallClassRequester;
import com.boxfishedu.workorder.requester.SmallClassTeacherRequester;
import com.boxfishedu.workorder.web.view.course.RecommandCourseView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by hucl on 17/1/5.
 */
@Order(100)
@Component
public class CreateEventCustomer extends SmallClassEventCustomer {

    @Autowired
    private SmallClassRequester smallClassRequester;

    @Autowired
    private SmallClassTeacherRequester smallClassTeacherRequester;




    @PostConstruct
    public void initEvent() {
        this.setSmallClassCardStatus(SmallClassCardStatus.CREATE);
    }

    @Override
    public void exec(SmallClassEvent smallClassEvent) {
        SmallClass smallClass = smallClassEvent.getSource();
        smallClass.setStatus(smallClassEvent.getType().getCode());

        //找出leader鱼卡

        //获取推荐课程
//        RecommandCourseView recommandCourseView=smallClassRequester.;

        //获取推荐教师

        //保存smallclass

        //将小班课信息更新进workorder,courseschedule

        //持久化数据到数据库

        //记录流水日志

    }
}
