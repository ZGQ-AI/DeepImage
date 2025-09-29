package org.tech.ai.deepimage.constant;

public class ResponseConstant {

    public static final int SUCCESS = 200;
    public static final int SYSTEM_ERROR = 500;
    public static final int PARAM_ERROR = 400;
    public static final int NOT_FOUND = 404;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int TOO_MANY_REQUESTS = 429;
    public static final int SERVICE_UNAVAILABLE = 503;
    public static final int GATEWAY_TIMEOUT = 504;
    public static final String SUCCESS_MESSAGE = "success";
    public static final String SYSTEM_ERROR_MESSAGE = "system error";
    public static final String PARAM_ERROR_MESSAGE = "param error";
    public static final String NOT_FOUND_MESSAGE = "not found";
    public static final String UNAUTHORIZED_MESSAGE = "unauthorized";
    public static final String FORBIDDEN_MESSAGE = "forbidden";
    public static final String TOO_MANY_REQUESTS_MESSAGE = "too many requests";
    public static final String SERVICE_UNAVAILABLE_MESSAGE = "service unavailable";
    public static final String GATEWAY_TIMEOUT_MESSAGE = "gateway timeout";

    // auth error messages
    public static final String INVALID_CREDENTIALS_MESSAGE = "invalid email or password";
    public static final String INVALID_REFRESH_TOKEN_MESSAGE = "invalid or expired refresh token";
    public static final String EMAIL_OR_USERNAME_EXISTS_MESSAGE = "email or username already exists";
    public static final String USER_NOT_FOUND_MESSAGE = "user not found";
    public static final String EMAIL_NOT_VERIFIED_MESSAGE = "email not verified";

}