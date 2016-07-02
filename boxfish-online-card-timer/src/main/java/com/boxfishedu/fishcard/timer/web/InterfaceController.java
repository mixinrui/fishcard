package com.boxfishedu.fishcard.timer.web;

import com.boxfishedu.beans.view.JsonResultModel;
import com.boxfishedu.fishcard.timer.task.NotifyTimer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by hucl on 16/7/2.
 */
@CrossOrigin
@RestController
@RequestMapping("/timer")
public class InterfaceController {
    @Autowired
    private NotifyTimer notifyTimer;

    @RequestMapping(value = "/assign", method = RequestMethod.POST)
    public JsonResultModel assignTeacher(){
        try {
            notifyTimer.notifyService();
            return JsonResultModel.newJsonResultModel("ok");
        }
        catch (Exception ex){
            return JsonResultModel.newJsonResultModel("error"+ex.getMessage());
        }
    }

    @RequestMapping(value = "/alert", method = RequestMethod.GET)
    public JsonResultModel alert(){
        notifyTimer.teacherOutNumberNotifyService();
        return JsonResultModel.newJsonResultModel("ok");
    }
}
