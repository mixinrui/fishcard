package com.boxfishedu.card.comment.manage.entity.dto;

import java.util.List;

/**
 * Created by ansel on 16/11/30.
 */
public class CommentCardExcelDto extends ExcelDto{

    public CommentCardExcelDto(List<CommentCardDto> data){
        super(data,"外教点评记录");
    }

    @Override
    String[] getHeaderName() {
        return new String[]{"点评编号","点评创建时间","实际完成时间","点评状态","老师ID","老师姓名","学生ID","课程名","所属订单"};
    }

    @Override
    String[] getFields() {
        return new String[]{"code","createTime","teacherAnswerTime","statusDesc","teacherId","teacherName","studentId","courseName","orderCode"};
    }
}
