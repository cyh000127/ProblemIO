<template>
    <div class="quiz-detail-container">
      <div class="container mx-auto px-4">
        <div v-if="loading" class="text-center py-8">
          <i class="pi pi-spin pi-spinner text-4xl"></i>
        </div>
  
        <div v-else-if="quiz" class="flex flex-column gap-6">
          <Card>
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
              <div class="flex flex-column gap-4">
                <div class="flex justify-content-between align-items-start">
                  <div class="flex-1">
                    <h1 class="text-3xl font-bold mb-2">{{ quiz.title }}</h1>
                    <p class="text-color-secondary text-lg">{{ quiz.description }}</p>
                  </div>
                  <Button
                    :icon="isLiked ? 'pi pi-heart-fill' : 'pi pi-heart'"
                    :label="`${quiz.likeCount || 0}`"
                    :severity="isLiked ? undefined : 'secondary'"
                    :outlined="!isLiked"
                    @click="handleLike"
                  />
                </div>
  
                <div class="flex align-items-center gap-3">
                  <Avatar
                    :image="quiz.author?.profileImageUrl"
                    :label="quiz.author?.nickname?.charAt(0)"
                    shape="circle"
                  />
                  <div>
                    <p class="font-semibold m-0">{{ quiz.author?.nickname }}</p>
                    <p class="text-sm text-color-secondary m-0">
                      {{ quiz.questions?.length || 0 }} questions
                    </p>
                  </div>
                </div>
  
                <div class="flex gap-3">
                  <Button
                    label="Start Quiz"
                    icon="pi pi-play"
                    size="large"
                    class="flex-1"
                    @click="startQuiz"
                  />
                  <Button
                    v-if="authStore.isAuthenticated"
                    :label="isFollowed ? 'Following' : 'Follow'"
                    :icon="isFollowed ? 'pi pi-check' : 'pi pi-user-plus'"
                    :severity="isFollowed ? 'secondary' : undefined"
                    :outlined="isFollowed"
                    @click="handleFollow"
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
  import { ref, onMounted, computed } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { useToast } from 'primevue/usetoast'
  import { useConfirm } from 'primevue/useconfirm'
  import { useAuthStore } from '@/stores/auth'
  import { useQuizStore } from '@/stores/quiz'
  import { getQuiz, likeQuiz, unlikeQuiz } from '@/api/quiz'
  import { followUser, unfollowUser } from '@/api/user'
  
  const route = useRoute()
  const router = useRouter()
  const toast = useToast()
  const confirm = useConfirm()
  const authStore = useAuthStore()
  const quizStore = useQuizStore()
  
  const quiz = ref(null)
  const loading = ref(false)
  const isLiked = ref(false)
  const isFollowed = ref(false)
  
  const loadQuiz = async () => {
    loading.value = true
    try {
      const data = await getQuiz(Number(route.params.id))
      quiz.value = data
      isLiked.value = data.isLikedByMe || false
      isFollowed.value = data.isFollowedByMe || false
    } catch (error: any) {
      toast.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Failed to load quiz',
        life: 3000,
      })
    } finally {
      loading.value = false
    }
  }
  
  const handleLike = async () => {
    if (!authStore.isAuthenticated) {
      confirm.require({
        message: '로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?',
        header: 'Login Required',
        icon: 'pi pi-exclamation-triangle',
        accept: () => {
          router.push('/login')
        },
      })
      return
    }
  
    try {
      if (isLiked.value) {
        await unlikeQuiz(Number(route.params.id))
        isLiked.value = false
        quiz.value.likeCount = Math.max(0, (quiz.value.likeCount || 0) - 1)
      } else {
        await likeQuiz(Number(route.params.id))
        isLiked.value = true
        quiz.value.likeCount = (quiz.value.likeCount || 0) + 1
      }
    } catch (error: any) {
      toast.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Failed to toggle like',
        life: 3000,
      })
    }
  }
  
  const handleFollow = async () => {
    if (!authStore.isAuthenticated) {
      confirm.require({
        message: '로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?',
        header: 'Login Required',
        icon: 'pi pi-exclamation-triangle',
        accept: () => {
          router.push('/login')
        },
      })
      return
    }
  
    try {
      if (isFollowed.value) {
        await unfollowUser(quiz.value.author.id)
        isFollowed.value = false
      } else {
        await followUser(quiz.value.author.id)
        isFollowed.value = true
      }
    } catch (error: any) {
      toast.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Failed to toggle follow',
        life: 3000,
      })
    }
  }
  
  const startQuiz = () => {
    if (quiz.value) {
      quizStore.startQuiz(quiz.value)
      router.push(`/quiz/${route.params.id}/play`)
    }
  }
  
  onMounted(() => {
    loadQuiz()
  })
  </script>
  
  <style scoped>
  .quiz-detail-container {
    min-height: calc(100vh - 200px);
  }
  
  .container {
    max-width: 1200px;
    margin: 0 auto;
  }
  
  .aspect-video {
    aspect-ratio: 16 / 9;
  }
  </style>