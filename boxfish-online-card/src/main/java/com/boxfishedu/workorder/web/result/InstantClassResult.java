package com.boxfishedu.workorder.web.result;

import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.bean.instanclass.TeacherInstantClassStatus;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherPhotoRequester;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by hucl on 16/11/3.
 */
@Data
@Component
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InstantClassResult {
    protected Integer status;
    protected String desc;
    protected Long slotId;
    protected GroupInfo groupInfo=null;

    @Autowired
    private TeacherPhotoRequester teacherPhotoRequester;

    public InstantClassResult(InstantClassRequestStatus instantClassRequestStatus){
        this.status=instantClassRequestStatus.getCode();
        this.desc=instantClassRequestStatus.getDesc();
    }

    public InstantClassResult(InstantClassRequestStatus instantClassRequestStatus,String desc){
        this.status=instantClassRequestStatus.getCode();
        this.desc=desc;
    }

    public InstantClassResult(TeacherInstantClassStatus teacherInstantClassStatus){
        this.status=teacherInstantClassStatus.getCode();
        this.desc=teacherInstantClassStatus.getDesc();
    }

    public InstantClassResult(InstantClassCard instantClassCard,TeacherInstantClassStatus teacherInstantClassStatus){
        this.status=teacherInstantClassStatus.getCode();
        this.desc=teacherInstantClassStatus.getDesc();
        this.slotId=instantClassCard.getSlotId();
        switch (teacherInstantClassStatus){
            case MATCHED:{
                this.groupInfo=new GroupInfo();
                this.groupInfo.setGroupId(instantClassCard.getGroupId());
                this.groupInfo.setGroupName(instantClassCard.getGroupName());
                this.groupInfo.setChatRoomId(instantClassCard.getChatRoomId());
                this.groupInfo.setWorkOrderId(instantClassCard.getWorkorderId());
                this.groupInfo.setStudentId(instantClassCard.getStudentId());
                break;
            }
            default:
                break;

        }
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
            this.groupInfo.setTeacherName(instantClassCard.getTeacherName());
            this.groupInfo.setStudentId(instantClassCard.getStudentId());
            this.groupInfo.setTeacherThumbNail(teacherPhotoRequester.getTeacherPhoto(instantClassCard.getTeacherId()));
        }
    }

    public static InstantClassResult newInstantClassResult(InstantClassRequestStatus instantClassRequestStatus){
        return new InstantClassResult(instantClassRequestStatus);
    }

    public static InstantClassResult newInstantClassResult(InstantClassRequestStatus instantClassRequestStatus,String desc){
        return new InstantClassResult(instantClassRequestStatus,desc);
    }

    public static InstantClassResult newInstantClassResult(TeacherInstantClassStatus teacherInstantClassStatus){
        return new InstantClassResult(teacherInstantClassStatus);
    }

    public static InstantClassResult newInstantClassResult(InstantClassCard instantClassCard){
        return new InstantClassResult(instantClassCard);
    }

    public static InstantClassResult newInstantClassResult(InstantClassCard instantClassCard,TeacherInstantClassStatus teacherInstantClassStatus){
        return new InstantClassResult(instantClassCard,teacherInstantClassStatus);
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
        private String teacherName;
        @JsonProperty("teacherPhoto")
        private String teacherThumbNail;
        private Long studentId;
    }

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    class CourseInfo{
        private String courseId;
        private String courseName;
    }
}
