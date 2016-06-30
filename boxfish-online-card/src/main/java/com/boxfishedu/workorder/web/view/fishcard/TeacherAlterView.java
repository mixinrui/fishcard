package com.boxfishedu.workorder.web.view.fishcard;

import lombok.Data;

/**
 * Created by hucl on 16/5/28.
 */
@Data
public class TeacherAlterView {
    private Integer roleId;
    private Long count;

    public TeacherAlterView(Long count,Integer roleId){
        this.count=count;
        this.roleId=roleId;
    }
}
