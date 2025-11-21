<template>
    <div class="quiz-play-container">
      <div class="container mx-auto px-4 max-w-4xl">
        <div v-if="!quizStore.currentQuiz" class="text-center py-8">
          <i class="pi pi-spin pi-spinner text-4xl"></i>
        </div>
  
        <div v-else class="flex flex-column gap-6">
          <div class="mb-4">
            <div class="flex justify-content-between align-items-center mb-2">
              <h2 class="text-2xl font-bold m-0">{{ quizStore.currentQuiz.title }}</h2>
              <Badge
                :value="`${quizStore.currentQuestionIndex + 1} / ${quizStore.currentQuiz.questions.length}`"
                severity="info"
              />
            </div>
            <ProgressBar
              :value="progressPercentage"
              class="h-1rem"
            />
          </div>
  
          <Card>
            <template #content>
              <div class="flex flex-column gap-6">
                <div v-if="currentQuestion.imageUrl" class="aspect-video bg-surface-100 overflow-hidden border-round">
                  <img
                    :src="currentQuestion.imageUrl"
                    :alt="currentQuestion.description"
                    class="w-full h-full object-cover"
                  />
                </div>
  
                <div v-if="currentQuestion.description" class="text-xl">
                  {{ currentQuestion.description }}
                </div>
  
                <div class="flex flex-column gap-3">
                  <label class="text-lg font-semibold">Your Answer:</label>
                  <InputText
                    v-model="currentAnswer"
                    placeholder="Enter your answer..."
                    class="w-full"
                    @keyup.enter="submitAnswer"
                  />
                </div>
  
                <div class="flex gap-3">
                  <Button
                    v-if="quizStore.currentQuestionIndex > 0"
                    label="Previous"
                    icon="pi pi-arrow-left"
                    severity="secondary"
                    outlined
                    @click="previousQuestion"
                  />
                  <Button
                    v-if="quizStore.currentQuestionIndex < quizStore.currentQuiz.questions.length - 1"
                    label="Next"
                    icon="pi pi-arrow-right"
                    iconPos="right"
                    class="flex-1"
                    :disabled="!currentAnswer.trim()"
                    @click="nextQuestion"
                  />
                  <Button
                    v-else
                    label="Submit Quiz"
                    icon="pi pi-check"
                    class="flex-1"
                    :disabled="!currentAnswer.trim()"
                    :loading="submitting"
                    @click="submitQuiz"
                  />
                </div>
              </div>
            </template>
          </Card>
        </div>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, computed, onMounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { useToast } from 'primevue/usetoast'
  import { useQuizStore } from '@/stores/quiz'
  import { submitQuiz as submitQuizAPI } from '@/api/submission'
  
  const route = useRoute()
  const router = useRouter()
  const toast = useToast()
  const quizStore = useQuizStore()
  
  const currentAnswer = ref('')
  const submitting = ref(false)
  
  const currentQuestion = computed(() => {
    if (!quizStore.currentQuiz) return null
    return quizStore.currentQuiz.questions[quizStore.currentQuestionIndex]
  })
  
  const progressPercentage = computed(() => {
    if (!quizStore.currentQuiz) return 0
    return ((quizStore.currentQuestionIndex + 1) / quizStore.currentQuiz.questions.length) * 100
  })
  
  const submitAnswer = () => {
    if (!currentAnswer.value.trim()) return
  
    quizStore.submitAnswer({
      questionId: currentQuestion.value.id,
      answerText: currentAnswer.value.trim(),
    })
  
    if (quizStore.currentQuestionIndex < quizStore.currentQuiz.questions.length - 1) {
      nextQuestion()
    } else {
      submitQuiz()
    }
  }
  
  const nextQuestion = () => {
    if (currentAnswer.value.trim()) {
      quizStore.submitAnswer({
        questionId: currentQuestion.value.id,
        answerText: currentAnswer.value.trim(),
      })
    }
    quizStore.nextQuestion()
    currentAnswer.value = ''
  }
  
  const previousQuestion = () => {
    if (quizStore.currentQuestionIndex > 0) {
      quizStore.currentQuestionIndex--
      // 이전 답변 복원
      const prevAnswer = quizStore.userAnswers[quizStore.currentQuestionIndex]
      currentAnswer.value = prevAnswer?.answerText || ''
    }
  }
  
  const submitQuiz = async () => {
    if (!currentAnswer.value.trim()) {
      toast.add({
        severity: 'warn',
        summary: 'Warning',
        detail: 'Please enter an answer',
        life: 3000,
      })
      return
    }
  
    // 마지막 답변 저장
    quizStore.submitAnswer({
      questionId: currentQuestion.value.id,
      answerText: currentAnswer.value.trim(),
    })
  
    submitting.value = true
    try {
      const result = await submitQuizAPI(Number(route.params.id), quizStore.userAnswers)
      quizStore.setQuizResult(result)
      router.push(`/quiz/${route.params.id}/result`)
    } catch (error: any) {
      toast.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Failed to submit quiz',
        life: 3000,
      })
    } finally {
      submitting.value = false
    }
  }
  
  onMounted(() => {
    if (!quizStore.currentQuiz) {
      router.push('/')
    }
  })
  </script>
  
  <style scoped>
  .quiz-play-container {
    min-height: calc(100vh - 200px);
  }
  
  .container {
    max-width: 1200px;
    margin: 0 auto;
  }
  
  .aspect-video {
    aspect-ratio: 16 / 9;
  }
  
  .h-1rem {
    height: 1rem;
  }
  </style>