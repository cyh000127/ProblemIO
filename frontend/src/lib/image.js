const apiBaseRaw = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api').replace(/\/+$/, '')
const apiBaseUrl = apiBaseRaw.endsWith('/api') ? apiBaseRaw : `${apiBaseRaw}`
const staticBaseUrl = apiBaseUrl.replace(/\/api$/, '')

export const resolveImageUrl = (url) => {
  if (!url) return url
  if (url.startsWith('http') || url.startsWith('blob:') || url.startsWith('data:')) return url
  
  // 백엔드에서 온전한 S3 URL을 주지 않고 상대 경로만 준 경우 (예외적 상황)
  // staticBaseUrl(보통 localhost)을 붙이게 되는데, 
  // 만약 백엔드가 S3 URL 처리를 안했다면 여기서라도 S3 주소를 붙일 수도 있음.
  // 하지만 원칙적으로 백엔드 Serializer가 동작해야 함. 
  // 일단 localhost가 뜨는건 여기서 staticBaseUrl이 localhost이기 때문.
  const prefix = url.startsWith('/') ? '' : '/'
  return `${staticBaseUrl}${prefix}${url}`
}

export const resolveQuizImages = (quiz) => {
  if (!quiz) return quiz
  const resolved = { ...quiz }
  resolved.thumbnailUrl = resolveImageUrl(quiz.thumbnailUrl)

  if (quiz.questions) {
    resolved.questions = quiz.questions.map((q) => ({
      ...q,
      imageUrl: resolveImageUrl(q.imageUrl),
    }))
  }
  return resolved
}
