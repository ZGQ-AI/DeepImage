package org.tech.ai.deepimage.constant;

/**
 * Image download related constants
 * 
 * @author zgq
 * @since 2025-10-22
 */
public class ImageDownloadConstant {
    
    private ImageDownloadConstant() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * HTTP request header: Accept
     */
    public static final String HEADER_ACCEPT = "Accept";
    
    /**
     * Accept value: image types
     */
    public static final String ACCEPT_IMAGE = "image/*";
    
    /**
     * HTTP request header: User-Agent
     */
    public static final String HEADER_USER_AGENT = "User-Agent";
    
    /**
     * Download result status: completed
     */
    public static final String STATUS_COMPLETED = "completed";
    
    /**
     * Download result status: failed
     */
    public static final String STATUS_FAILED = "failed";
    
    /**
     * Download result status: partial success
     */
    public static final String STATUS_PARTIAL = "partial";
    
    /**
     * Time conversion: milliseconds to seconds
     */
    public static final int MILLIS_TO_SECONDS = 1000;
}
