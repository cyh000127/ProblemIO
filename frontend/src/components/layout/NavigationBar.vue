<template>
    <Menubar :model="menuItems" class="border-bottom-1 surface-border">
      <template #start>
        <router-link to="/" class="flex align-items-center gap-2 no-underline text-primary">
          <i class="pi pi-home text-2xl"></i>
          <span class="text-xl font-bold">Problem.io</span>
        </router-link>
      </template>
      <template #end>
        <div class="flex align-items-center gap-3">
          <!-- 검색 바 -->
          <span class="p-input-icon-left">
            <i class="pi pi-search" />
            <InputText
              v-model="searchKeyword"
              placeholder="Search quizzes..."
              class="w-20rem"
              @keyup.enter="handleSearch"
            />
          </span>
  
          <!-- 비로그인 상태 -->
          <template v-if="!authStore.isAuthenticated">
            <Button label="Create Quiz" icon="pi pi-plus" text @click="handleCreateQuiz" />
            <Button label="My Page" icon="pi pi-user" text @click="handleMyPage" />
            <Button label="Login" icon="pi pi-sign-in" @click="goToLogin" />
            <Button label="Signup" severity="secondary" @click="goToSignup" />
          </template>
  
          <!-- 로그인 상태 -->
          <template v-else>
            <Button label="Create Quiz" icon="pi pi-plus" @click="goToCreateQuiz" />
            <Button
              icon="pi pi-user"
              :label="authStore.user?.nickname || 'My Page'"
              @click="goToMyPage"
              class="flex align-items-center gap-2"
            >
              <Avatar
                :image="authStore.user?.profileImageUrl"
                :label="authStore.user?.nickname?.charAt(0)"
                shape="circle"
                size="small"
              />
            </Button>
            <Button label="Logout" icon="pi pi-sign-out" severity="danger" text @click="handleLogout" />
          </template>
        </div>
      </template>
    </Menubar>
  </template>
  
  <script setup lang="ts">
  import { ref } from 'vue'
  import { useRouter } from 'vue-router'
  import { useAuthStore } from '@/stores/auth'
  import { useToast } from 'primevue/usetoast'
  import { useConfirm } from 'primevue/useconfirm'
  
  const router = useRouter()
  const authStore = useAuthStore()
  const toast = useToast()
  const confirm = useConfirm()
  
  const searchKeyword = ref('')
  
  const menuItems = []
  
  const handleSearch = () => {
    if (searchKeyword.value.trim()) {
      router.push({ name: 'search', query: { q: searchKeyword.value } })
    }
  }

  const handleCreateQuiz = () => {
    if (!authStore.isAuthenticated) {
      confirm.require({
        message: '로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?',
        header: 'Login Required',
        icon: 'pi pi-exclamation-triangle',
        accept: () => {
          router.push('/login')
        },
      })
    } else {
      router.push('/quiz/create')
    }
  }
  
  const handleMyPage = () => {
    if (!authStore.isAuthenticated) {
      confirm.require({
        message: '로그인이 필요합니다. 로그인 페이지로 이동하시겠습니까?',
        header: 'Login Required',
        icon: 'pi pi-exclamation-triangle',
        accept: () => {
          router.push('/login')
        },
      })
    } else {
      router.push('/mypage')
    }
  }
  
  const goToLogin = () => {
    router.push('/login')
  }
  
  const goToSignup = () => {
    router.push('/signup')
  }
  
  const goToCreateQuiz = () => {
    router.push('/quiz/create')
  }
  
  const goToMyPage = () => {
    router.push('/mypage')
  }
  
  const handleLogout = () => {
    confirm.require({
      message: '로그아웃 하시겠습니까?',
      header: 'Logout',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        authStore.logoutUser()
        toast.add({
          severity: 'success',
          summary: 'Success',
          detail: '로그아웃되었습니다.',
          life: 3000,
        })
      },
    })
  }
  </script>
  
  <style scoped>
  .w-20rem {
    width: 20rem;
  }
  </style>