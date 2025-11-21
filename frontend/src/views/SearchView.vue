<template>
    <div class="search-container">
      <div class="container mx-auto px-4">
        <div class="mb-6">
          <h1 class="text-3xl font-bold mb-2">Search Results</h1>
          <p class="text-color-secondary" v-if="searchKeyword">
            Results for: <span class="font-semibold">{{ searchKeyword }}</span>
          </p>
        </div>
  
        <div v-if="loading" class="text-center py-8">
          <i class="pi pi-spin pi-spinner text-4xl"></i>
        </div>
  
        <div v-else-if="quizzes.length === 0" class="text-center py-8">
          <p class="text-color-secondary text-xl">No quizzes found</p>
        </div>
  
        <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
          <Card
            v-for="quiz in quizzes"
            :key="quiz.id"
            class="quiz-card cursor-pointer"
            @click="goToQuiz(quiz.id)"
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
                <div class="flex align-items-center gap-2">
                  <Avatar
                    :image="quiz.author?.profileImageUrl"
                    :label="quiz.author?.nickname?.charAt(0)"
                    shape="circle"
                    size="small"
                  />
                  <span class="text-sm text-color-secondary">{{ quiz.author?.nickname }}</span>
                </div>
                <h3 class="text-xl font-bold m-0">{{ quiz.title }}</h3>
                <p class="text-color-secondary m-0 line-clamp-2">{{ quiz.description }}</p>
                <div class="flex align-items-center gap-4 text-sm text-color-secondary">
                  <span><i class="pi pi-heart"></i> {{ quiz.likeCount || 0 }}</span>
                  <span><i class="pi pi-play"></i> {{ quiz.playCount || 0 }}</span>
                </div>
              </div>
            </template>
          </Card>
        </div>
  
        <Paginator
          v-if="totalPages > 1"
          :first="(currentPage - 1) * pageSize"
          :rows="pageSize"
          :totalRecords="totalElements"
          @page="onPageChange"
        />
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, onMounted, watch } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { useToast } from 'primevue/usetoast'
  import { getQuizzes } from '@/api/quiz'
  
  const route = useRoute()
  const router = useRouter()
  const toast = useToast()
  
  const quizzes = ref([])
  const loading = ref(false)
  const searchKeyword = ref('')
  const currentPage = ref(1)
  const pageSize = ref(12)
  const totalPages = ref(0)
  const totalElements = ref(0)
  
  const loadQuizzes = async () => {
    if (!searchKeyword.value) {
      quizzes.value = []
      return
    }
  
    loading.value = true
    try {
      const response = await getQuizzes({
        page: currentPage.value,
        size: pageSize.value,
        sort: 'latest',
        keyword: searchKeyword.value,
      })
      quizzes.value = response.content || []
      totalPages.value = response.totalPages || 0
      totalElements.value = response.totalElements || 0
    } catch (error: any) {
      toast.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Failed to load search results',
        life: 3000,
      })
    } finally {
      loading.value = false
    }
  }
  
  const goToQuiz = (quizId: number) => {
    router.push(`/quiz/${quizId}`)
  }
  
  const onPageChange = (event: any) => {
    currentPage.value = event.page + 1
    loadQuizzes()
  }
  
  watch(
    () => route.query.q,
    (newQuery) => {
      searchKeyword.value = (newQuery as string) || ''
      currentPage.value = 1
      if (searchKeyword.value) {
        loadQuizzes()
      }
    },
    { immediate: true }
  )
  </script>
  
  <style scoped>
  .search-container {
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
  
  .line-clamp-2 {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  </style>