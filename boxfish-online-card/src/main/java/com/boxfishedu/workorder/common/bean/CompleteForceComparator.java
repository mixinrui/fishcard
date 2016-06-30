package com.boxfishedu.workorder.common.bean;

import java.util.Comparator;

/**
 * Created by hucl on 16/6/25.
 */
public class CompleteForceComparator implements Comparator<FishCardDelayMessage> {
    public int compare(FishCardDelayMessage fmsg1, FishCardDelayMessage fmsg2) {
        if (fmsg1.getEndTime().after(fmsg2.getEndTime())) {
            return 1;
        } else if (fmsg1.getEndTime().equals(fmsg2.getEndTime())) {
            return 1;
        } else {
            return -1;
        }
    }
}
