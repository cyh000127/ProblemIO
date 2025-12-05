<template>
  <div class="home-container">
    <div class="container page-container">
      <div class="home-controls mb-6">
        <div class="flex gap-1 filter-group">
          <Button icon="pi pi-heart" :label="'인기'" :class="['filter-button', { 'is-active': sort === 'popular' }]" :severity="sort === 'popular' ? undefined : 'secondary'" :outlined="sort !== 'popular'" @click="sort = 'popular'" />
          <Button icon="pi pi-eye" :label="'조회'" :class="['filter-button', { 'is-active': sort === 'views' }]" :severity="sort === 'views' ? undefined : 'secondary'" :outlined="sort !== 'views'" @click="sort = 'views'" />
          <Button icon="pi pi-clock" :label="'최신'" :class="['filter-button', { 'is-active': sort === 'latest' }]" :severity="sort === 'latest' ? undefined : 'secondary'" :outlined="sort !== 'latest'" @click="sort = 'latest'" />
        </div>
        <span class="home-search">
          <InputText v-model="searchKeyword" placeholder="Search quizzes..." class="search-bar" @keyup.enter="handleSearch" />
        </span>
      </div>

      <div v-if="loading" class="text-center py-8">
        <i class="pi pi-spin pi-spinner text-4xl"></i>
      </div>

      <div v-else-if="quizzes.length === 0" class="text-center py-8 empty-state">
        <p class="text-color-secondary text-xl">No quizzes found</p>
      </div>

      <div v-else class="quiz-grid-container mb-6">
        <div v-for="quiz in quizzes" :key="quiz.id" class="quiz-card cursor-pointer" @click="goToQuiz(quiz.id)">
          <div class="quiz-thumbnail">
            <img :src="quiz.thumbnailUrl || '/placeholder.svg'" :alt="quiz.title" class="quiz-thumbnail-img" />
          </div>
          <div class="quiz-meta">
            <h3 class="quiz-title">{{ quiz.title }}</h3>
            <div class="quiz-stats">
              <div class="quiz-stat">
                <i class="pi pi-heart text-xs"></i>
                <span>{{ quiz.likeCount || 0 }}</span>
              </div>
              <div class="quiz-stat view">
                <i class="pi pi-eye text-xs"></i>
                <span>{{ quiz.playCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <Paginator v-if="totalPages > 1" :first="(currentPage - 1) * pageSize" :rows="pageSize" :totalRecords="totalElements" @page="onPageChange" class="mb-6" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import { useRouter } from "vue-router";
import { useToast } from "primevue/usetoast";
import { getQuizzes } from "@/api/quiz";

const router = useRouter();
const toast = useToast();

const quizzes = ref([]);
const searchKeyword = ref("");
const loading = ref(false);
const sort = ref("popular");
const currentPage = ref(1);
const pageSize = ref(12);
const totalPages = ref(0);
const totalElements = ref(0);

const loadQuizzes = async () => {
  loading.value = true;
  try {
    const response = await getQuizzes({
      page: currentPage.value,
      size: pageSize.value,
      sort: sort.value,
    });
    quizzes.value = response.content || [];
    totalPages.value = response.totalPages || 0;
    totalElements.value = response.totalElements || 0;
  } catch (error: any) {
    toast.add({
      severity: "error",
      summary: "Error",
      detail: "Failed to load quizzes",
      life: 3000,
    });
  } finally {
    loading.value = false;
  }
};

const goToQuiz = (quizId: number) => {
  router.push(`/quiz/${quizId}`);
};

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({ name: "search", query: { q: searchKeyword.value } });
  }
};

const onPageChange = (event: any) => {
  currentPage.value = event.page + 1;
  loadQuizzes();
};

watch(sort, () => {
  currentPage.value = 1;
  loadQuizzes();
});

onMounted(() => {
  loadQuizzes();
});
</script>

<style scoped>
.home-container {
  min-height: calc(100vh - 200px);
  padding: 1rem 0 2rem;
}

.container {
  width: 100%;
}

.page-container {
  max-width: 1500px;
  margin: 0 auto;
  padding: 0 12px;
}

/* Filters + Search in one line */
.home-controls {
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  gap: 1rem;
  padding: 0.5rem 0 1.5rem;
  width: 100%;
}

.filter-group {
  display: inline-flex;
  gap: 0.5rem;
  flex-wrap: nowrap;
}

.filter-button {
  color: #111827 !important;
  transition: background-color 0.15s ease, border-color 0.15s ease, color 0.15s ease;
}

:global(.filter-button.is-active) {
  background: var(--color-primary) !important;
  border-color: var(--color-primary) !important;
  color: #ffffff !important;
}

:global([data-theme="light"] .filter-button.is-active) {
  background: #16c6b0 !important;
  border-color: #13b8a3 !important;
  box-shadow: 0 10px 26px rgba(19, 184, 163, 0.26);
}

:global([data-theme="dark"] .filter-button.is-active) {
  background: #0fb397 !important;
  border-color: #1abc9c !important;
  color: #ffffff !important;
  box-shadow: 0 10px 28px rgba(16, 185, 129, 0.35);
}

:global(.filter-button.is-active:hover) {
  background: var(--color-primary-hover) !important;
  border-color: var(--color-primary-hover) !important;
  color: #ffffff !important;
}

:global([data-theme="light"] .filter-button.is-active:hover) {
  background: #12b4a1 !important;
  border-color: #12b4a1 !important;
}

:global([data-theme="dark"] .filter-button) {
  color: #e5e7eb !important;
  border-color: #374151 !important;
  background: rgba(255, 255, 255, 0.04) !important;
}

:global([data-theme="dark"] .filter-button.p-button-outlined) {
  border-color: #4b5563 !important;
  color: #e5e7eb !important;
}

.filter-button:hover {
  background: rgba(0, 150, 136, 0.1) !important;
  border-color: var(--color-primary) !important;
  color: var(--color-heading) !important;
}

:global([data-theme="dark"] .filter-button:hover) {
  background: rgba(26, 188, 156, 0.18) !important;
  border-color: #34d399 !important;
  color: #f9fafb !important;
}

.home-search {
  width: 100%;
  display: block;
}

.search-bar {
  width: 100%;
  max-width: 720px;
  min-width: 360px;
}

/* Grid */
.quiz-grid-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
  padding: 0.5rem;
  width: 100%;
}

@media (max-width: 1200px) {
  .quiz-grid-container {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 960px) {
  .quiz-grid-container {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .home-controls {
    grid-template-columns: 1fr;
    max-width: 100%;
  }
  .search-bar {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .quiz-grid-container {
    grid-template-columns: 1fr;
  }
}

.quiz-thumbnail {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 14px;
  background: linear-gradient(180deg, #eef3f6, #f7ede8);
}

:global([data-theme="dark"] .quiz-thumbnail) {
  background: #0f0f0f;
  border: 1px solid #2d2d2d;
}

.quiz-thumbnail-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  image-rendering: auto;
  transition: transform 0.2s ease;
}

.quiz-card {
  background: var(--color-background-soft);
  border-radius: 18px;
  border: 1px solid var(--color-border);
  padding: 0.6rem 0.6rem 0.3rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  box-shadow: var(--surface-glow);
  transition: transform 0.2s, box-shadow 0.2s;
}

:global([data-theme="dark"] .quiz-card) {
  background: #161616;
  border: 1px solid #262626;
  box-shadow:
    0 16px 40px rgba(0, 0, 0, 0.45),
    inset 0 1px 0 rgba(255, 255, 255, 0.02);
}

.quiz-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 28px rgba(0, 0, 0, 0.08);
}

:global([data-theme="dark"] .quiz-card:hover) {
  background: #1e1e1e;
  box-shadow:
    0 20px 48px rgba(0, 0, 0, 0.55),
    0 0 0 1px rgba(255, 255, 255, 0.03);
}

.quiz-card:hover .quiz-thumbnail-img {
  transform: scale(1.03);
}

.quiz-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.5rem;
  padding: 0 0.4rem 0.4rem;
}

.quiz-title {
  font-size: 1rem;
  font-weight: 700;
  margin: 0;
  color: var(--color-heading);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.quiz-stat {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.3rem 0.6rem;
  border-radius: 999px;
  background: rgba(137, 168, 124, 0.15);
  color: var(--color-heading);
  font-size: 0.85rem;
}

.quiz-stats {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
}

.quiz-stat.view {
  background: rgba(59, 130, 246, 0.12);
}

.empty-state {
  background: rgba(244, 241, 236, 0.6);
  border-radius: 14px;
  padding: 2rem;
}
</style>
