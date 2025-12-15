import instance from './axios'

// 관리자용 퀴즈 목록 조회 (숨김 포함)
export const getAdminQuizzes = async (params) => {
  const response = await instance.get('/admin/quizzes', { params })
  return response.data
}

// 퀴즈 숨김 처리 토글
export const toggleQuizVisibility = async (quizId) => {
  const response = await instance.patch(`/admin/quizzes/${quizId}/hide`)
  return response.data
}

// 챌린지 생성
export const createChallenge = async (challengeData) => {
  const response = await instance.post('/admin/challenges', challengeData)
  return response.data
}
