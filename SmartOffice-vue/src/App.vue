<template>
  <router-view />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

onMounted(() => {
  // 页面加载时获取用户信息
  const token = localStorage.getItem('token')
  if (token) {
    userStore.getUserInfo()
  }
})
</script>
