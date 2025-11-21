// src/api/axios.js
import axios, { AxiosHeaders } from 'axios'

// API 기본 URL 설정 (환경변수로 관리 가능)
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

// Axios 인스턴스 생성
const apiClient = axios.create({
  baseURL: API_BASE_URL,
})

// 요청 인터셉터
apiClient.interceptors.request.use(
  (config) => {
    // headers 객체가 없으면 AxiosHeaders 인스턴스로 생성
    if (!config.headers) {
      config.headers = new AxiosHeaders()
    }

    // 토큰 추가
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.set('Authorization', `Bearer ${token}`)
    }

    // FormData 여부 판단
    const isFormData = config.data instanceof FormData

    if (!isFormData) {
      // config.headers['Content-Type'] = 'application/json' 과 동일
      if (!config.headers.has('Content-Type')) {
        config.headers.set('Content-Type', 'application/json')
      }
    }

    return config
  },
  (error) => Promise.reject(error)
)

// 응답 인터셉터
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default apiClient
