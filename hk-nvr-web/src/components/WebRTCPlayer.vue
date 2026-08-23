<template>
  <video ref="video" autoplay muted playsinline class="player"></video>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue'

defineProps<{ url:string }>()
const video = ref<HTMLVideoElement>()
let pc: RTCPeerConnection | undefined

onUnmounted(()=>pc?.close())
</script>

<style scoped>
.player{width:100%;height:100%;background:#000}
</style>
