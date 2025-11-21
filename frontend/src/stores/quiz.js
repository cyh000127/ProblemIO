import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useQuizStore = defineStore('quiz', () => {
  // Quiz Creation State
  const quizForm = ref({
    title: '',
    description: '',
    thumbnailUrl: '',
    questions: [],
  })

  // Quiz Play State
  const currentQuiz = ref(null)
  const currentQuestionIndex = ref(0)
  const userAnswers = ref([])
  const quizResult = ref(null)

  // Actions
  function addQuestion() {
    quizForm.value.questions.push({
      questionOrder: quizForm.value.questions.length + 1,
      description: '',
      imageUrl: '',
      answers: [],
    })
  }

  function removeQuestion(index) {
    quizForm.value.questions.splice(index, 1)
    // 순서 재정렬
    quizForm.value.questions.forEach((q, idx) => {
      q.questionOrder = idx + 1
    })
  }

  function resetQuizForm() {
    quizForm.value = {
      title: '',
      description: '',
      thumbnailUrl: '',
      questions: [],
    }
  }

  function startQuiz(quiz) {
    currentQuiz.value = quiz
    currentQuestionIndex.value = 0
    userAnswers.value = []
    quizResult.value = null
  }

  function submitAnswer(answer) {
    userAnswers.value.push(answer)
  }

  function nextQuestion() {
    if (currentQuestionIndex.value < currentQuiz.value.questions.length - 1) {
      currentQuestionIndex.value++
    }
  }

  function setQuizResult(result) {
    quizResult.value = result
  }

  function resetQuizPlay() {
    currentQuiz.value = null
    currentQuestionIndex.value = 0
    userAnswers.value = []
    quizResult.value = null
  }

  return {
    // State
    quizForm,
    currentQuiz,
    currentQuestionIndex,
    userAnswers,
    quizResult,
    // Actions
    addQuestion,
    removeQuestion,
    resetQuizForm,
    startQuiz,
    submitAnswer,
    nextQuestion,
    setQuizResult,
    resetQuizPlay,
  }
})
