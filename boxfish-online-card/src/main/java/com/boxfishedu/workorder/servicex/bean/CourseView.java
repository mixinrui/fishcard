package com.boxfishedu.workorder.servicex.bean;

import com.boxfishedu.workorder.entity.mongo.ScheduleCourseInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import lombok.Data;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Created by LuoLiBing on 16/4/25.
 */
@Data
//@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseView implements Serializable {

    private final static Logger logger = LoggerFactory.getLogger(CourseView.class);

    @JsonProperty(value = "courseId")
    private String bookSectionId;

    private String name;

    // TODO 添加英文课名
    @JsonIgnore
    private String englishName;

    private String thumbnail;

    private List<String> courseType;

    private List<String> difficulty;

    private Long lastModified;

    public void setLocale(Locale locale) {
        // 非中文环境,并且课程英文名部位空的情况下全部显示成英文
        if(!Objects.equals(locale, Locale.CHINA) && StringUtils.isNotEmpty(englishName)) {
            name = englishName;
        }
    }

    public static CourseView courseViewAdapter(ScheduleCourseInfo scheduleCourseInfo) {
        logger.info("scheduleCourseInfo信息 [{}]", scheduleCourseInfo);
        if (scheduleCourseInfo == null) {
            return null;
        }
        CourseView courseView = new CourseView();
        courseView.setBookSectionId(scheduleCourseInfo.getCourseId());
        courseView.setThumbnail(scheduleCourseInfo.getThumbnail());
        courseView.setName(scheduleCourseInfo.getName());
        List<String> courseTypeList = Lists.newArrayList();
        courseTypeList.add(scheduleCourseInfo.getCourseType());
        List<String> difficultyList = Lists.newArrayList();
        if (scheduleCourseInfo.getDifficulty() != null) {
            difficultyList.add(scheduleCourseInfo.getDifficulty());
        }
        courseView.setCourseType(courseTypeList);
        courseView.setDifficulty(difficultyList);
        courseView.setLastModified(scheduleCourseInfo.getLastModified());
        courseView.setEnglishName(scheduleCourseInfo.getEnglishName());
        logger.info("courseView []", courseView);
        return courseView;
    }
}
