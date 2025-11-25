<template>
  <div class="quiz-create-container">
    <div class="container mx-auto px-4 max-w-4xl">
      <Card>
        <template #header>
          <div class="p-4 border-bottom-1 surface-border">
            <h1 class="text-3xl font-bold m-0">Create New Quiz</h1>
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
                <label class="text-sm font-medium">Thumbnail Image</label>
                <small class="text-color-secondary">Uploading an image file will auto-fill the URL. You can still paste a URL if you already have one.</small>
                <div class="flex align-items-center gap-3 flex-wrap">
                  <FileUpload
                    mode="basic"
                    name="file"
                    accept="image/*"
                    :auto="false"
                    :maxFileSize="5000000"
                    chooseLabel="Upload"
                    chooseIcon="pi pi-upload"
                    :disabled="thumbnailUploading"
                    @select="handleThumbnailFile"
                  />
                  <Button
                    v-if="quizForm.thumbnailUrl"
                    label="Remove"
                    severity="secondary"
                    text
                    icon="pi pi-times"
                    @click="clearThumbnail"
                  />
                </div>
                <InputText
                  v-model="quizForm.thumbnailUrl"
                  placeholder="https://example.com/image.jpg"
                  class="w-full"
                />
                <div v-if="thumbnailPreview" class="image-preview">
                  <img :src="thumbnailPreview" alt="Thumbnail preview" />
                </div>
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

              <div v-if="quizForm.questions.length === 0" class="text-center py-8 text-color-secondary">
                <p>No questions yet. Click "Add Question" to get started.</p>
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
                      <label class="text-sm font-medium">Image</label>
                      <small class="text-color-secondary">Uploading a file will auto-fill the URL field below.</small>
                      <div class="flex align-items-center gap-3 flex-wrap">
                        <FileUpload
                          mode="basic"
                          name="file"
                          accept="image/*"
                          :auto="false"
                          :maxFileSize="5000000"
                          chooseLabel="Upload"
                          chooseIcon="pi pi-upload"
                          :disabled="questionUploadingIndex === index"
                          @select="(event) => handleQuestionImageFile(index, event)"
                        />
                        <Button
                          v-if="question.imageUrl"
                          label="Remove"
                          severity="secondary"
                          text
                          icon="pi pi-times"
                          @click="clearQuestionImage(index)"
                        />
                      </div>
                      <InputText
                        v-model="question.imageUrl"
                        placeholder="https://example.com/question-image.jpg"
                        class="w-full"
                      />
                      <div v-if="getImagePreview(question.imageUrl)" class="image-preview">
                        <img :src="getImagePreview(question.imageUrl)" alt="Question preview" />
                      </div>
                    </div>

                    <div class="flex flex-column gap-2">
                      <label class="text-sm font-medium">Correct Answers *</label>
                      <small class="text-color-secondary mb-2">
                        Enter multiple answers (synonyms, variations). Press Enter to add each answer.
                      </small>
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
                label="Complete Creation"
                icon="pi pi-check"
                :loading="submitting"
                :disabled="!isFormValid || thumbnailUploading || questionUploadingIndex !== null"
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
import { useRouter } from 'vue-router'
import { useToast } from 'primevue/usetoast'
import { useQuizStore } from '@/stores/quiz'
import { createQuiz } from '@/api/quiz'
import { uploadFile } from '@/api/file'
import { resolveImageUrl } from '@/lib/image'

const router = useRouter()
const toast = useToast()
const quizStore = useQuizStore()

const submitting = ref(false)
const thumbnailUploading = ref(false)
const questionUploadingIndex = ref<number | null>(null)

const quizForm = computed(() => quizStore.quizForm)

const buildImageUrl = (path?: string) => resolveImageUrl(path)

const thumbnailPreview = computed(() => buildImageUrl(quizForm.value.thumbnailUrl))

const isFormValid = computed(() => {
  if (!quizForm.value.title?.trim()) return false
  if (quizForm.value.questions.length === 0) return false
  
  return quizForm.value.questions.every((q) => {
    return q.answers && q.answers.length > 0
  })
})

const addQuestion = () => {
  quizStore.addQuestion()
}

const removeQuestion = (index: number) => {
  quizStore.removeQuestion(index)
}

const handleCancel = () => {
  if (confirm('Are you sure you want to cancel? All changes will be lost.')) {
    quizStore.resetQuizForm()
    router.push('/')
  }
}

const handleSubmit = async () => {
  if (thumbnailUploading.value || questionUploadingIndex.value !== null) {
    toast.add({
      severity: 'warn',
      summary: 'Uploading images',
      detail: 'Please wait until image uploads are finished.',
      life: 2500,
    })
    return
  }

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
    const result = await createQuiz(quizForm.value)
    quizStore.resetQuizForm()
    toast.add({
      severity: 'success',
      summary: 'Success',
      detail: 'Quiz created successfully',
      life: 3000,
    })
    router.push(`/quiz/${result.id}`)
  } catch (error: any) {
    toast.add({
      severity: 'error',
      summary: 'Error',
      detail: error.response?.data?.message || 'Failed to create quiz',
      life: 3000,
    })
  } finally {
    submitting.value = false
  }
}

const handleThumbnailFile = async (event: any) => {
  const file = event.files?.[0]
  if (!file) return

  thumbnailUploading.value = true
  try {
    const { url } = await uploadFile(file, 'thumbnail')
    quizForm.value.thumbnailUrl = url
    toast.add({
      severity: 'success',
      summary: 'Upload success',
      detail: 'Thumbnail image has been set.',
      life: 2000,
    })
  } catch (error: any) {
    toast.add({
      severity: 'error',
      summary: 'Upload failed',
      detail: error.response?.data?.message || 'Failed to upload image.',
      life: 3000,
    })
  } finally {
    thumbnailUploading.value = false
  }
}

const clearThumbnail = () => {
  quizForm.value.thumbnailUrl = ''
}

const handleQuestionImageFile = async (index: number, event: any) => {
  const file = event.files?.[0]
  if (!file) return

  questionUploadingIndex.value = index
  try {
    const { url } = await uploadFile(file, 'question')
    quizForm.value.questions[index].imageUrl = url
    toast.add({
      severity: 'success',
      summary: 'Upload success',
      detail: `Question ${quizForm.value.questions[index].questionOrder} image set.`,
      life: 2000,
    })
  } catch (error: any) {
    toast.add({
      severity: 'error',
      summary: 'Upload failed',
      detail: error.response?.data?.message || 'Failed to upload image.',
      life: 3000,
    })
  } finally {
    questionUploadingIndex.value = null
  }
}

const clearQuestionImage = (index: number) => {
  quizForm.value.questions[index].imageUrl = ''
}

const getImagePreview = (path?: string) => buildImageUrl(path)

onMounted(() => {
  // 초기화 시 첫 번째 질문 추가
  if (quizForm.value.questions.length === 0) {
    addQuestion()
  }
})
</script>

<style scoped>
.quiz-create-container {
  min-height: calc(100vh - 200px);
}

.container {
  max-width: 1200px;
  margin: 0 auto;
}

.question-card {
  margin-bottom: 1rem;
}

.image-preview {
  margin-top: 0.5rem;
}

.image-preview img {
  max-width: 320px;
  border-radius: 8px;
  border: 1px solid var(--surface-border);
  object-fit: cover;
}
</style>
