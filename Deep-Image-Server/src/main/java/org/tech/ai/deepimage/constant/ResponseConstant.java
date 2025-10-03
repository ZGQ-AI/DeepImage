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

    // unified error messages
    public static final String NOT_LOGIN_MESSAGE = "not login";
    public static final String FORBIDDEN_MESSAGE_DETAIL = "permission denied";
    public static final String PARAM_INVALID_MESSAGE = "parameter invalid";

    // user error messages
    public static final String USERNAME_ALREADY_EXISTS_MESSAGE = "用户名已被占用";
    public static final String SESSION_NOT_FOUND_MESSAGE = "会话不存在";
    public static final String CANNOT_DELETE_CURRENT_SESSION_MESSAGE = "不能删除当前会话";
    public static final String SESSION_NOT_BELONG_TO_USER_MESSAGE = "该会话不属于当前用户";

    // google oauth error messages
    public static final String GOOGLE_OAUTH_AUTHORIZATION_FAILED = "GOOGLE_OAUTH_AUTHORIZATION_FAILED";
    public static final String GOOGLE_OAUTH_MISSING_CODE = "GOOGLE_OAUTH_MISSING_CODE";
    public static final String GOOGLE_OAUTH_TOKEN_EXCHANGE_FAILED = "GOOGLE_OAUTH_TOKEN_EXCHANGE_FAILED";
    public static final String GOOGLE_OAUTH_MISSING_ID_TOKEN = "GOOGLE_OAUTH_MISSING_ID_TOKEN";
    public static final String GOOGLE_OAUTH_MISSING_EMAIL = "GOOGLE_OAUTH_MISSING_EMAIL";
    public static final String GOOGLE_OAUTH_CALLBACK_PROCESSING_FAILED = "GOOGLE_OAUTH_CALLBACK_PROCESSING_FAILED";
    public static final String GOOGLE_OAUTH_SERVICE_UNAVAILABLE = "GOOGLE_OAUTH_SERVICE_UNAVAILABLE";

    // file error messages
    public static final String FILE_UPLOAD_FAILED_MESSAGE = "文件上传失败";
    public static final String FILE_NOT_FOUND_MESSAGE = "文件不存在";
    public static final String FILE_PERMISSION_DENIED_MESSAGE = "无权操作该文件";
    public static final String FILE_SIZE_EXCEEDED_MESSAGE = "文件大小超过限制";
    public static final String FILE_TYPE_INVALID_MESSAGE = "业务类型不合法";
    public static final String FILE_BEING_REFERENCED_MESSAGE = "文件正在被引用，无法删除";
    public static final String FILE_ALREADY_EXISTS_MESSAGE = "文件已存在";
    public static final String FILE_NOT_EXISTS_MESSAGE = "文件不存在";

    // tag error messages
    public static final String TAG_NOT_FOUND_MESSAGE = "标签不存在或无权访问";

    // file share error messages
    public static final String FILE_ACCESS_DENIED_MESSAGE = "无权访问该文件";
    public static final String SHARE_NOT_FOUND_MESSAGE = "分享记录不存在";
    public static final String SHARE_EXPIRED_MESSAGE = "分享已过期";
    public static final String SHARE_PERMISSION_DENIED_MESSAGE = "无权分享该文件";
    public static final String CANCEL_SHARE_PERMISSION_DENIED_MESSAGE = "无权取消该分享";
    public static final String VIEW_SHARE_PERMISSION_DENIED_MESSAGE = "无权查看该分享";
    public static final String TARGET_USER_NOT_FOUND_MESSAGE = "目标用户不存在";
    public static final String TEMPORARY_SHARE_REQUIRES_EXPIRY_MESSAGE = "临时分享必须设置过期时间";
    public static final String SHARE_ALREADY_EXISTS_MESSAGE = "已存在相同的分享关系";
    public static final String VIEW_ACCESS_LOG_PERMISSION_DENIED_MESSAGE = "无权查看访问日志";

    // default redirect urls
    public static final String DEFAULT_FRONTEND_URL = "http://localhost:5173";

}