package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Created by ansel on 16/8/9.
 */
@Data
public class PushToStudentAndTeacher {
    List<Long> user_id;
    String push_title;
    Map<String,String> data;
}
