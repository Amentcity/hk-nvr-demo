<template>
  <div class="video-wall" :style="grid">
    <div v-for="stream in streams" :key="stream.cameraId" class="video-item">
      <WebRTCPlayer :url="stream.webrtcUrl || stream.url" />
      <span>{{stream.name}}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import WebRTCPlayer from './WebRTCPlayer.vue'
const props=defineProps<{streams:any[],layout:number}>()
const grid=computed(()=>({gridTemplateColumns:`repeat(${Math.sqrt(props.layout || 4)},1fr)`}))
</script>

<style scoped>
.video-wall{display:grid;height:100%;gap:5px;background:#000}.video-item{position:relative}
</style>
