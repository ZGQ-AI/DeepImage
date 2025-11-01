/**
 * Tag Store - Manages tag information
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
import type { CreateTagRequest, UpdateTagRequest, TagResponse } from '../types/tag'

export const useTagStore = defineStore('tag', () => {
  // Tag list
  const tags = ref<TagResponse[]>([])

  // Loading states
  const tagsLoading = ref(false)
  const operationLoading = ref(false)

  /**
   * Fetch tag list
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
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      message.error(error?.message || '获取标签列表失败')
      throw error
    } finally {
      tagsLoading.value = false
    }
  }

  /**
   * Create a tag
   */
  async function createTag(request: CreateTagRequest) {
    operationLoading.value = true
    try {
      const { data } = await createTagApi(request)
      if (data.code === 200) {
        message.success('标签创建成功')
        // Refresh tag list
        await fetchTags()
        return data.data
      } else {
        throw new Error(data.message)
      }
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      message.error(error?.message || '创建标签失败')
      throw error
    } finally {
      operationLoading.value = false
    }
  }

  /**
   * Update a tag
   */
  async function updateTag(request: UpdateTagRequest) {
    operationLoading.value = true
    try {
      const { data } = await updateTagApi(request)
      if (data.code === 200) {
        message.success('标签更新成功')
        // Refresh tag list
        await fetchTags()
        return data.data
      } else {
        throw new Error(data.message)
      }
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      message.error(error?.message || '更新标签失败')
      throw error
    } finally {
      operationLoading.value = false
    }
  }

  /**
   * Delete a tag
   */
  async function deleteTag(tagId: number) {
    operationLoading.value = true
    try {
      const { data } = await deleteTagApi({ tagId })
      if (data.code === 200) {
        message.success('标签删除成功')
        // Refresh tag list
        await fetchTags()
        return true
      } else {
        throw new Error(data.message)
      }
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      message.error(error?.message || '删除标签失败')
      throw error
    } finally {
      operationLoading.value = false
    }
  }

  /**
   * Clear tag state (used for logout and other scenarios)
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
