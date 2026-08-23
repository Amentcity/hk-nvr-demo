<template>
  <el-container class="monitor">
    <el-aside width="260px">
      <CameraTree :cameras="cameras" @select="openCamera" />
    </el-aside>

    <el-main>
      <LayoutSelector @change="changeLayout" />
      <VideoWall :streams="streams" :layout="layout" />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CameraTree from '@/components/CameraTree.vue'
import VideoWall from '@/components/VideoWall.vue'
import LayoutSelector from '@/components/LayoutSelector.vue'
import { startStream } from '@/api/stream'

const layout = ref(4)
const cameras = ref([
  {id:1,name:'大厅摄像头'},
  {id:2,name:'停车场摄像头'}
])

const streams = ref<any[]>([])

async function openCamera(camera:any){
  const res = await startStream(camera.id)
  streams.value.push({...camera,...res.data})
}

function changeLayout(value:number){
  layout.value=value
}
</script>

<style scoped>
.monitor{height:100%;}
</style>
