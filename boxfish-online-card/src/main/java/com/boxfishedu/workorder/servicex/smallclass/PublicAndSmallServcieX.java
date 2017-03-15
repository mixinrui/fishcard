package com.boxfishedu.workorder.servicex.smallclass;

import com.boxfishedu.workorder.common.util.DateUtil;
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
import java.util.List;
import java.util.Objects;

/**
 * Created by ansel on 2017/3/14.
 */
@Service
public class PublicAndSmallServcieX {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    SmallClassJpaRepository smallClassJpaRepository;

    @Autowired
    RestTemplate restTemplate;

    @Value("{interface.address.destroy_group_service}")
    String destroyGroupUrl;


    public void destroyPublicAndSmallClass(){
        logger.info("@destroyPublicAndSmallClass destroying public and small class group ...");
        LocalDateTime now = LocalDateTime.now();
        List<SmallClass> listPublicAndSmall = smallClassJpaRepository.findPublicAndSmallClassForDestory(
                DateUtil.localDate2Date(now.minusHours(0)),DateUtil.localDate2Date(now.minusHours(24)));
        StringBuffer stringBuffer = new StringBuffer(destroyGroupUrl);
        stringBuffer.append("/teaching/destroy/public_and_small_group/");
        listPublicAndSmall.stream().filter(smallClass -> Objects.nonNull(smallClass)).forEach(smallClass -> {
            StringBuffer sbTemp = stringBuffer;
            sbTemp.append(smallClass.getGroupId());
            restTemplate.delete(UriComponentsBuilder.fromUriString(sbTemp.toString())
                    .path("")
                    .queryParam("")
                    .build()
                    .toUri());
            logger.info("@destroyPublicAndSmallClass groupId:[{}]", smallClass.getGroupId());
        });
    }

}
