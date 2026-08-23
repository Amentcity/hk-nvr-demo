<template>
  <el-card>
    <div>{{ title }}</div>
    <h2>{{ value }}</h2>
  </el-card>
</template>

<script setup lang="ts">
defineProps<{title:string,value:string|number}>()
</script>
