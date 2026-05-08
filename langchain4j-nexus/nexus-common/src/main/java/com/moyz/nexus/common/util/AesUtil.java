package com.moyz.nexus.common.util;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * copy from com.baomidou.mybatisplus.core.toolkit.AES
 */
public class AesUtil {

    public static String AES_KEY = "";

    public static String encrypt(String data) {
        return encrypt(data, AES_KEY);
    }


    public static String decrypt(String data) {
        return decrypt(data, AES_KEY);
    }

    /**
     * 加密
     *
     * @param data 需要加密的内容
     * @param key  加密密码
     * @return
     */
    public static byte[] encrypt(byte[] data, byte[] key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key, Constants.AES);
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, Constants.AES);
            Cipher cipher = Cipher.getInstance(Constants.AES_CBC_CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(key));
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new MybatisPlusException(e);
        }
    }

    /**
     * 解密
     *
     * @param data 待解密内�?
     * @param key  解密密钥
     * @return
     */
    public static byte[] decrypt(byte[] data, byte[] key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key, Constants.AES);
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, Constants.AES);
            Cipher cipher = Cipher.getInstance(Constants.AES_CBC_CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(key));
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new MybatisPlusException(e);
        }
    }

    /**
     * 加密
     *
     * @param data 需要加密的内容
     * @param key  加密密码
     * @return
     */
    public static String encrypt(String data, String key) {
        byte[] valueByte = encrypt(data.getBytes(StandardCharsets.UTF_8), key.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(valueByte);
    }

    /**
     * 解密
     *
     * @param data 待解密内�?base64 字符�?
     * @param key  解密密钥
     * @return
     */
    public static String decrypt(String data, String key) {
        byte[] originalData = Base64.getDecoder().decode(data.getBytes());
        byte[] valueByte = decrypt(originalData, key.getBytes(StandardCharsets.UTF_8));
        return new String(valueByte);
    }

    /**
     * 生成一个随机字符串密钥
     *
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String generateRandomKey() {
        return IdWorker.get32UUID().substring(0, 16);
    }
}
