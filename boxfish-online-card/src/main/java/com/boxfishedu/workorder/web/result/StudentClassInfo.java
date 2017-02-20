package com.boxfishedu.workorder.web.result;

import lombok.Data;

/**
 * Created by hucl on 17/2/20.
 */
@Data
public class StudentClassInfo {
    private Long one2One;
    private Long smallClass;

    public StudentClassInfo(Long one2One,Long smallClass){
        this.one2One=one2One;
        this.smallClass=smallClass;
    }

    public StudentClassInfo(){

    }
}
