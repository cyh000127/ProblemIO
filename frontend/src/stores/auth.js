// src/stores/auth.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout as apiLogout } from '@/api/auth'
import { getMe, getMySummary } from '@/api/user'
import { useRouter } from 'vue-router'

export const useAuthStore = defineStore('auth', () => {
  const router = useRouter()

  const accessToken = ref(localStorage.getItem('accessToken') || null)
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isAuthenticated = computed(() => !!accessToken.value && !!user.value)

  const loadUserWithSummary = async () => {
    const [me, summary] = await Promise.all([getMe(), getMySummary()])

    const merged = {
      ...me,
      followerCount: summary.followerCount,
      followingCount: summary.followingCount,
      quizCount: summary.createdQuizCount,  // DTO 필드명 확인 필요
    }

    user.value = merged
    localStorage.setItem('user', JSON.stringify(merged))
  }


  async function loginUser(email, password) {
    try {
      const data = await login(email, password)
      accessToken.value = data.accessToken
      localStorage.setItem('accessToken', accessToken.value)


      await loadUserWithSummary()

      return data
    } catch (error) {
      accessToken.value = null
      user.value = null
      localStorage.removeItem('accessToken')
      localStorage.removeItem('user')
      throw error
    }
  }

  async function refreshUser() {
    try {
      if (!accessToken.value) return

      await loadUserWithSummary()

    } catch (error) {
      console.error('Failed to refresh user:', error)
    }
  }

  async function logoutUser() {
    try {
      if (accessToken.value) {
        await apiLogout()
      }
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      accessToken.value = null
      user.value = null
      localStorage.removeItem('accessToken')
      localStorage.removeItem('user')
      router.push('/login')
    }
  }

  return {
    accessToken,
    user,
    isAuthenticated,
    loginUser,
    logoutUser,
    refreshUser,
  }
})
