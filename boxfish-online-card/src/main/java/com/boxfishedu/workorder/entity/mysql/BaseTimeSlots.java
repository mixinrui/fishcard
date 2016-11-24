package com.boxfishedu.workorder.entity.mysql;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by LuoLiBing on 16/11/23.
 *
 */
@Entity
@Table
@Data
public class BaseTimeSlots implements Serializable {

    public final static Integer TEACHING_TYPE_CN = 0;

    public final static Integer TEACHING_TYPE_FRN = 1;

    public final static Integer CLIENT_TYPE_STU = 0;

    public final static Integer CLIENT_TYPE_TEA = 1;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Date classDate;
    private Integer slotId;
    private Date startTime;
    private Date endTime;
    // 概率
    private Integer probability;
    private Integer teachingType;
    private Integer clientType;

    public boolean roll() {
        return ThreadLocalRandom.current().nextInt(100) + 1 <= probability;
    }

    private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static LocalTime initStartTime = LocalTime.parse("07:00:00", timeFormatter);

    public void initTime() {
        LocalTime startTime = initStartTime.plusMinutes((slotId - 1) * 30);
        this.startTime = toDate(startTime);
        this.endTime = toDate(startTime.plusMinutes(25));

    }

    public static Date toDate(LocalTime localTime) {
        Instant instant = localTime.atDate(LocalDate.now())
                .atZone(ZoneId.systemDefault()).toInstant();
        return toDate(instant);
    }

    public static Date toDate(Instant instant) {
        BigInteger milis = BigInteger.valueOf(instant.getEpochSecond()).multiply(
                BigInteger.valueOf(1000));
        milis = milis.add(BigInteger.valueOf(instant.getNano()).divide(
                BigInteger.valueOf(1_000_000)));
        return new Date(milis.longValue());
    }
}
