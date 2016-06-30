package com.boxfishedu.workorder.common.bean;

import lombok.Data;

import java.util.Comparator;
import java.util.Date;

/**
 * Created by hucl on 16/6/8.
 */
@Data
public class FishCardDelayMessage {
    private Integer type;
    private Long id;
    private Integer status;
    private Date startTime;
    private Date endTime;

    public static FishCardDelayMessage newFishCardDelayMessage() {
        return new FishCardDelayMessage();
    }

    public static  class StartTimeComparator implements Comparator<FishCardDelayMessage>{
        public int compare(FishCardDelayMessage fmsg1,FishCardDelayMessage fmsg2){
            if(fmsg1.getStartTime().after(fmsg2.getStartTime())){
                return 1;
            }
            else if(fmsg1.getStartTime().equals(fmsg2.getStartTime())){
                return 1;
            }
            else {
                return -1;
            }
        }
    }
}
