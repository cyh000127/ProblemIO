import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// Lazy loading for better performance
const HomeView = () => import('@/views/HomeView.vue')
const SearchView = () => import('@/views/SearchView.vue')
const LoginView = () => import('@/views/auth/LoginView.vue')
const SignupView = () => import('@/views/auth/SignupView.vue')
const QuizDetailView = () => import('@/views/quiz/QuizDetailView.vue')
const QuizPlayView = () => import('@/views/quiz/QuizPlayView.vue')
const QuizResultView = () => import('@/views/quiz/QuizResultView.vue')
const QuizCreateView = () => import('@/views/quiz/QuizCreateView.vue')
const QuizEditView = () => import('@/views/quiz/QuizEditView.vue')
const UserProfileView = () => import('@/views/user/UserProfileView.vue')
const MyPageView = () => import('@/views/user/MyPageView.vue')
const MyQuizListView = () => import('@/views/user/MyQuizListView.vue')
const ProfileEditView = () => import('@/views/user/ProfileEditView.vue')

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // Auth Routes
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { requiresAuth: false },
    },
    {
      path: '/signup',
      name: 'signup',
      component: SignupView,
      meta: { requiresAuth: false },
    },
    // Main & Search
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: false },
    },
    {
      path: '/search',
      name: 'search',
      component: SearchView,
      meta: { requiresAuth: false },
    },
    // Quiz Routes
    {
      path: '/quiz/:id',
      name: 'quiz-detail',
      component: QuizDetailView,
      meta: { requiresAuth: false },
      props: true,
    },
    {
      path: '/quiz/:id/play',
      name: 'quiz-play',
      component: QuizPlayView,
      meta: { requiresAuth: false },
      props: true,
    },
    {
      path: '/quiz/:id/result',
      name: 'quiz-result',
      component: QuizResultView,
      meta: { requiresAuth: false },
      props: true,
    },
    {
      path: '/quiz/create',
      name: 'quiz-create',
      component: QuizCreateView,
      meta: { requiresAuth: true },
    },
    {
      path: '/quiz/edit/:id',
      name: 'quiz-edit',
      component: QuizEditView,
      meta: { requiresAuth: true },
      props: true,
    },
    // User Routes
    {
      path: '/users/:id',
      name: 'user-profile',
      component: UserProfileView,
      meta: { requiresAuth: false },
      props: true,
    },
    {
      path: '/mypage',
      name: 'mypage',
      component: MyPageView,
      meta: { requiresAuth: true },
    },
    {
      path: '/mypage/quizzes',
      name: 'my-quizzes',
      component: MyQuizListView,
      meta: { requiresAuth: true },
    },
    {
      path: '/mypage/edit',
      name: 'profile-edit',
      component: ProfileEditView,
      meta: { requiresAuth: true },
    },
    // Redirect old routes
    {
      path: '/feed',
      redirect: '/',
    },
    {
      path: '/auth/login',
      redirect: '/login',
    },
    {
      path: '/auth/signup',
      redirect: '/signup',
    },
    {
      path: '/create',
      redirect: '/quiz/create',
    },
    {
      path: '/profile',
      redirect: '/mypage',
    },
  ],
})

// Navigation Guard
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  // 인증이 필요한 라우트인지 확인
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    // 원래 가려던 경로를 query로 전달
    next({
      name: 'login',
      query: { redirect: to.fullPath },
    })
  } else {
    next()
  }
})

export default router