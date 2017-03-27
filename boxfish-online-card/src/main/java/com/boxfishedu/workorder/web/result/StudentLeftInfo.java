package com.boxfishedu.workorder.web.result;

import lombok.Data;

/**
 * Created by hucl on 17/2/20.
 */
@Data
public class StudentLeftInfo {
    private Long singleCN;
    private Long singleFRN;
    private Long comment;
    private Long multiFRN;

    public StudentLeftInfo(){

    }

    public StudentLeftInfo(Long singleCN,Long singleFRN,Long comment,Long multiFRN){
        this.singleCN=singleCN;
        this.singleFRN=singleFRN;
        this.multiFRN=multiFRN;
        this.comment=comment;
    }
}
