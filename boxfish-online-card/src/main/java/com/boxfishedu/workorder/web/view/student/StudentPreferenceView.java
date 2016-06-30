package com.boxfishedu.workorder.web.view.student;

import com.boxfishedu.workorder.web.view.base.BaseView;
import lombok.Data;

/**
 * Created by hucl on 16/3/16.
 */
@Data
public class StudentPreferenceView extends BaseView {
    private int difficulty;
    private String grade;
    private String publication;
    private String book;
    private String version;
}
