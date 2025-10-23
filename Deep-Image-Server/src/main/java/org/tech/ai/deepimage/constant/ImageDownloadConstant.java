package org.tech.ai.deepimage.constant;

/**
 * 图片下载相关常量
 * 
 * @author zgq
 * @since 2025-10-22
 */
public class ImageDownloadConstant {
    
    private ImageDownloadConstant() {
        // 私有构造函数，防止实例化
    }
    
    /**
     * HTTP请求头：Accept
     */
    public static final String HEADER_ACCEPT = "Accept";
    
    /**
     * Accept值：图片类型
     */
    public static final String ACCEPT_IMAGE = "image/*";
    
    /**
     * HTTP请求头：User-Agent
     */
    public static final String HEADER_USER_AGENT = "User-Agent";
    
    /**
     * 下载结果状态：完成
     */
    public static final String STATUS_COMPLETED = "completed";
    
    /**
     * 下载结果状态：失败
     */
    public static final String STATUS_FAILED = "failed";
    
    /**
     * 下载结果状态：部分成功
     */
    public static final String STATUS_PARTIAL = "partial";
    
    /**
     * 秒转换：毫秒到秒
     */
    public static final int MILLIS_TO_SECONDS = 1000;
}

