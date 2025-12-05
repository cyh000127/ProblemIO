<template>
  <div :class="['comment-input', { compact: !!parentCommentId }]">
    <div v-if="!parentCommentId" class="notice">
      댓글 작성 시 IP가 기록되며 사이트 이용 제한이나 요청에 따라 법적 조치가 취해질 수 있습니다.
    </div>

    <!-- 비로그인 게스트 정보 -->
    <div v-if="!isAuthenticated" class="guest-row">
      <InputText
        v-model="nickname"
        class="flex-1"
        placeholder="닉네임"
        :disabled="submitting"
      />
      <InputText
        v-model="password"
        type="password"
        class="guest-password"
        placeholder="비밀번호"
        :disabled="submitting"
      />
    </div>

    <div class="input-row">
      <textarea
        v-model="content"
        class="comment-textarea"
        rows="3"
        :placeholder="placeholderText"
        :disabled="submitting"
        @keyup.enter.exact.prevent="submit"
      />
      <Button
        :label="buttonLabel"
        class="submit-btn"
        :loading="submitting"
        :disabled="!canSubmit"
        @click="submit"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import { useAuthStore } from "@/stores/auth";
import { createComment } from "@/api/comment";

const props = defineProps({
  quizId: Number,
  parentCommentId: { type: Number, default: null },
  placeholder: { type: String, default: "" },
  buttonLabel: { type: String, default: "" },
});

const emit = defineEmits(["submitted"]);

const authStore = useAuthStore();
const isAuthenticated = authStore.isAuthenticated;

const content = ref("");
const nickname = ref("");
const password = ref("");
const submitting = ref(false);

const placeholderText = computed(() =>
  props.placeholder || (props.parentCommentId ? "답글을 입력하세요..." : "댓글을 입력하세요...")
);
const buttonLabel = computed(() =>
  props.buttonLabel || (props.parentCommentId ? "답글 작성" : "작성하기")
);

const canSubmit = computed(() => {
  if (!content.value.trim()) return false;
  if (authStore.isAuthenticated) return true;
  return nickname.value.trim() && password.value.trim();
});

async function submit() {
  if (!canSubmit.value || submitting.value) return;

  const payload = {
    content: content.value,
    parentCommentId: props.parentCommentId ?? null,
  };

  if (!authStore.isAuthenticated) {
    payload.nickname = nickname.value.trim();
    payload.password = password.value.trim();
  }

  submitting.value = true;
  try {
    await createComment(props.quizId, payload);

    content.value = "";
    if (!authStore.isAuthenticated) {
      password.value = "";
    }

    emit("submitted");
  } catch (err) {
    console.error("댓글 작성 실패", err);
  } finally {
    submitting.value = false;
  }
}
</script>

<style scoped>
.comment-input {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.comment-input.compact .notice {
  display: none;
}

.comment-input.compact .input-row {
  padding-left: 8px;
}

.notice {
  background: #f3f4f6;
  color: #6b7280;
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 13px;
}

.guest-row {
  display: flex;
  gap: 8px;
}

.guest-password {
  width: 160px;
}

.input-row {
  display: flex;
  gap: 10px;
  align-items: stretch;
}

.comment-textarea {
  flex: 1;
  border: 1px solid #d1d5db;
  border-radius: 10px;
  padding: 10px 12px;
  font-size: 14px;
  resize: vertical;
  min-height: 88px;
  box-sizing: border-box;
}

.submit-btn {
  align-self: flex-end;
  width: 110px;
}
</style>
