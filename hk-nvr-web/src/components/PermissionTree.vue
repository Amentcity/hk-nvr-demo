<template>
<el-tree :data="data" show-checkbox node-key="id" />
</template>
<script setup lang="ts">
defineProps<{data:any[]}>()
</script>
