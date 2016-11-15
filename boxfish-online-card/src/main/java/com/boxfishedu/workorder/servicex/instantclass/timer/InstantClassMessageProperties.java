package com.boxfishedu.workorder.servicex.instantclass.timer;

import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import org.springframework.amqp.core.MessageProperties;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by hucl on 16/11/8.
 */
@SuppressWarnings("ALL")
public class InstantClassMessageProperties {
    public static MessageProperties getMsgProperties(InstantClassCard instantClassCard){
        MessageProperties messageProperties = new MessageProperties();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(instantClassCard.getRequestMatchTeacherTime());
        calendar.add(Calendar.SECOND,20);
        Date deadDate=calendar.getTime();

        Long diff=deadDate.getTime()-new Date().getTime();
        if(diff<0l){
            diff=0l;
        }
        messageProperties.setExpiration(diff+"");
        messageProperties.setTimestamp(new Date());
        return messageProperties;
    }
}
