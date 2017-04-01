package com.boxfishedu.workorder.web.result;

import lombok.Data;

/**
 * Created by hucl on 17/2/20.
 */
@Data
public class StudentLeftInfo {
    private Long singleCN=0l;
    private Long singleFRN=0l;
    private Long comment=0l;
    private Long multiFRN=0l;

    public StudentLeftInfo(){

    }

    public StudentLeftInfo(Long singleCN,Long singleFRN,Long comment,Long multiFRN){
        this.singleCN=singleCN;
        this.singleFRN=singleFRN;
        this.multiFRN=multiFRN;
        this.comment=comment;
    }
}
