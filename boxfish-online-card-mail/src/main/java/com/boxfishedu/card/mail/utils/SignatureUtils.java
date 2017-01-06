package com.boxfishedu.card.mail.utils;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Created by LuoLiBing on 17/1/6.
 */
public class SignatureUtils {

    public static String sign(String token, Object...params) {
        StringBuilder builder = new StringBuilder(100);
        builder.append(token);
        for(int i = 0; i < params.length; i++) {
            builder.append(",");
            builder.append(params[i]);
        }
        return DigestUtils.md5Hex(builder.toString().getBytes());
    }

}
