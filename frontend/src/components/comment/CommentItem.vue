<template>
  <div class="comment-item">
    <div class="meta">
      <div class="author">{{ comment.nickname || "익명" }}</div>
      <div class="date">{{ formatDate(comment.createdAt) }}</div>
    </div>

    <!-- 내용 -->
    <p v-if="!isEditing" class="content">{{ comment.content }}</p>

    <!-- 인라인 편집 영역 -->
    <div v-else class="edit-area">
      <textarea
        v-model="editContent"
        class="edit-textarea"
        rows="3"
        placeholder="내용을 수정하세요"
        :disabled="saving"
      />
      <div class="edit-row">
        <input
          v-if="isGuest"
          v-model="password"
          type="password"
          class="guest-password"
          placeholder="비밀번호 (게스트 전용)"
          :disabled="saving"
        />
        <span v-if="error" class="error-text">{{ error }}</span>
      </div>
      <div class="edit-actions">
        <Button
          label="취소"
          severity="secondary"
          size="small"
          :disabled="saving"
          @click="cancelEdit"
        />
        <Button
          label="저장"
          size="small"
          :loading="saving"
          @click="saveEdit"
        />
      </div>
    </div>

    <!-- 하단 버튼 -->
    <div class="actions">
      <!-- 좋아요 -->
      <button @click="toggleLike" class="action-btn">
        <span :class="likedByMe ? 'text-red-500' : 'text-gray-400'">♥</span>
        <span>{{ comment.likeCount }}</span>
      </button>

      <!-- 수정 -->
      <button class="action-btn text-blue-600" @click="startEdit">
        수정
      </button>

      <!-- 삭제 -->
      <button class="action-btn text-red-600" @click="remove">
        삭제
      </button>
    </div>
  </div>

  <!-- 게스트 비밀번호 확인 모달 -->
  <Dialog
    v-model:visible="showPasswordModal"
    header="댓글 삭제"
    modal
    :closable="!deleting"
    :dismissableMask="!deleting"
    :style="{ width: '22rem' }"
  >
    <div class="flex flex-col gap-3">
      <p class="m-0 text-sm text-gray-600">
        게스트로 작성된 댓글입니다. 삭제하려면 비밀번호를 입력하세요.
      </p>
      <InputText
        v-model="password"
        type="password"
        placeholder="비밀번호"
        :disabled="deleting"
      />
      <div v-if="error" class="text-sm text-red-500">{{ error }}</div>
      <div class="flex justify-end gap-2">
        <Button
          label="취소"
          severity="secondary"
          :disabled="deleting"
          @click="closeModal"
        />
        <Button
          label="삭제"
          severity="danger"
          :loading="deleting"
          @click="confirmDelete"
        />
      </div>
    </div>
  </Dialog>
</template>

<script setup>
import { computed, ref } from "vue";
import { toggleCommentLike, deleteComment, updateComment } from "@/api/comment";

const props = defineProps({
  comment: { type: Object, required: true },
});

const emit = defineEmits(["updated"]);

const likedByMe = computed(
  () =>
    props.comment.likedByMe ?? props.comment.liked ?? props.comment.isLiked
);
const isGuest = computed(
  () => !props.comment.userId && (props.comment.isGuest ?? true)
);

const showPasswordModal = ref(false);
const deleting = ref(false);
const password = ref("");
const error = ref("");
const isEditing = ref(false);
const editContent = ref(props.comment.content ?? "");
const saving = ref(false);

function resolveCommentId() {
  return props.comment.id ?? props.comment.commentId;
}

function formatDate(str) {
  return new Date(str).toLocaleString("ko-KR");
}

/** 좋아요 토글 */
async function toggleLike() {
  const commentId = resolveCommentId();
  if (!commentId) return;

  await toggleCommentLike(commentId);
  emit("updated");
}

/** 삭제 */
async function remove() {
  const commentId = resolveCommentId();
  if (!commentId) return;

  // 게스트면 모달로 비밀번호 입력
  if (isGuest.value) {
    showPasswordModal.value = true;
    return;
  }

  await deleteComment(commentId);
  emit("updated");
}

async function confirmDelete() {
  if (!password.value.trim()) {
    error.value = "비밀번호를 입력하세요.";
    return;
  }

  const commentId = resolveCommentId();
  if (!commentId) return;

  deleting.value = true;
  error.value = "";
  try {
    await deleteComment(commentId, password.value.trim());
    emit("updated");
    closeModal();
  } catch (err) {
    error.value = "삭제에 실패했습니다. 비밀번호를 확인하세요.";
  } finally {
    deleting.value = false;
  }
}

function closeModal() {
  if (deleting.value) return;
  showPasswordModal.value = false;
  password.value = "";
  error.value = "";
}

function startEdit() {
  isEditing.value = true;
  editContent.value = props.comment.content ?? "";
  error.value = "";
  password.value = "";
}

function cancelEdit() {
  if (saving.value) return;
  isEditing.value = false;
  editContent.value = props.comment.content ?? "";
  error.value = "";
  password.value = "";
}

async function saveEdit() {
  if (!editContent.value.trim()) {
    error.value = "내용을 입력하세요.";
    return;
  }

  const commentId = resolveCommentId();
  if (!commentId) return;

  if (isGuest.value && !password.value.trim()) {
    error.value = "비밀번호를 입력하세요.";
    return;
  }

  saving.value = true;
  error.value = "";
  try {
    await updateComment(commentId, {
      content: editContent.value.trim(),
      ...(isGuest.value ? { password: password.value.trim() } : {}),
    });
    emit("updated");
    isEditing.value = false;
  } catch (err) {
    error.value = "수정에 실패했습니다. 비밀번호를 확인하세요.";
  } finally {
    saving.value = false;
  }
}
</script>

<style scoped>
.comment-item {
  border: 1px solid var(--color-border);
  border-radius: 10px;
  padding: 12px;
  background: var(--color-background-soft);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
}

.author {
  font-weight: 700;
  color: var(--color-heading);
}

.date {
  color: var(--color-text);
}

.content {
  margin: 0;
  white-space: pre-line;
  color: var(--color-heading);
}

.actions {
  display: flex;
  gap: 12px;
  align-items: center;
  font-size: 13px;
}

.action-btn {
  display: inline-flex;
  gap: 4px;
  align-items: center;
  color: var(--color-text);
}

.edit-area {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.edit-textarea {
  width: 100%;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 10px;
  font-size: 14px;
  resize: vertical;
  background: var(--color-background-soft);
  color: var(--color-heading);
}

.edit-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.guest-password {
  flex: 1;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  padding: 8px 10px;
  font-size: 13px;
  background: var(--color-background-soft);
  color: var(--color-heading);
}

.error-text {
  color: #ef4444;
  font-size: 12px;
}

.edit-actions {
  display: flex;
  gap: 8px;
}
</style>
