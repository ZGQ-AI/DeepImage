/**
 * 标签Store - 管理标签信息
 */
import { ref } from 'vue'
import { defineStore } from 'pinia'
import { message } from 'ant-design-vue'
import {
  createTag as createTagApi,
  listTags as listTagsApi,
  updateTag as updateTagApi,
  deleteTag as deleteTagApi,
} from '../api/tag'
import type {
  CreateTagRequest,
  UpdateTagRequest,
  TagResponse,
} from '../types/tag'

export const useTagStore = defineStore('tag', () => {
  // 标签列表
  const tags = ref<TagResponse[]>([])

  // 加载状态
  const tagsLoading = ref(false)
  const operationLoading = ref(false)

  /**
   * 获取标签列表
   */
  async function fetchTags() {
    tagsLoading.value = true
    try {
      const { data } = await listTagsApi()
      if (data.code === 200) {
        tags.value = data.data
      } else {
        throw new Error(data.message)
      }
    } catch (error: any) {
      message.error(error?.message || '获取标签列表失败')
      throw error
    } finally {
      tagsLoading.value = false
    }
  }

  /**
   * 创建标签
   */
  async function createTag(request: CreateTagRequest) {
    operationLoading.value = true
    try {
      const { data } = await createTagApi(request)
      if (data.code === 200) {
        message.success('标签创建成功')
        // 刷新标签列表
        await fetchTags()
        return data.data
      } else {
        throw new Error(data.message)
      }
    } catch (error: any) {
      message.error(error?.message || '创建标签失败')
      throw error
    } finally {
      operationLoading.value = false
    }
  }

  /**
   * 更新标签
   */
  async function updateTag(request: UpdateTagRequest) {
    operationLoading.value = true
    try {
      const { data } = await updateTagApi(request)
      if (data.code === 200) {
        message.success('标签更新成功')
        // 刷新标签列表
        await fetchTags()
        return data.data
      } else {
        throw new Error(data.message)
      }
    } catch (error: any) {
      message.error(error?.message || '更新标签失败')
      throw error
    } finally {
      operationLoading.value = false
    }
  }

  /**
   * 删除标签
   */
  async function deleteTag(tagId: number) {
    operationLoading.value = true
    try {
      const { data } = await deleteTagApi({ tagId })
      if (data.code === 200) {
        message.success('标签删除成功')
        // 刷新标签列表
        await fetchTags()
        return true
      } else {
        throw new Error(data.message)
      }
    } catch (error: any) {
      message.error(error?.message || '删除标签失败')
      throw error
    } finally {
      operationLoading.value = false
    }
  }

  /**
   * 清空标签状态（用于退出登录等场景）
   */
  function clearTagState() {
    tags.value = []
  }

  return {
    tags,
    tagsLoading,
    operationLoading,
    fetchTags,
    createTag,
    updateTag,
    deleteTag,
    clearTagState,
  }
})

