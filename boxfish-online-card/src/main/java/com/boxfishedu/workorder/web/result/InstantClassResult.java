package com.boxfishedu.workorder.web.result;

import com.boxfishedu.workorder.common.bean.instanclass.InstantClassRequestStatus;
import com.boxfishedu.workorder.common.bean.instanclass.TeacherInstantClassStatus;
import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.entity.mysql.InstantClassCard;
import com.boxfishedu.workorder.entity.mysql.WorkOrder;
import com.boxfishedu.workorder.requester.TeacherPhotoRequester;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * Created by hucl on 16/11/3.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("ALL")
public class InstantClassResult {
    protected Integer status;
    protected String desc;
    protected Long slotId;
    protected GroupInfo groupInfo=null;

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

    public InstantClassResult(InstantClassCard instantClassCard,InstantClassRequestStatus instantClassRequestStatus){
        this.status=instantClassRequestStatus.getCode();
        if(instantClassCard.getMatchResultReadFlag()==1){
            try {
                this.desc = getMatchedDesc(instantClassCard);
            }
            catch (Exception ex){
                this.desc = instantClassRequestStatus.getDesc();
            }
        }
        else {
            this.desc = instantClassRequestStatus.getDesc();
        }
        this.slotId=instantClassCard.getSlotId();
        switch (instantClassRequestStatus){
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

    public InstantClassResult(InstantClassCard instantClassCard,TeacherPhotoRequester teacherPhotoRequester){
        InstantClassRequestStatus instantClassRequestStatus=InstantClassRequestStatus.getEnumByCode(instantClassCard.getStatus());
        this.status=instantClassRequestStatus.getCode();
        //提示用户当前已经匹配了课程或者稍后再试
        if(instantClassCard.getMatchResultReadFlag()==1){
            try {
                this.desc = getMatchedDesc(instantClassCard);
            }
            catch (Exception ex){
                this.desc = instantClassRequestStatus.getDesc();
            }
        }
        else {
            this.desc = instantClassRequestStatus.getDesc();
        }
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

    private static String getMatchedDesc(InstantClassCard instantClassCard) {
        Date begin=instantClassCard.getRequestMatchTeacherTime();
        Date end= DateUtil.addMinutes(begin,25);
        Date deadLine=DateUtil.addMinutes(begin,30);
        long minute=(deadLine.getTime()-new Date().getTime())/(1000*60);
        String beginStr=DateUtil.dateTrimYear(begin).substring(0,5);
        String endStr=DateUtil.dateTrimYear(end).substring(0,5);
        StringBuilder builder=new StringBuilder().append("已安排")
                .append(String.join("-",beginStr,endStr)).append("的实时课程;请等待外教发起邀请,或").append(minute).append("分钟后再试");
        return builder.toString();
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

    public static InstantClassResult newInstantClassResult(InstantClassCard instantClassCard,TeacherPhotoRequester teacherPhotoRequester){
        return new InstantClassResult(instantClassCard,teacherPhotoRequester);
    }

    public static InstantClassResult newInstantClassResult(InstantClassCard instantClassCard,TeacherInstantClassStatus teacherInstantClassStatus){
        return new InstantClassResult(instantClassCard,teacherInstantClassStatus);
    }

    public static InstantClassResult newInstantClassResult(InstantClassCard instantClassCard,InstantClassRequestStatus instantClassRequestStatus){
        return new InstantClassResult(instantClassCard,instantClassRequestStatus);
    }

    public InstantClassResult(){

    }

    @Data
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
