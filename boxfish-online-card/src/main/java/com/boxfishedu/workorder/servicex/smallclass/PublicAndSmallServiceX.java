package com.boxfishedu.workorder.servicex.smallclass;

import com.boxfishedu.workorder.common.util.DateUtil;
import com.boxfishedu.workorder.dao.jpa.SmallClassJdbc;
import com.boxfishedu.workorder.dao.jpa.SmallClassJpaRepository;
import com.boxfishedu.workorder.entity.mysql.SmallClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by ansel on 2017/3/14.
 */
@Service
public class PublicAndSmallServiceX {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    SmallClassJpaRepository smallClassJpaRepository;

    @Autowired
    SmallClassJdbc smallClassJdbc;

    @Autowired
    RestTemplate restTemplate;

    @Value("{interface.address.destroy_group_service}")
    String destroyGroupUrl;


    public void destroyPublicAndSmallClass(){
        logger.info("@destroyPublicAndSmallClass destroying public and small class group ...");
        LocalDateTime now = LocalDateTime.now();
        List<SmallClass> listPublicAndSmall = smallClassJpaRepository.findPublicAndSmallClassForDestroy(
                DateUtil.localDate2Date(now.minusHours(24)),DateUtil.localDate2Date(now.minusHours(0)));
        StringBuilder stringBuilder = new StringBuilder(destroyGroupUrl);
        stringBuilder.append("/teaching/destroy/public_and_small_group/");
        listPublicAndSmall.stream().filter(Objects::nonNull).forEach(smallClass -> {
            stringBuilder.append(smallClass.getGroupId());
            restTemplate.delete(UriComponentsBuilder.fromUriString(stringBuilder.toString())
                    .path("")
                    .queryParam("")
                    .build()
                    .toUri());
            logger.info("@destroyPublicAndSmallClass groupId:[{}]", smallClass.getGroupId());
        });
//        List groupIdList = smallClassJdbc.getPublicAndSmallGroupId();
//        restTemplate.delete(UriComponentsBuilder.fromUriString("http://localhost/teaching-service" + "/teaching/destroy/public_and_small_group/")
//                    .path("")
//                    .queryParam(Arrays.toString(groupIdList.toArray()))
//                    .build()
//                    .toUri());
    }
}
