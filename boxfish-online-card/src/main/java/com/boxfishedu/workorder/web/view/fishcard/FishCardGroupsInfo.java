package com.boxfishedu.workorder.web.view.fishcard;

import lombok.Data;

/**
 * Created by jiaozijun on 16/12/8.
 */
@Data
public class FishCardGroupsInfo {

    private String groupId;  //群主Id

    private Long workOrderId;// 鱼卡id

    private Integer chatRoomId;//房间号

    private String [] memberAccount;//学生id 老师id



}
