package com.boxfishedu.workorder.common.login;


import com.boxfishedu.workorder.common.util.DateUtil;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.security.Security;
import java.util.Date;

import javax.crypto.Cipher;

/**
 * Created by jiaozijun on 16/6/22.
 */

@Component
public class TokenUtils {


    /**
     * 字符串默认键值
     */
    private static String strDefaultKey = "national";

    /**
     * 有效时间 30 分钟
     **/
    private static int effectiveMinutes = 30;

    /**
     * 加密工具
     */
    private Cipher encryptCipher = null;

    /**
     * 解密工具
     */
    private Cipher decryptCipher = null;

    /**
     * 将byte数组转换为表示16进制值的字符串， 如：byte[]{8,18}转换为：0813， 和public static byte[]
     * hexStr2ByteArr(String strIn) 互为可逆的转换过程
     *
     * @param arrB 需要转换的byte数组
     * @return 转换后的字符串
     * @throws Exception 本方法不处理任何异常，所有异常全部抛出
     */
    public static String byteArr2HexStr(byte[] arrB) throws Exception {
        int iLen = arrB.length;
        // 每个byte用两个字符才能表示，所以字符串的长度是数组长度的两倍
        StringBuffer sb = new StringBuffer(iLen * 2);
        for (int i = 0; i < iLen; i++) {
            int intTmp = arrB[i];
            // 把负数转换为正数
            while (intTmp < 0) {
                intTmp = intTmp + 256;
            }
            // 小于0F的数需要在前面补0
            if (intTmp < 16) {
                sb.append("0");
            }
            sb.append(Integer.toString(intTmp, 16));
        }
        return sb.toString();
    }

    /**
     * 将表示16进制值的字符串转换为byte数组， 和public static String byteArr2HexStr(byte[] arrB)
     * 互为可逆的转换过程
     *
     * @param strIn 需要转换的字符串
     * @return 转换后的byte数组
     * @throws Exception 本方法不处理任何异常，所有异常全部抛出
     */
    public static byte[] hexStr2ByteArr(String strIn) throws Exception {
        byte[] arrB = strIn.getBytes();
        int iLen = arrB.length;

        // 两个字符表示一个字节，所以字节数组长度是字符串长度除以2
        byte[] arrOut = new byte[iLen / 2];
        for (int i = 0; i < iLen; i = i + 2) {
            String strTmp = new String(arrB, i, 2);
            arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16);
        }
        return arrOut;
    }

    /**
     * 默认构造方法，使用默认密钥
     *
     * @throws Exception
     */
    public TokenUtils() throws Exception {
        this(strDefaultKey);
    }

    /**
     * 指定密钥构造方法
     *
     * @param strKey 指定的密钥
     * @throws Exception
     */
    public TokenUtils(String strKey) throws Exception {
        Security.addProvider(new com.sun.crypto.provider.SunJCE());
        Key key = getKey(strKey.getBytes());

        encryptCipher = Cipher.getInstance("DES");
        encryptCipher.init(Cipher.ENCRYPT_MODE, key);

        decryptCipher = Cipher.getInstance("DES");
        decryptCipher.init(Cipher.DECRYPT_MODE, key);
    }

    /**
     * 加密字节数组
     *
     * @param arrB 需加密的字节数组
     * @return 加密后的字节数组
     * @throws Exception
     */
    public byte[] encrypt(byte[] arrB) throws Exception {
        return encryptCipher.doFinal(arrB);
    }


    /**
     * 解密字节数组
     *
     * @param arrB 需解密的字节数组
     * @return 解密后的字节数组
     * @throws Exception
     */
    public byte[] decrypt(byte[] arrB) throws Exception {
        return decryptCipher.doFinal(arrB);
    }


    /**
     * 加密字符串
     *
     * @param strIn 需加密的字符串
     * @return 加密后的字符串
     * @throws Exception
     */
    public String encrypt(String strIn) throws Exception {
        return byteArr2HexStr(encrypt(strIn.getBytes()));
    }



    /**
     * 解密字符串
     *
     * @param strIn 需解密的字符串
     * @return 解密后的字符串
     * @throws Exception
     */
    public String decrypt(String strIn) throws Exception {
        return new String(decrypt(hexStr2ByteArr(strIn)));
    }


    /**
     * 加密方式  用户名_纳秒数
     *
     * @param userName
     * @return
     * @throws Exception
     */
    public String getToken(String userName) throws Exception {
        long naoTime =  System.nanoTime();// 纳秒
        String tokenBefore = userName.trim()+"_"+naoTime;
        return byteArr2HexStr(encrypt((tokenBefore).getBytes()));
    }

    // 获取用户名
    public  String getUserName(String token)throws Exception {
       return decrypt(token).split("_")==null?"":decrypt(token).split("_")[0];
    }
    // 获取纳秒
    public String getNM(String token)throws Exception {
        return decrypt(token).split("_")==null?"":decrypt(token).split("_")[1];
    }

    /**
     * 根据token 获取 上一次操作时间
     *
     * @param token 必须存在
     * @return
     * @throws Exception
     */
    public Date getDateFromTokenReal(String token) throws Exception {
       // String date = this.decrypt(token);
        return DateUtil.String2Date(token);
    }

    /**
     * 根据token获取用户登陆name
     * @param token
     * @return
     * @throws Exception
     */
    public String getUserNameFromToken(String token) throws Exception {
        return this.decrypt(token);
    }



    /**
     * 判断token是否有效
     *
     * @param realToken
     * @return
     */
    public boolean isTokenCanUsed(String realToken) throws Exception {
        boolean flag = true;
        Date dateinRedis = this.getDateFromTokenReal(realToken);
        dateinRedis = DateUtil.addMinutes(dateinRedis, -effectiveMinutes);
        return dateinRedis.before(new Date());
    }



    /**
     * 从指定字符串生成密钥，密钥所需的字节数组长度为8位 不足8位时后面补0，超出8位只取前8位
     *
     * @param arrBTmp 构成该字符串的字节数组
     * @return 生成的密钥
     * @throws Exception
     */
    private Key getKey(byte[] arrBTmp) throws Exception {
        // 创建一个空的8位字节数组（默认值为0）
        byte[] arrB = new byte[8];

        // 将原始字节数组转换为8位
        for (int i = 0; i < arrBTmp.length && i < arrB.length; i++) {
            arrB[i] = arrBTmp[i];
        }

        // 生成密钥
        Key key = new javax.crypto.spec.SecretKeySpec(arrB, "DES");

        return key;
    }


    private static final String privateKey = "2390s03*(072&32#jsiioe0uqyy";

    public  String getPassword(String password) {
        return Hashing.md5().newHasher().
                putString(password, Charsets.UTF_8).
                putString(privateKey, Charsets.UTF_8).
                hash().toString();
    }

    public static void main(String[] args) {
        try {
            TokenUtils des = new TokenUtils();//自定义密钥
            //System.out.println(des.getPassword("boxfishedu"));

//            TokenUtils des = new TokenUtils();//自定义密钥
//            System.out.println(des.getPassword("boxfishedu"));
            String test1 = "I#zUcPAP@3XkXazyDljZZLgTxSyNjCx";
            String test2 = "wH4JIvlXnk8O5iSOPlnS28&XyfiUbOI";
            String test3 = "If%lj^KghmDt6G&W$rk%uFlV$BZEAhv";
            String test4 = "I5b0RCQzgQ!90tziLyhceJLpQNSFP6m";
            String test5 = "hagM0qErK3XFg5NA2GqqB#tg#R20UOF";
            String test6 = "9tl5$e*U1lRY7Z!9w6FqF^4Ki2dNKIi";
            String test7 ="%BfY1$r5w#uJh*BLI!r6%y3LBAc3!uP";
            String test8 = "X#oPmOvb6s%%sU7Tr^HcyQEcHtusrqw";
            String test9 = "F*MX%^vyMFYXW^t&QK9hL2mVyKKwWDQ";
            String test10 = "C6TleQu5s&CaQT3AAO!cB5LEIwX^ZVz";
            String test11 = "42JB1#gRzJKEPw9s0c35$fvT2IqUHf9";



            System.out.println("'"+des.getPassword(test1)+"'");
            System.out.println("'"+des.getPassword(test2)+"'");
            System.out.println("'"+des.getPassword(test3)+"'");
            System.out.println("'"+des.getPassword(test4)+"'");
            System.out.println("'"+des.getPassword(test5)+"'");
            System.out.println("'"+des.getPassword(test6)+"'");
            System.out.println("'"+des.getPassword(test7)+"'");
            System.out.println("'"+des.getPassword(test8)+"'");
            System.out.println("'"+des.getPassword(test9)+"'");
            System.out.println("'"+des.getPassword(test10)+"'");
            System.out.println("'"+des.getPassword(test11)+"'");


           // System.out.println("解密后的字符：" + des.decrypt(des.encrypt(test)));

            //System.out.println("解密后的字符：" + des.decrypt("202cb962ac59075b964b07152d234b70"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
