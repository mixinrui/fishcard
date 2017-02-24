package com.boxfishedu.workorder.common.util;

/**
 * Created by jiaozijun on 17/2/23.
 */

import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;

/**
 * Created by lauzhihao on 2016/05/23.
 * <p>
 * MD5加密工具类
 */
public final class MD5Util {

    private static Logger logger = LoggerFactory.getLogger(MD5Util.class);

    public static String encrypt(String content) {
        if (Strings.isNullOrEmpty(content)) {
            return "";
        }
        MessageDigest digestInstance;
        try {
            digestInstance = MessageDigest.getInstance("MD5");
            digestInstance.update(content.getBytes("utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("sign error!");
        }

        byte[] md = digestInstance.digest();
        StringBuilder sb = new StringBuilder();
        for (byte aMd : md) {
            int val = (aMd) & 0xff;
            if (val < 16)
                sb.append("0");
            sb.append(Integer.toHexString(val));
        }
        logger.info("sign={}", sb.toString().toUpperCase());
        return sb.toString().toUpperCase();
    }

    public static boolean invalidSign(String inputSign, String... args) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (String arg : args) {
            stringBuilder.append(arg);
        }
        String sign = encrypt(stringBuilder.toString());
        logger.debug("inputSign = {},outputSign = {}", inputSign, sign);
        return !StringUtils.equals(sign, inputSign);
    }
}
