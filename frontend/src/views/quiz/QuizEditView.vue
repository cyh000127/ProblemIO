<template>
    <div class="quiz-edit-container">
      <div class="container mx-auto px-4 max-w-4xl">
        <div v-if="loading" class="text-center py-8">
          <i class="pi pi-spin pi-spinner text-4xl"></i>
        </div>
  
        <Card v-else>
          <template #header>
            <div class="p-4 border-bottom-1 surface-border">
              <h1 class="text-3xl font-bold m-0">Edit Quiz</h1>
            </div>
          </template>
          <template #content>
            <div class="flex flex-column gap-6">
              <!-- Quiz Meta Info -->
              <div class="flex flex-column gap-4">
                <h2 class="text-2xl font-bold">Quiz Information</h2>
                
                <div class="flex flex-column gap-2">
                  <label class="text-sm font-medium">Title *</label>
                  <InputText
                    v-model="quizForm.title"
                    placeholder="Enter quiz title"
                    class="w-full"
                  />
                </div>
  
                <div class="flex flex-column gap-2">
                  <label class="text-sm font-medium">Description</label>
                  <Textarea
                    v-model="quizForm.description"
                    placeholder="Describe your quiz"
                    rows="3"
                    class="w-full"
                  />
                </div>
  
                <div class="flex flex-column gap-2">
                  <label class="text-sm font-medium">Thumbnail URL</label>
                  <InputText
                    v-model="quizForm.thumbnailUrl"
                    placeholder="https://example.com/image.jpg"
                    class="w-full"
                  />
                </div>
              </div>
  
              <Divider />
  
              <!-- Questions List -->
              <div class="flex flex-column gap-4">
                <div class="flex justify-content-between align-items-center">
                  <h2 class="text-2xl font-bold m-0">Questions</h2>
                  <Button
                    label="Add Question"
                    icon="pi pi-plus"
                    @click="addQuestion"
                  />
                </div>
  
                <Card
                  v-for="(question, index) in quizForm.questions"
                  :key="index"
                  class="question-card"
                >
                  <template #header>
                    <div class="p-3 border-bottom-1 surface-border flex justify-content-between align-items-center">
                      <span class="font-semibold">Question {{ question.questionOrder }}</span>
                      <Button
                        icon="pi pi-trash"
                        severity="danger"
                        text
                        rounded
                        @click="removeQuestion(index)"
                      />
                    </div>
                  </template>
                  <template #content>
                    <div class="flex flex-column gap-4">
                      <div class="flex flex-column gap-2">
                        <label class="text-sm font-medium">Question Description</label>
                        <Textarea
                          v-model="question.description"
                          placeholder="Enter question description"
                          rows="2"
                          class="w-full"
                        />
                      </div>
  
                      <div class="flex flex-column gap-2">
                        <label class="text-sm font-medium">Image URL</label>
                        <InputText
                          v-model="question.imageUrl"
                          placeholder="https://example.com/question-image.jpg"
                          class="w-full"
                        />
                      </div>
  
                      <div class="flex flex-column gap-2">
                        <label class="text-sm font-medium">Correct Answers *</label>
                        <Chips
                          v-model="question.answers"
                          placeholder="Type answer and press Enter"
                          class="w-full"
                        />
                      </div>
                    </div>
                  </template>
                </Card>
              </div>
  
              <Divider />
  
              <!-- Submit Button -->
              <div class="flex justify-content-end gap-3">
                <Button
                  label="Cancel"
                  severity="secondary"
                  outlined
                  @click="handleCancel"
                />
                <Button
                  label="Save Changes"
                  icon="pi pi-check"
                  :loading="submitting"
                  :disabled="!isFormValid"
                  @click="handleSubmit"
                />
              </div>
            </div>
          </template>
        </Card>
      </div>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref, computed, onMounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  import { useToast } from 'primevue/usetoast'
  import { useQuizStore } from '@/stores/quiz'
  import { getQuiz, updateQuiz } from '@/api/quiz'
  
  const route = useRoute()
  const router = useRouter()
  const toast = useToast()
  const quizStore = useQuizStore()
  
  const loading = ref(false)
  const submitting = ref(false)
  
  const quizForm = computed(() => quizStore.quizForm)
  
  const isFormValid = computed(() => {
    if (!quizForm.value.title?.trim()) return false
    if (quizForm.value.questions.length === 0) return false
    
    return quizForm.value.questions.every((q) => {
      return q.answers && q.answers.length > 0
    })
  })
  
  const loadQuiz = async () => {
    loading.value = true
    try {
      const quiz = await getQuiz(Number(route.params.id))
      quizForm.value.title = quiz.title
      quizForm.value.description = quiz.description
      quizForm.value.thumbnailUrl = quiz.thumbnailUrl
      quizForm.value.questions = quiz.questions.map((q) => ({
        id: q.id,
        questionOrder: q.order,
        description: q.description,
        imageUrl: q.imageUrl,
        answers: q.answers?.map((a) => a.text) || [],
      }))
    } catch (error: any) {
      toast.add({
        severity: 'error',
        summary: 'Error',
        detail: 'Failed to load quiz',
        life: 3000,
      })
      router.push('/')
    } finally {
      loading.value = false
    }
  }
  
  const addQuestion = () => {
    quizStore.addQuestion()
  }
  
  const removeQuestion = (index: number) => {
    quizStore.removeQuestion(index)
  }
  
  const handleCancel = () => {
    router.push(`/quiz/${route.params.id}`)
  }
  
  const handleSubmit = async () => {
    if (!isFormValid.value) {
      toast.add({
        severity: 'warn',
        summary: 'Warning',
        detail: 'Please fill in all required fields',
        life: 3000,
      })
      return
    }
  
    submitting.value = true
    try {
      await updateQuiz(Number(route.params.id), quizForm.value)
      toast.add({
        severity: 'success',
        summary: 'Success',
        detail: 'Quiz updated successfully',
        life: 3000,
      })
      router.push(`/quiz/${route.params.id}`)
    } catch (error: any) {
      toast.add({
        severity: 'error',
        summary: 'Error',
        detail: error.response?.data?.message || 'Failed to update quiz',
        life: 3000,
      })
    } finally {
      submitting.value = false
    }
  }
  
  onMounted(() => {
    loadQuiz()
  })
  </script>
  
  <style scoped>
  .quiz-edit-container {
    min-height: calc(100vh - 200px);
  }
  
  .container {
    max-width: 1200px;
    margin: 0 auto;
  }
  
  .question-card {
    margin-bottom: 1rem;
  }
  </style>