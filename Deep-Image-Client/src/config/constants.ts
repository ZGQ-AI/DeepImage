/**
 * Application constants configuration
 * Unified management of all hardcoded strings, numbers and configuration values in the application
 */

/**
 * Page title constants
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
 * Page description constants
 */
export const PAGE_DESCRIPTIONS = {
  GALLERY: '管理您的图片收藏',
  SEARCH: '通过关键词搜索网络图片，选择后下载到图库',
  RECYCLE_BIN: '已删除的文件将在此保存，可以恢复或彻底删除',
} as const

/**
 * Button text constants
 */
export const BUTTON_TEXTS = {
  // Common operations
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
  
  // File operations
  RENAME: '重命名',
  DOWNLOAD: '下载',
  PREVIEW: '预览',
  MANAGE_TAGS: '管理标签',
  EMPTY_RECYCLE_BIN: '清空回收站',
} as const

/**
 * Message constants
 */
export const MESSAGES = {
  // Upload related
  UPLOAD_SUCCESS: (count: number) => `成功上传 ${count} 张图片`,
  UPLOAD_ERROR: (filename: string, error: string) => `${filename} 上传失败: ${error}`,
  
  // Delete related
  DELETE_CONFIRM: (count: number) => `确定要删除选中的 ${count} 张图片吗？删除后可在回收站中恢复。`,
  DELETE_SUCCESS: (filename: string) => `图片 "${filename}" 删除成功`,
  BATCH_DELETE_SUCCESS: (success: number) => `成功删除 ${success} 张图片`,
  BATCH_DELETE_PARTIAL: (success: number, failed: number) => `删除完成：成功 ${success} 张，失败 ${failed} 张`,
  
  // Restore related
  RESTORE_CONFIRM: (count: number) => `确定要恢复选中的 ${count} 个文件吗？`,
  RESTORE_SUCCESS: (count: number) => `成功恢复 ${count} 个文件`,
  RESTORE_PARTIAL: (success: number, failed: number) => `恢复完成：成功 ${success} 个，失败 ${failed} 个`,
  
  // Permanent delete related
  PERMANENT_DELETE_CONFIRM: (count: number) => `确定要彻底删除选中的 ${count} 个文件吗？此操作不可恢复！`,
  PERMANENT_DELETE_SUCCESS: (count: number) => `成功删除 ${count} 个文件`,
  EMPTY_BIN_CONFIRM: '确定要清空回收站吗？所有文件将被彻底删除，此操作不可恢复！',
  EMPTY_BIN_SUCCESS: (count: number) => `已清空回收站，删除了 ${count} 个文件`,
  
  // Rename related
  RENAME_SUCCESS: (filename: string) => `重命名成功：${filename}`,
  RENAME_NO_CHANGE: '文件名未改变',
  RENAME_EMPTY_ERROR: '文件名不能为空',
  
  // Search related
  SEARCH_SUCCESS: (count: number) => `搜索到 ${count} 张图片`,
  SEARCH_NO_RESULTS: '未找到相关图片，请尝试其他关键词',
  SEARCH_KEYWORD_REQUIRED: '请输入搜索关键词',
  
  // Download related
  DOWNLOAD_START: '正在下载图片...',
  DOWNLOAD_SUCCESS: (filename: string) => `图片 "${filename}" 下载完成`,
  DOWNLOAD_ERROR: (error: string) => `下载失败: ${error}`,
  
  // Others
  LOADING_IMAGES: '加载图片失败',
  LOADING_RECYCLE_BIN: '加载回收站失败',
  NO_SELECTION: '请先选择要删除的图片',
  NO_SELECTION_RESTORE: '请先选择要恢复的文件',
  NO_SELECTION_DELETE: '请先选择要删除的文件',
  COPYRIGHT_NOTICE: '请注意图片版权，仅用于学习和研究目的',
} as const

/**
 * File upload configuration constants
 */
export const UPLOAD_CONFIG = {
  // Default file size limit (MB)
  MAX_SIZE: 10,
  // Default maximum file count
  MAX_COUNT: 20,
  // Search image count configuration
  SEARCH_COUNT: 10,
  SEARCH_COUNT_RANGE: {
    MIN: 1,
    MAX: 30,
  },
  // Maximum length of search keyword
  SEARCH_KEYWORD_MAX_LENGTH: 50,
} as const

/**
 * Pagination configuration constants
 */
export const PAGINATION_CONFIG = {
  // Default page size
  DEFAULT_PAGE_SIZE: 100,
  // Small page size (for recycle bin, etc.)
  SMALL_PAGE_SIZE: 10,
  // Page size options
  PAGE_SIZE_OPTIONS: ['10', '20', '50', '100'],
  // Pagination text template
  SHOW_TOTAL: (total: number) => `共 ${total} 条`,
} as const

/**
 * Style configuration constants
 */
export const STYLE_CONFIG = {
  // Grid columns
  GRID_COLUMNS: {
    DEFAULT: 4,
    LARGE: 5,
    MEDIUM: 3,
    SMALL: 2,
    MOBILE: 1,
  },
  // Debounce delay (milliseconds)
  DEBOUNCE_TIMEOUT: 500,
  // Responsive breakpoints
  BREAKPOINTS: {
    LARGE: 1600,
    MEDIUM: 1200,
    SMALL: 768,
    MOBILE: 480,
  },
  // Grid gap
  GRID_GAP: {
    DEFAULT: 16,
    SMALL: 12,
    MOBILE: 0,
  },
  // Image container height
  IMAGE_CONTAINER_HEIGHT: {
    DEFAULT: 200,
    SMALL: 150,
  },
} as const

/**
 * Sort option constants
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
 * Step description constants (for image search)
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
 * Status display constants (for image search)
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
 * Placeholder text constants
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
 * Empty state text constants
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

