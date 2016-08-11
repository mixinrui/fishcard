package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

/**
 * Created by ansel on 16/8/11.
 */
@Data
public class UpdatePicturesForm {
    Long userId;
    String role;
    String accessToken;
    String picturePath;
}
