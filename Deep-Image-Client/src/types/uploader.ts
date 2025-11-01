/**
 * Common file uploader component type definitions
 */

// Display mode
export type UploadMode = 'compact' | 'full'

// Upload state
export enum UploadState {
  IDLE = 'idle',           // Idle
  DRAG_OVER = 'drag-over', // Drag over
  PASTE_FOCUS = 'paste-focus', // Paste focus
  UPLOADING = 'uploading', // Uploading
  SUCCESS = 'success',     // Success
  ERROR = 'error'          // Error
}

// Upload file information
export interface UploadFile {
  uid: string                    // Unique identifier
  name: string                   // File name
  size: number                   // File size (bytes)
  type: string                   // MIME type
  status?: 'uploading' | 'done' | 'error' // Upload status
  percent?: number               // Upload progress (0-100)
  url?: string                   // File URL (after successful upload)
  thumbUrl?: string              // Thumbnail URL
  response?: any                 // Server response
  error?: Error                  // Error message
  originFileObj?: File           // Original File object
}

// CommonFileUploader component Props
export interface CommonFileUploaderProps {
  // File restrictions
  accept?: string                // Accepted file types, default 'image/*'
  maxSize?: number              // Maximum file size (MB), default 10
  multiple?: boolean            // Whether to support multiple selection, default false
  maxCount?: number             // Maximum file count, default 1
  
  // UI configuration
  mode?: UploadMode             // Display mode: compact / full, default 'full'
  uploadText?: string           // Upload hint text
  height?: string | number      // Upload area height
  
  // Feature switches
  enableDragDrop?: boolean      // Enable drag and drop, default true
  enablePaste?: boolean         // Enable paste, default true
  enableUrlInput?: boolean      // Enable URL input, default false
  
  // Other configuration
  disabled?: boolean            // Whether disabled
  listType?: 'text' | 'picture' // File list style
}

// CommonFileUploader component Emits
export interface CommonFileUploaderEmits {
  (e: 'file-select', files: File[]): void
  (e: 'file-remove', file: UploadFile): void
  (e: 'change', fileList: UploadFile[]): void
}

