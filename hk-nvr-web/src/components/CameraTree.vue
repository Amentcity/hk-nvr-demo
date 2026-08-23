<template>
  <el-card class="camera-tree">
    <el-input v-model="keyword" placeholder="搜索摄像头" />
    <el-tree :data="filtered" node-key="id" @node-click="selectCamera" />
  </el-card>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

const props = defineProps<{ cameras:any[] }>()
const emit = defineEmits(['select'])
const keyword = ref('')

const filtered = computed(()=>
  keyword.value
    ? props.cameras.filter(item=>item.name.includes(keyword.value))
    : props.cameras
)

function selectCamera(camera:any){
  emit('select', camera)
}
</script>
