<template>
    <div class="my-quizzes-container">
      <div class="container mx-auto px-4">
        <div class="flex justify-content-between align-items-center mb-6">
          <h1 class="text-3xl font-bold m-0">My Quizzes</h1>
          <Button
            label="Create New Quiz"
            icon="pi pi-plus"
            @click="goToCreateQuiz"
          />
        </div>
  
        <div v-if="loading" class="text-center py-8">
          <i class="pi pi-spin pi-spinner text-4xl"></i>
        </div>
  
        <div v-else-if="quizzes.length === 0" class="text-center py-8 text-color-secondary">
          <p class="text-xl mb-4">No quizzes yet</p>
          <Button
            label="Create Your First Quiz"
            icon="pi pi-plus"
            @click="goToCreateQuiz"
          />
        </div>
  
        <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <Card
            v-for="quiz in quizzes"
            :key="quiz.id"
            class="quiz-card"
          >
            <template #header>
              <div class="aspect-video bg-surface-100 overflow-hidden">
                <img
                  :src="quiz.thumbnailUrl || '/placeholder.svg'"
                  :alt="quiz.title"
                  class="w-full h-full object-cover"
                />
              </div>
            </template>
            <template #content>
              <div class="flex flex-column gap-3">
                <h3 class="text-xl font-bold m-0">{{ quiz.title }}</h3>
                <div class="flex align-items-center gap-4 text-sm text-color-secondary">
                  <span><i class="pi pi-heart"></i> {{ quiz.likeCount || 0 }}</span>
                  <span><i class="pi pi-play"></i> {{ quiz.playCount || 0 }}</span>
                  <span><i class="pi pi-calendar"></i> {{ formatDate(quiz.createdAt) }}</span>
                </div>
                <div class="flex gap-2 mt-2">
                  <Button
                    label="View"
                    icon="pi pi-eye"
                    severity="secondary"
                    outlined
                    class="flex-1"
                    @click="goToQuiz(quiz.id)"
                  />
                  <Button
                    label="Edit"
                    icon="pi pi-pencil"
                    severity="secondary"
                    outlined
                    class="flex-1"
                    @click="goToEdit(quiz.id)"
                  />
                  <Button
                    label="Delete"
                    icon="pi pi-trash"
                    severity="danger"
                    outlined
                    @click="handleDelete(quiz.id)"
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
  import { ref, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { useToast } from 'primevue/usetoast'
  import { useConfirm } from 'primevue/useconfirm'
  import { getMyQuizzes, deleteQuiz } from '@/api/quiz'
  
  const router = useRouter()
  const toast = useToast()
  const confirm = useConfirm()
  
  const quizzes = ref([])
  const loading = ref(false)
  
  const loadQuizzes = async () => {
    loading.value = true
    try {
      quizzes.value = await getMyQuizzes()
    } catch (error: any) {
      toast.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Failed to load quizzes',
        life: 3000,
      })
    } finally {
      loading.value = false
    }
  }
  
  const goToQuiz = (quizId: number) => {
    router.push(`/quiz/${quizId}`)
  }
  
  const goToEdit = (quizId: number) => {
    router.push(`/quiz/edit/${quizId}`)
  }
  
  const goToCreateQuiz = () => {
    router.push('/quiz/create')
  }
  
  const handleDelete = (quizId: number) => {
    confirm.require({
      message: 'Are you sure you want to delete this quiz?',
      header: 'Delete Quiz',
      icon: 'pi pi-exclamation-triangle',
      accept: async () => {
        try {
          await deleteQuiz(quizId)
          toast.add({
            severity: 'success',
            summary: 'Success',
            detail: 'Quiz deleted successfully',
            life: 3000,
          })
          loadQuizzes()
        } catch (error: any) {
          toast.add({
            severity: 'error',
            summary: 'Error',
            detail: 'Failed to delete quiz',
            life: 3000,
          })
        }
      },
    })
  }
  
  const formatDate = (dateString: string) => {
    if (!dateString) return ''
    const date = new Date(dateString)
    return date.toLocaleDateString()
  }
  
  onMounted(() => {
    loadQuizzes()
  })
  </script>
  
  <style scoped>
  .my-quizzes-container {
    min-height: calc(100vh - 200px);
  }
  
  .container {
    max-width: 1200px;
    margin: 0 auto;
  }
  
  .aspect-video {
    aspect-ratio: 16 / 9;
  }
  
  .quiz-card {
    transition: transform 0.2s, box-shadow 0.2s;
  }
  
  .quiz-card:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  }
  </style>