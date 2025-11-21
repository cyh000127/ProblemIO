<template>
    <div class="quiz-result-container">
      <div class="container mx-auto px-4 max-w-4xl">
        <div v-if="!quizStore.quizResult" class="text-center py-8">
          <i class="pi pi-spin pi-spinner text-4xl"></i>
        </div>
  
        <div v-else class="flex flex-column gap-6">
          <!-- Result Summary -->
          <Card>
            <template #content>
              <div class="text-center flex flex-column gap-6">
                <div class="inline-flex align-items-center justify-content-center w-28 h-28 border-round-full bg-primary-50">
                  <i class="pi pi-trophy text-primary text-6xl"></i>
                </div>
  
                <div>
                  <h2 class="text-3xl md:text-4xl font-bold mb-4">
                    {{ resultMessage }}
                  </h2>
                  <p class="text-6xl md:text-7xl font-bold text-primary mb-4">
                    {{ scorePercentage }}%
                  </p>
                  <p class="text-xl text-color-secondary">
                    You got <span class="font-bold">{{ correctCount }}</span> out of
                    <span class="font-bold">{{ totalQuestions }}</span> correct!
                  </p>
                </div>
  
                <div class="flex flex-column sm:flex-row gap-4 justify-content-center">
                  <Button
                    label="Try Again"
                    icon="pi pi-refresh"
                    @click="tryAgain"
                    size="large"
                  />
                  <Button
                    label="More Quizzes"
                    icon="pi pi-list"
                    severity="secondary"
                    outlined
                    size="large"
                    @click="goToHome"
                  />
                </div>
              </div>
            </template>
          </Card>
  
          <!-- Per Question Results -->
          <Card>
            <template #header>
              <h2 class="text-2xl font-bold p-4 m-0">Question Results</h2>
            </template>
            <template #content>
              <div class="flex flex-column gap-6">
                <div
                  v-for="(item, index) in quizStore.quizResult.details"
                  :key="index"
                  class="p-4 border-1 surface-border border-round"
                >
                  <div class="flex align-items-center justify-content-between">
                    <h3 class="text-xl font-bold m-0">
                      Question {{ index + 1 }}
                    </h3>
                    <span
                      :class="[
                        'font-bold',
                        item.isCorrect ? 'text-green-600' : 'text-red-600',
                      ]"
                    >
                      {{ item.isCorrect ? 'Correct' : 'Incorrect' }}
                    </span>
                  </div>
  
                  <div class="mt-4">
                    <img
                      :src="item.imageUrl"
                      alt="Question Image"
                      class="w-full border-round shadow-2"
                    />
                  </div>
  
                  <div class="mt-4">
                    <p class="font-semibold">Your Answer:</p>
                    <p>{{ item.userAnswer }}</p>
                  </div>
  
                  <div class="mt-2">
                    <p class="font-semibold">Correct Answer:</p>
                    <p>{{ item.correctAnswer }}</p>
                  </div>
                </div>
              </div>
            </template>
          </Card>
        </div>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { computed } from 'vue'
  import { useRouter, useRoute } from 'vue-router'
  import { useQuizStore } from '@/stores/quiz'
  import Button  from 'primevue/button'
  import Card from 'primevue/card'
  
  const quizStore = useQuizStore()
  const router = useRouter()
  const route = useRoute()
  
  // Derived Data
  const totalQuestions = computed(() => quizStore.quizResult?.totalQuestions || 0)
  const correctCount = computed(() => quizStore.quizResult?.correctCount || 0)
  const scorePercentage = computed(() =>
    totalQuestions.value ? Math.round((correctCount.value / totalQuestions.value) * 100) : 0
  )
  
  const resultMessage = computed(() => {
    const score = scorePercentage.value
    if (score === 100) return 'Perfect Score!'
    if (score >= 80) return 'Excellent!'
    if (score >= 60) return 'Good Job!'
    return 'Keep Practicing!'
  })
  
  // Actions
  const tryAgain = () => {
    router.push(`/quiz/${route.params.id}/play`)
  }
  
  const goToHome = () => {
    router.push('/')
  }
  </script>
  
  <style scoped>
  .container {
    max-width: 1200px;
    margin: 0 auto;
  }
  </style>
  