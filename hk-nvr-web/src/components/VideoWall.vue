<template>
  <div class="wall">
    <WebRTCPlayer v-for="item in streams" :key="item.id" :url="item.url" />
  </div>
</template>

<script setup lang="ts">
import WebRTCPlayer from './WebRTCPlayer.vue'
defineProps<{streams:any[]}>()
</script>

<style scoped>
.wall{display:grid;grid-template-columns:repeat(2,1fr);height:100%;gap:4px;background:#000}
</style>
