<template>
  <div class="app-container">
    <NavigationBar />
    <main class="main-content">
      <router-view />
    </main>
    <AppFooter />
    <Toast />
    <ConfirmDialog />
    <button
      class="theme-toggle"
      :aria-label="`Switch to ${theme === 'light' ? 'dark' : 'light'} mode`"
      @click="toggleTheme"
    >
      <i :class="theme === 'light' ? 'pi pi-moon' : 'pi pi-sun'"></i>
    </button>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import NavigationBar from '@/components/layout/NavigationBar.vue'
import AppFooter from '@/components/layout/AppFooter.vue'

const theme = ref(localStorage.getItem("theme") || "light");

const applyTheme = (mode: string) => {
  const value = mode === "dark" ? "dark" : "light";
  theme.value = value;
  document.documentElement.setAttribute("data-theme", value);
  localStorage.setItem("theme", value);
};

const toggleTheme = () => {
  applyTheme(theme.value === "light" ? "dark" : "light");
};

onMounted(() => {
  applyTheme(theme.value);
});
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding: 2rem 0;
}

.theme-toggle {
  position: fixed;
  right: 20px;
  bottom: 20px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 1px solid var(--color-border);
  background: var(--color-background-mute);
  color: var(--color-heading);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  z-index: 1000;
}

.theme-toggle:hover {
  background: var(--color-primary);
  color: #ffffff;
  border-color: var(--color-primary);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.25);
}

.theme-toggle:active {
  transform: translateY(1px);
}
</style>
