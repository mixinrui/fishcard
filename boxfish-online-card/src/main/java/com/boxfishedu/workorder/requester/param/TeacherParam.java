package com.boxfishedu.workorder.requester.param;

import lombok.Data;
import org.bson.types.ObjectId;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by jiaozijun on 16/12/22.
 */
@Data
public class TeacherParam {

    private ObjectId id;

    private Long teacherId;

    private String name;

    private Long boxFishId;

    private Integer property;//1 外部  2 内部

    private String avatarUrl;

    private String nickName;

    private Integer gender;

    private Date birthday;

    private Double score;

    private String school;

    private Long addressId;//存放省市区编码

    private String address;

    private String province;//省

    private String city;//市

    private String county;//区

    private String telephone;

    private String telephoneBack;

    private String email;

    private Long qq;

    private String weiXin;

    private double gpsLongitude;

    private double gpsLatitude;

    private double baiDuLongitude;

    private double baiDuLatitude;

    private String payAli;

    private String payWeiXin;

    private Date createTime;

    private Date updateTime;

    private String identificationType;

    private String identificationValue;

    private Integer isActive = 0;

    private Integer serviceCount;

    private Integer teachingType;//1 中教  2 外教  3 试讲

    private Set<Integer> roleIds = Collections.EMPTY_SET;

    private Set<String> courseTypeIds = Collections.EMPTY_SET;

    private Integer isPublicSchool = 0;

    private Double evaluationScore; //评测成绩

    private Integer auditType = 0;//审核状态

    private Integer interviewType = 0;//面试状态

    private Integer triallectureType = 0;//试讲状态

    private String noPassReason;//不通过理由

    private Integer teachingAge = 1;//教龄信息  枚举

    private Integer isInThePond = 0;//是否在池子里

    private Integer authorizeType = 0;//是否可以给教师教课权限

    private Double spokenScore; //口语成绩

    private Double markScore; //综合评分

    private Integer statusCode = 0;

    //分数集合:颜值/发音/准确性/逻辑/表达  //控场能力/对语言知识的掌握能力/逻辑树/单词/open question/function

    private HashMap<String,Double> interviewScoresMap;

    private HashMap<String,Double> trialScoresMap;


    //以下为外教信息
    private String firstName;

    private String lastName;

    private String cellphoneNumber;

    private Integer degree;

    private String nationality;

    private String occupation;

    private Integer timezone;//时区 1 ~ 12 和-1 ~ -12

    private String skype;

    private String schoolCountry;

    private String specialty;//专业

    private String schoolingTime;//在校时间

    private String job;

    private Integer snack;//零食

    private Integer spokenLevel;//口语水平

    private Date triallectureStartTime;//试讲时间

    private Date triallectureEndTime;//试讲时间

    private  Integer  triallectureSlotId;//试讲时间片Id by ms

    private  Date triallectureDay;//试讲日期 by ms

    private String triallectureCourseId;//试讲课程id

    private String triallectureCourseName;//试讲课程名称

    private String triallectureCourseType;//试讲课程类型

    private String demoCourse; //demo课

    private String initAccount;//初始账号

    private Integer teachingExperience;//教学经验

    private Integer schoolStartYear;

    private Integer schoolEndYear;

    private Long triallectureTeacher;

    private Long triallectureStudent;

    private Integer educationalBackground;//学历

    private Date auditTime;//审核时间

    private Date interviewTime;//面试时间

    private Integer nationalityLevel;//国家级别

    private Double interviewScore;//面试分数

    private Double trialScore;//试讲分数

    private Long onlineGoodReview;//在线点赞数量 师生互评,记录点赞数量 ,点赞就+1

}
