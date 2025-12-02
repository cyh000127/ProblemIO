<template>
  <div class="comment-section">

    <!-- 댓글 입력 -->
    <CommentInput
      :quiz-id="quizId"
      @submitted="loadComments"
    />

    <hr class="my-4" />

    <!-- 댓글 리스트 -->
    <CommentList
      :comments="comments"
      @updated="loadComments"
    />

  </div>
</template>

<script setup>
import { ref, watch } from "vue";
import CommentInput from "./CommentInput.vue";
import CommentList from "./CommentList.vue";
import { getComments } from "@/api/comment";

const props = defineProps({
  quizId: { type: Number, required: true },
});

const comments = ref([]);

async function loadComments() {
  try {
    const result = await getComments(props.quizId, 1, 20);
    console.log("👉 댓글 API 응답:", result);

    const list = Array.isArray(result?.comments)
      ? result.comments
      : Array.isArray(result?.data?.comments)
      ? result.data.comments
      : Array.isArray(result?.content)
      ? result.content
      : Array.isArray(result)
      ? result
      : [];

    comments.value = list;
  } catch (err) {
    console.error("댓글 불러오기 실패", err);
    comments.value = [];
  }
}

watch(
  () => props.quizId,
  () => loadComments()
);

// 최초 로딩
loadComments();
</script>

<style scoped>
.comment-section {
  padding: 10px 0;
}
</style>
