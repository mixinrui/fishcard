

package com.boxfishedu.fishcard.timer.web.absenteeism;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.fishcard.timer.task.NotifyTimer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by ansel on 16/9/21.
 */
@RestController
@RequestMapping("/timer")
public class AbsenteesimController {
    @Autowired
    NotifyTimer notifyTimer;

//    @RequestMapping(value = "/deduct/score", method = RequestMethod.GET)
//    public Object handleDeductScore(){
//        notifyTimer.deductScore();
//        return JsonResultModel.newJsonResultModel("ok");
//    }
}
