<template>
  <Card class="h-full hover:shadow-lg transition-duration-200 cursor-pointer" :class="{ 'opacity-75 grayscale-0': isExpired }" @click="handleClick">
    <template #header>
      <div v-if="challenge.challengeType === 'TIME_ATTACK'" class="challenge-badge badge-time-attack">TIME ATTACK</div>
      <div v-else-if="challenge.challengeType === 'SURVIVAL'" class="challenge-badge badge-survival">SURVIVAL</div>
      <div v-else class="challenge-badge badge-event">EVENT</div>
    </template>

    <template #title>
      <div class="text-xl font-bold mb-2">{{ challenge.title }}</div>
    </template>

    <template #content>
      <p class="text-color-secondary line-clamp-2 h-3rem">
        {{ challenge.description }}
      </p>

      <div class="mt-4 flex gap-2" v-if="challenge.challengeType === 'TIME_ATTACK'">
        <div class="challenge-badge badge-time-attack border-round-sm flex align-items-center gap-2">
          <i class="pi pi-clock" style="font-size: 1rem; line-height: 1"></i>
          <strong style="line-height: 1; font-size: 0.9rem">{{ formatTime(challenge.timeLimit) }}</strong>
        </div>
      </div>
    </template>

    <template #footer>
      <Button
        :label="isExpired ? '이미 종료됨 (결과 보기)' : '도전하기'"
        :class="['w-full', isExpired ? 'opacity-80' : '']"
        :icon="isExpired ? 'pi pi-chart-bar' : 'pi pi-bolt'"
        :severity="isExpired ? 'secondary' : 'primary'"
      />
    </template>
  </Card>
</template>

<script setup>
import { computed } from "vue";
import Card from "primevue/card";
import Button from "primevue/button";
import Tag from "primevue/tag";

const props = defineProps({
  challenge: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(["click"]);

const isExpired = computed(() => {
  if (!props.challenge.endAt) return false;
  const end = new Date(props.challenge.endAt);
  const now = new Date();
  return now > end;
});

const handleClick = () => {
  // if (isExpired.value) return; // Allow click even if expired
  emit("click");
};

const formatTime = (seconds) => {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m}분 ${s}초`;
};
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.challenge-badge {
  padding: 0.25rem 0.75rem;
  font-weight: 700;
  text-align: center;
  border-radius: 0; /* Header shape */
}

/* Time Attack: Red */
.badge-time-attack {
  background-color: #fee2e2;
  color: #dc2626;
}

/* Survival: Purple */
.badge-survival {
  background-color: #f3e8ff;
  color: #9333ea;
}

/* Event: Gray */
.badge-event {
  background-color: #f3f4f6;
  color: #4b5563;
}

/* Dark Mode Overrides */
:global([data-theme="dark"]) .badge-time-attack {
  background-color: rgba(220, 38, 38, 0.2);
  color: #fca5a5 !important;
}
:global([data-theme="dark"]) .badge-time-attack strong,
:global([data-theme="dark"]) .badge-time-attack i {
  color: #fca5a5 !important;
}

:global([data-theme="dark"]) .badge-survival {
  background-color: rgba(147, 51, 234, 0.2);
  color: #d8b4fe !important;
}
:global([data-theme="dark"]) .badge-survival strong,
:global([data-theme="dark"]) .badge-survival i {
  color: #d8b4fe !important;
}

:global([data-theme="dark"]) .badge-event {
  background-color: rgba(75, 85, 99, 0.4);
  color: #d1d5db !important;
}
:global([data-theme="dark"]) .badge-event strong,
:global([data-theme="dark"]) .badge-event i {
  color: #d1d5db !important;
}
</style>
