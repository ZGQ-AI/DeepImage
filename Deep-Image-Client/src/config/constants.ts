/**
 * 应用常量配置
 * 统一管理应用中所有硬编码的字符串、数字和配置值
 */

/**
 * 页面标题常量
 */
export const PAGE_TITLES = {
  GALLERY: '我的图库',
  SEARCH: '图片搜索',
  RECYCLE_BIN: '回收站',
  TAG_MANAGEMENT: '标签管理',
  USER_PROFILE: '个人中心',
  HOME: '首页',
} as const

/**
 * 页面描述常量
 */
export const PAGE_DESCRIPTIONS = {
  GALLERY: '管理您的图片收藏',
  SEARCH: '通过关键词搜索网络图片，选择后下载到图库',
  RECYCLE_BIN: '已删除的文件将在此保存，可以恢复或彻底删除',
} as const

/**
 * 按钮文字常量
 */
export const BUTTON_TEXTS = {
  // 通用操作
  UPLOAD: '上传更多',
  UPLOAD_COLLAPSE: '收起上传',
  BATCH_OPERATE: '批量操作',
  EXIT_SELECTION: '退出选择',
  SELECT_ALL: '全选',
  CLEAR_SELECTION: '清空',
  DELETE: '删除',
  RESTORE: '恢复',
  PERMANENT_DELETE: '彻底删除',
  CONFIRM: '确认',
  CANCEL: '取消',
  CONFIRM_DELETE: '确认删除',
  CONFIRM_RESTORE: '确认恢复',
  CONFIRM_EMPTY: '确认清空',
  SEARCH: '搜索图片',
  SEARCH_FILE_NAME: '搜索文件名',
  VIEW_GALLERY: '查看图库',
  START_NEW_SEARCH: '开始新搜索',
  CREATE_TAG: '创建标签',
  REFRESH: '刷新',
  
  // 文件操作
  RENAME: '重命名',
  DOWNLOAD: '下载',
  PREVIEW: '预览',
  MANAGE_TAGS: '管理标签',
  EMPTY_RECYCLE_BIN: '清空回收站',
} as const

/**
 * 提示信息常量
 */
export const MESSAGES = {
  // 上传相关
  UPLOAD_SUCCESS: (count: number) => `成功上传 ${count} 张图片`,
  UPLOAD_ERROR: (filename: string, error: string) => `${filename} 上传失败: ${error}`,
  
  // 删除相关
  DELETE_CONFIRM: (count: number) => `确定要删除选中的 ${count} 张图片吗？删除后可在回收站中恢复。`,
  DELETE_SUCCESS: (filename: string) => `图片 "${filename}" 删除成功`,
  BATCH_DELETE_SUCCESS: (success: number) => `成功删除 ${success} 张图片`,
  BATCH_DELETE_PARTIAL: (success: number, failed: number) => `删除完成：成功 ${success} 张，失败 ${failed} 张`,
  
  // 恢复相关
  RESTORE_CONFIRM: (count: number) => `确定要恢复选中的 ${count} 个文件吗？`,
  RESTORE_SUCCESS: (count: number) => `成功恢复 ${count} 个文件`,
  RESTORE_PARTIAL: (success: number, failed: number) => `恢复完成：成功 ${success} 个，失败 ${failed} 个`,
  
  // 永久删除相关
  PERMANENT_DELETE_CONFIRM: (count: number) => `确定要彻底删除选中的 ${count} 个文件吗？此操作不可恢复！`,
  PERMANENT_DELETE_SUCCESS: (count: number) => `成功删除 ${count} 个文件`,
  EMPTY_BIN_CONFIRM: '确定要清空回收站吗？所有文件将被彻底删除，此操作不可恢复！',
  EMPTY_BIN_SUCCESS: (count: number) => `已清空回收站，删除了 ${count} 个文件`,
  
  // 重命名相关
  RENAME_SUCCESS: (filename: string) => `重命名成功：${filename}`,
  RENAME_NO_CHANGE: '文件名未改变',
  RENAME_EMPTY_ERROR: '文件名不能为空',
  
  // 搜索相关
  SEARCH_SUCCESS: (count: number) => `搜索到 ${count} 张图片`,
  SEARCH_NO_RESULTS: '未找到相关图片，请尝试其他关键词',
  SEARCH_KEYWORD_REQUIRED: '请输入搜索关键词',
  
  // 下载相关
  DOWNLOAD_START: '正在下载图片...',
  DOWNLOAD_SUCCESS: (filename: string) => `图片 "${filename}" 下载完成`,
  DOWNLOAD_ERROR: (error: string) => `下载失败: ${error}`,
  
  // 其他
  LOADING_IMAGES: '加载图片失败',
  LOADING_RECYCLE_BIN: '加载回收站失败',
  NO_SELECTION: '请先选择要删除的图片',
  NO_SELECTION_RESTORE: '请先选择要恢复的文件',
  NO_SELECTION_DELETE: '请先选择要删除的文件',
  COPYRIGHT_NOTICE: '请注意图片版权，仅用于学习和研究目的',
} as const

/**
 * 文件上传配置常量
 */
export const UPLOAD_CONFIG = {
  // 默认文件大小限制（MB）
  MAX_SIZE: 10,
  // 默认最大文件数量
  MAX_COUNT: 20,
  // 搜索图片数量配置
  SEARCH_COUNT: 10,
  SEARCH_COUNT_RANGE: {
    MIN: 1,
    MAX: 30,
  },
  // 搜索关键词最大长度
  SEARCH_KEYWORD_MAX_LENGTH: 50,
} as const

/**
 * 分页配置常量
 */
export const PAGINATION_CONFIG = {
  // 默认分页大小
  DEFAULT_PAGE_SIZE: 100,
  // 小分页大小（回收站等）
  SMALL_PAGE_SIZE: 10,
  // 分页选项
  PAGE_SIZE_OPTIONS: ['10', '20', '50', '100'],
  // 分页文字模板
  SHOW_TOTAL: (total: number) => `共 ${total} 条`,
} as const

/**
 * 样式配置常量
 */
export const STYLE_CONFIG = {
  // 网格列数
  GRID_COLUMNS: {
    DEFAULT: 4,
    LARGE: 5,
    MEDIUM: 3,
    SMALL: 2,
    MOBILE: 1,
  },
  // 防抖延迟（毫秒）
  DEBOUNCE_TIMEOUT: 500,
  // 响应式断点
  BREAKPOINTS: {
    LARGE: 1600,
    MEDIUM: 1200,
    SMALL: 768,
    MOBILE: 480,
  },
  // 网格间距
  GRID_GAP: {
    DEFAULT: 16,
    SMALL: 12,
    MOBILE: 0,
  },
  // 图片容器高度
  IMAGE_CONTAINER_HEIGHT: {
    DEFAULT: 200,
    SMALL: 150,
  },
} as const

/**
 * 排序选项常量
 */
export const SORT_OPTIONS = {
  CREATED_AT_DESC: {
    value: 'createdAt-desc',
    label: '上传时间 (新→旧)',
  },
  CREATED_AT_ASC: {
    value: 'createdAt-asc',
    label: '上传时间 (旧→新)',
  },
  FILE_SIZE_DESC: {
    value: 'fileSize-desc',
    label: '文件大小 (大→小)',
  },
  FILE_SIZE_ASC: {
    value: 'fileSize-asc',
    label: '文件大小 (小→大)',
  },
  FILENAME_ASC: {
    value: 'originalFilename-asc',
    label: '文件名 (A→Z)',
  },
  FILENAME_DESC: {
    value: 'originalFilename-desc',
    label: '文件名 (Z→A)',
  },
} as const

/**
 * 步骤描述常量（用于图片搜索）
 */
export const STEP_DESCRIPTIONS = {
  SEARCH: {
    title: '搜索图片',
    description: '输入关键词搜索',
  },
  SELECT: {
    title: '选择图片',
    description: '预览并选择图片',
  },
  DOWNLOAD: {
    title: '下载完成',
    description: '保存到图库',
  },
} as const

/**
 * 状态显示常量（用于图片搜索）
 */
export const STATUS_CONFIG = {
  COMPLETED: {
    color: 'success',
    text: '全部成功',
  },
  PARTIAL: {
    color: 'warning',
    text: '部分成功',
  },
  FAILED: {
    color: 'error',
    text: '全部失败',
  },
} as const

/**
 * 占位符文字常量
 */
export const PLACEHOLDERS = {
  SEARCH_KEYWORD: '输入要搜索的关键词，如：樱花、风景、动物',
  SEARCH_FILE_NAME: '搜索文件名',
  FILTER_BY_TAG: '按标签筛选',
  ALL_TAGS: '全部标签',
  SELECT_TAGS: '选择要添加的标签',
  RENAME_FILENAME: '请输入文件名',
} as const

/**
 * 空状态文字常量
 */
export const EMPTY_STATES = {
  GALLERY: {
    title: '暂无图片',
    description: '上传您的第一张图片开始使用',
  },
  RECYCLE_BIN: {
    title: '回收站是空的',
    description: '已删除的文件会在这里保留',
  },
  SEARCH_NO_RESULTS: '未找到相关图片，请尝试其他关键词',
} as const

