import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout as apiLogout, getMe } from '@/api/auth'
import { useRouter } from 'vue-router'

export const useAuthStore = defineStore('auth', () => {
  const router = useRouter()
  
  // State
  const accessToken = ref(localStorage.getItem('accessToken') || null)
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const isAuthenticated = computed(() => !!accessToken.value && !!user.value)

  // Actions
  async function loginUser(email, password) {
    try {
      const response = await login(email, password)
      accessToken.value = response.accessToken
      user.value = response.user || null
      
      // localStorage에 저장
      localStorage.setItem('accessToken', accessToken.value)
      localStorage.setItem('user', JSON.stringify(user.value))
      
      return response
    } catch (error) {
      throw error
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
      // 상태 초기화
      accessToken.value = null
      user.value = null
      localStorage.removeItem('accessToken')
      localStorage.removeItem('user')
      
      // 로그인 페이지로 이동
      router.push('/login')
    }
  }

  async function refreshUser() {
    try {
      const userData = await getMe()
      user.value = userData
      localStorage.setItem('user', JSON.stringify(userData))
    } catch (error) {
      console.error('Failed to refresh user:', error)
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
