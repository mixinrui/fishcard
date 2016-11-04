package com.boxfishedu.workorder.web.result;

import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * Created by hucl on 16/11/3.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InstantClassResult {
    private Integer status;
    private String desc;
    private String groupId;

    public InstantClassResult(InstantClassRequestStatus instantClassRequestStatus){
        this.status=instantClassRequestStatus.getCode();
        this.desc=instantClassRequestStatus.getDesc();
    }

    public InstantClassResult(InstantClassRequestStatus instantClassRequestStatus,String groupId){
        this.status=instantClassRequestStatus.getCode();
        this.desc=instantClassRequestStatus.getDesc();
        this.groupId=groupId;
    }

    public InstantClassResult(){

    }
}
