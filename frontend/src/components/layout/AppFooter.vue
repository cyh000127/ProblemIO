<template>
  <footer class="border-top-1 surface-border mt-8 py-6">
    <div class="container mx-auto px-4">
      <div class="flex flex-column md:flex-row justify-content-start align-items-center gap-4 footer-inner">
        <div class="text-left">
          <p class="text-color-secondary m-0">&copy; 2025 Problem.io. All rights reserved.</p>
        </div>
        <div class="flex gap-4 footer-links">
          <a href="#" class="text-color-secondary no-underline hover:text-primary">About</a>
          <a href="#" class="text-color-secondary no-underline hover:text-primary">Privacy</a>
          <a href="#" class="text-color-secondary no-underline hover:text-primary">Terms</a>
          <a href="#" class="text-color-secondary no-underline hover:text-primary">Contact</a>
        </div>
      </div>
    </div>
  </footer>
  </template>
  
  <script setup lang="ts">
  </script>
  
  <style scoped>
  .container {
    max-width: 1200px;
    margin: 0 auto;
  }

.footer-inner {
  justify-content: flex-start;
}

.footer-links {
  flex-wrap: wrap;
}
  </style>
