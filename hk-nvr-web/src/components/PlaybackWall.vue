<template>
  <div class="wall">
    <PlaybackPlayer v-for="item in streams" :key="item.taskId" :url="item.hlsUrl" />
  </div>
</template>

<script setup lang="ts">
import PlaybackPlayer from './PlaybackPlayer.vue'
defineProps<{streams:any[]}>()
</script>

<style scoped>
.wall{display:grid;grid-template-columns:repeat(2,1fr);height:100%;gap:5px;background:#000}
</style>
