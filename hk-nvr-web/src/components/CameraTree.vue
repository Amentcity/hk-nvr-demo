<template>
  <el-tree :data="cameras" node-key="id" @node-click="select" />
</template>

<script setup lang="ts">
defineProps<{cameras:any[]}>()
const emit=defineEmits(['open'])
function select(camera:any){emit('open',camera)}
</script>
