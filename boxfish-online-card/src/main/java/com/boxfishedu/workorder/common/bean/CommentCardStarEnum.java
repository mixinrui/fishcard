package com.boxfishedu.workorder.common.bean;

/**
 * Created by ansel on 16/11/12.
 */
public enum CommentCardStarEnum {
    LEVEL1(1,100),

    LEVEL2(2,200),

    LEVEL3(3,400),

    LEVEL4(4,600),

    LEVEL5(5,800);

    private int starLevel;

    private int point;

    CommentCardStarEnum(int starLevel,int point){
        this.starLevel = starLevel;
        this.point = point;
    }

    public int getStarLevel(){
        return this.starLevel;
    }

    public static int getPoint(int starLevel){
        for (CommentCardStarEnum commentCardStarEnum: CommentCardStarEnum.values()) {
            if (commentCardStarEnum.starLevel == starLevel){
                return commentCardStarEnum.point;
            }
        }
        return  0;
    }
}
