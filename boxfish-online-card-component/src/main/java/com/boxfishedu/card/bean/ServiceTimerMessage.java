 package com.boxfishedu.card.bean;

import lombok.Data;

import java.util.Map;

/**
 * Created by hucl on 16/5/28.
 */
@Data
public class ServiceTimerMessage {
    private String time;
    //0:分配教师的接收和回复;1:教师报警的接收和回复
    private Integer type;
    //0为无需处理;1为需要处理
    private Integer status;
    private Map<Integer,Long> body;
    private String startTime;
    private String endTime;

    public ServiceTimerMessage(Integer type){
        this.status=0;
        this.type=type;
        this.body=null;
    }

    public ServiceTimerMessage(){

    }
}
