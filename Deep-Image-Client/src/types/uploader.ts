/**
 * 通用文件上传组件的类型定义
 */

// 显示模式
export type UploadMode = 'compact' | 'full'

// 上传状态
export enum UploadState {
  IDLE = 'idle',           // 空闲
  DRAG_OVER = 'drag-over', // 拖拽悬停
  PASTE_FOCUS = 'paste-focus', // 粘贴聚焦
  UPLOADING = 'uploading', // 上传中
  SUCCESS = 'success',     // 成功
  ERROR = 'error'          // 失败
}

// 上传文件信息
export interface UploadFile {
  uid: string                    // 唯一标识
  name: string                   // 文件名
  size: number                   // 文件大小（字节）
  type: string                   // MIME 类型
  status?: 'uploading' | 'done' | 'error' // 上传状态
  percent?: number               // 上传进度 (0-100)
  url?: string                   // 文件 URL（上传成功后）
  thumbUrl?: string              // 缩略图 URL
  response?: any                 // 服务器响应
  error?: Error                  // 错误信息
  originFileObj?: File           // 原始 File 对象
}

// CommonFileUploader 组件 Props
export interface CommonFileUploaderProps {
  // 文件限制
  accept?: string                // 接受的文件类型，默认 'image/*'
  maxSize?: number              // 最大文件大小(MB)，默认 10
  multiple?: boolean            // 是否支持多选，默认 false
  maxCount?: number             // 最大文件数量，默认 1
  
  // UI 配置
  mode?: UploadMode             // 显示模式：compact / full，默认 'full'
  uploadText?: string           // 上传提示文字
  height?: string | number      // 上传区域高度
  
  // 功能开关
  enableDragDrop?: boolean      // 启用拖拽，默认 true
  enablePaste?: boolean         // 启用粘贴，默认 true
  enableUrlInput?: boolean      // 启用 URL 输入，默认 false
  
  // 其他配置
  disabled?: boolean            // 是否禁用
  listType?: 'text' | 'picture' // 文件列表的样式
}

// CommonFileUploader 组件 Emits
export interface CommonFileUploaderEmits {
  (e: 'file-select', files: File[]): void
  (e: 'file-remove', file: UploadFile): void
  (e: 'change', fileList: UploadFile[]): void
}

