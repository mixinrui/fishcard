package com.boxfishedu.workorder.web.result;

import lombok.Data;

/**
 * Created by hucl on 16/11/10.
 */
@Data
public class InstantGroupInfo {
   private Long id;
   private Long workOrderId;
   private String groupName;
   private String groupId;
   private Long chatRoomId;
   private Object createAt;
   private Boolean groupChecked;
   private Object updateAt;
}
