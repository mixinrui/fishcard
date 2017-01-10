package com.boxfishedu.workorder.common.log;

/**
 * Created by ansel on 2017/1/7.
 */
public class CommentCardLog extends BaseLog{

    public CommentCardLog(Long userId){
        this();
        this.UserId = userId;
    }

    public CommentCardLog(){
        this.ModuleCode = "fishCard";
        this.BusinessObject = "commentCard";
    }
}
