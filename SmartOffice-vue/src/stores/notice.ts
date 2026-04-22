import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUnreadCount as getUnreadCountApi } from '@/api/office'

export const useNoticeStore = defineStore('notice', () => {
  const unreadCount = ref(0)

  const fetchUnreadCount = async () => {
    try {
      const res = await getUnreadCountApi()
      unreadCount.value = res.data
    } catch (error) {
      console.error(error)
    }
  }

  return {
    unreadCount,
    fetchUnreadCount
  }
})