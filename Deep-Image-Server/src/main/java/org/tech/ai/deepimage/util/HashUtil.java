package org.tech.ai.deepimage.util;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.security.MessageDigest;

/**
 * 哈希工具类
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Slf4j
public class HashUtil {
    
    private static final String SHA256_ALGORITHM = "SHA-256";
    private static final int BUFFER_SIZE = 8192;
    
    /**
     * 计算字节数组的 SHA-256 哈希值
     * 
     * @param data 字节数组
     * @return 64位十六进制字符串
     */
    public static String sha256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA256_ALGORITHM);
            byte[] hashBytes = digest.digest(data);
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            log.error("计算SHA-256哈希失败", e);
            throw new RuntimeException("计算文件哈希失败", e);
        }
    }
    
    /**
     * 计算输入流的 SHA-256 哈希值
     * 
     * @param inputStream 输入流
     * @return 64位十六进制字符串
     */
    public static String sha256(InputStream inputStream) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA256_ALGORITHM);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            
            byte[] hashBytes = digest.digest();
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            log.error("计算SHA-256哈希失败", e);
            throw new RuntimeException("计算文件哈希失败", e);
        }
    }
    
    /**
     * 计算字符串的 SHA-256 哈希值
     * 
     * @param text 字符串
     * @return 64位十六进制字符串
     */
    public static String sha256(String text) {
        return sha256(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
    
    /**
     * 将字节数组转换为十六进制字符串
     * 
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}

