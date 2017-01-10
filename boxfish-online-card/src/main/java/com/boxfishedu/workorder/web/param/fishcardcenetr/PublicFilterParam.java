package com.boxfishedu.workorder.web.param.fishcardcenetr;

import lombok.Data;

/**
 * Created by hucl on 17/1/10.
 */
@Data
public class PublicFilterParam {
    private Long studentId;
    private Long teacherId;
    private String begin;
    private String end;
}
