package com.boxfishedu.workorder.web.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * Created by hucl on 16/11/3.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InstantClassResult {
    private Integer status;
    private String desc;
    private String groupId;
}
