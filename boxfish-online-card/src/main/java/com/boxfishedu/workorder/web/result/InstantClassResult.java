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
public class InstantClassResult {
    private Integer status;
    private String desc;
    private GroupInfo groupInfo=null;

    public InstantClassResult(InstantClassRequestStatus instantClassRequestStatus){
        this.status=instantClassRequestStatus.getCode();
        this.desc=instantClassRequestStatus.getDesc();
    }

    public InstantClassResult(InstantClassCard instantClassCard){
        InstantClassRequestStatus instantClassRequestStatus=InstantClassRequestStatus.getEnumByCode(instantClassCard.getStatus());
        this.status=instantClassRequestStatus.getCode();
        this.desc=instantClassRequestStatus.getDesc();
        if(instantClassCard.getStatus().equals(InstantClassRequestStatus.MATCHED.getCode())) {
            this.groupInfo=new GroupInfo();
            this.groupInfo.setGroupId(instantClassCard.getGroupId());
            this.groupInfo.setGroupName(instantClassCard.getGroupName());
            this.groupInfo.setChatRoomId(instantClassCard.getChatRoomId());
            this.groupInfo.setWorkOrderId(instantClassCard.getWorkorderId());
        }
    }

    public static InstantClassResult newInstantClassResult(InstantClassRequestStatus instantClassRequestStatus){
        return new InstantClassResult(instantClassRequestStatus);
    }

    public static InstantClassResult newInstantClassResult(InstantClassCard instantClassCard){
        return new InstantClassResult(instantClassCard);
    }

    public InstantClassResult(){

    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class GroupInfo{
        private Long workOrderId;
        private String groupName;
        private String groupId;
        private Long chatRoomId;
    }
}
