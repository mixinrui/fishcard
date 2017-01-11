package com.boxfishedu.workorder.web.view.base;

import lombok.Data;

/**
 * Created by jiaozijun on 16/12/7.
 */
@Data
public class GroupInfo {

    private String groupId;
    private Long workOrderId;
    private String [] infoMissOut; // 学生老师加密信息从师生教育获取   (加密之后的学生老师ID  组)
    private String [] infoMissIn; // 学生老师加密信息从鱼卡加密后      (加密之后的学生老师ID  组)

    private boolean normal = false; //是否正常
    private Long chatRoomId;//房间号



    @Override
    public GroupInfo clone() {
        GroupInfo prototypeClass = null;
        try {
            prototypeClass = (GroupInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println("克隆对象失败");
        }
        return prototypeClass;
    }
}