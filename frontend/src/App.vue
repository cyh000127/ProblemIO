<template>
  <div class="app-container">
    <NavigationBar />
    <main class="main-content">
      <router-view />
    </main>
    <AppFooter />
    <Toast />
    <ConfirmDialog />
  </div>
</template>

<script setup lang="ts">
import NavigationBar from '@/components/layout/NavigationBar.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
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
</style>