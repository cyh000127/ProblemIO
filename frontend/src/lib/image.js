const apiBaseRaw = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api').replace(/\/+$/, '')
const apiBaseUrl = apiBaseRaw.endsWith('/api') ? apiBaseRaw : `${apiBaseRaw}`
const staticBaseUrl = apiBaseUrl.replace(/\/api$/, '')

export const resolveImageUrl = (url) => {
  if (!url) return url
  if (url.startsWith('http')) return url
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
