package com.boxfishedu.workorder.web.result;

import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
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
    private GroupInfo groupInfo;

    public InstantClassResult(InstantClassRequestStatus instantClassRequestStatus){
        this.status=instantClassRequestStatus.getCode();
        this.desc=instantClassRequestStatus.getDesc();
    }

    public InstantClassResult(InstantClassRequestStatus instantClassRequestStatus, InstantClassCard instantClassCard){
        this.status=instantClassRequestStatus.getCode();
        this.desc=instantClassRequestStatus.getDesc();
        this.groupInfo.setGroupId(instantClassCard.getGroupId());
    }

    public static InstantClassResult newInstantClassResult(InstantClassRequestStatus instantClassRequestStatus){
        return new InstantClassResult(instantClassRequestStatus);
    }

    public static InstantClassResult newInstantClassResult(InstantClassRequestStatus instantClassRequestStatus,String groupId){
        return new InstantClassResult(instantClassRequestStatus,groupId);
    }

    public InstantClassResult(){

    }

    @Data
    class GroupInfo{
        private Long workOrderId;
        private String groupName;
        private String groupId;
        private Long chatRoomId;
    }
}
