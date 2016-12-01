package com.boxfishedu.workorder.web.filter;

import lombok.Data;

/**
 * Created by hucl on 16/11/30.
 */
@Data
public class ParentAuthBean {
    //家长id
    private Long id;

    private java.util.Map<String,String> target;
    
}
