<template>
  <video ref="video" controls autoplay class="player"></video>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import Hls from 'hls.js'

const props = defineProps<{url:string}>()
const video = ref<HTMLVideoElement>()
let hls:Hls|undefined

watch(()=>props.url,(url)=>{
 if(!url) return
 if(Hls.isSupported()){
  hls=new Hls()
  hls.loadSource(url)
  hls.attachMedia(video.value!)
 }
},{immediate:true})
</script>

<style scoped>
.player{width:100%;height:100%;background:#000}
</style>
