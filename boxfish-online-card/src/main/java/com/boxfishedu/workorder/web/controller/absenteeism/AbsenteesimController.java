package com.boxfishedu.workorder.web.controller.absenteeism;

import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.service.absenteeism.AbsenteeismService;
import com.boxfishedu.workorder.service.absenteeism.sdk.AbsenteeismSDK;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by ansel on 16/9/19.
 */
@RestController
@RequestMapping(value = "/test/aaa")
public class AbsenteesimController {

    @Autowired
    AbsenteeismSDK absenteeismSDK;

    @Autowired
    AbsenteeismService absenteeismService;

    @RequestMapping(value = "/bbb", method = RequestMethod.GET)
    public Object testAaa(){
        WorkOrder workOrder = new WorkOrder();
        workOrder.setStudentId(17000l);
        workOrder.setCourseId("L3NoYXJlL3N2bi9CYXNpYyAxLzAwNy5XaGVuIGRvIHlvdSBsaXN0ZW4gdG8gbXVzaWM_Lnhsc3g");
        return absenteeismSDK.absenteeismDeductScore(workOrder);
    }

    @RequestMapping(value = "/deduct/score", method = RequestMethod.GET)
    public void deductScore(){
        absenteeismService.testQueryAbsentStudent();
    }

}
