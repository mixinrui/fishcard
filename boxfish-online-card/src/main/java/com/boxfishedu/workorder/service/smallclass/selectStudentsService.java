package com.boxfishedu.workorder.service.smallclass;

import com.boxfishedu.workorder.common.util.RedisKeyGenerator;
import com.boxfishedu.workorder.dao.mongo.ConfigBeanMorphiaRepository;
import com.boxfishedu.workorder.servicex.studentrelated.PublicClassService;
import com.google.common.collect.Sets;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by hucl on 17/2/17.
 */
@Service
public class SelectStudentsService {

    RedisTemplate<String, String> redisTemplate;

    ZSetOperations<String, String> zSetOperations;

    SetOperations<String, String> setOperations;

    @Autowired
    ConfigBeanMorphiaRepository configBeanMorphiaRepository;

    @Autowired
    PublicClassService publicClassService;

    @Autowired
    public void initRedis(@Qualifier(value = "stringRedisTemplate") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.zSetOperations = redisTemplate.opsForZSet();
        this.setOperations = redisTemplate.opsForSet();
    }

    public String selectCandidate(Long smallClassId) {
        Set<String> selectedStudentIds =
                this.selectRandomStudentList(smallClassId, 0l);

        if (CollectionUtils.isEmpty(selectedStudentIds)) {
            return null;
        }
        String[] candidates = new String[selectedStudentIds.size()];
        String candidate = selectedStudentIds.toArray(candidates)[new Random().nextInt(selectedStudentIds.size())];

        Set<String> candidateSet = Sets.newHashSet();
        candidateSet.add(candidate);
        this.addSelectedStudents(candidateSet, smallClassId);

        return candidate;
    }

    public Set<String> selectRandomStudentList(Long smallClassId, Long retryTimes) {
        String key = RedisKeyGenerator.getGroupClassHeartBeatKey(smallClassId);

        Long heartBeatNum = zSetOperations.size(key);

        Long limit = Long.parseLong(configBeanMorphiaRepository.getSingleBean().getSelectStudentNum());
        limit = limit > heartBeatNum ? limit : heartBeatNum;

        //recursive exit
        if (limit * retryTimes >= heartBeatNum) {
            return dealAllRepeatedSet(key, smallClassId, limit);
        }

        Set<String> students = zSetOperations.range(key, retryTimes, limit);
        Set<String> selectedStudents = this.filterSelectedStudents(students, smallClassId);

        if (!CollectionUtils.isEmpty(students) && CollectionUtils.isEmpty(selectedStudents)) {
            return this.selectRandomStudentList(smallClassId, ++retryTimes);
        }

        return selectedStudents;
    }

    public Set<String> dealAllRepeatedSet(String key, Long smallClassId, Long limit) {
        Set<String> randomSet = zSetOperations.range(key, 0, limit);
        if (CollectionUtils.isEmpty(randomSet)) {
            return this.selectStudentdsFromDb(smallClassId);
        }
        return randomSet;
    }

    public Set<String> selectStudentdsFromDb(Long smallClassId) {
        Set<Long> members = publicClassService.getPublicClassRoomMembers(smallClassId);
        Set<String> md5UserSet = members.stream().map(userId -> DigestUtils.md5Hex(userId.toString())).collect(Collectors.toSet());
        Set<String> filteredMd5UserSet = this.filterSelectedStudents(md5UserSet, smallClassId);
        if (!CollectionUtils.isEmpty(md5UserSet) && CollectionUtils.isEmpty(filteredMd5UserSet)) {
            return md5UserSet;
        }
        return filteredMd5UserSet;
    }

    public Set<String> filterSelectedStudents(Set<String> students, Long smallClassId) {
        return students.stream()
                .filter(student -> !isSelected(student, smallClassId))
                .collect(Collectors.toSet());
    }

    public void addSelectedStudents(Set<String> students, Long smallClassId) {
        if (CollectionUtils.isEmpty(students)) {
            return;
        }

        String key = RedisKeyGenerator.getGroupSelectedKey(smallClassId);

        students.forEach(student -> setOperations.add(key, student));
    }

    private boolean isSelected(String useId, Long smallClassId) {
        String key = RedisKeyGenerator.getGroupSelectedKey(smallClassId);
        return setOperations.isMember(key, useId);
    }
}
