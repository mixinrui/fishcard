package com.boxfishedu.workorder.web.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Created by hucl on 17/2/20.
 */
@Data
public class StudentClassInfo {
    private Long singleClass;
    private Long multiClass;

    public StudentClassInfo(Long one2One,Long smallClass){
        this.singleClass=one2One;
        this.multiClass=smallClass;
    }

    public StudentClassInfo(){

    }
}
