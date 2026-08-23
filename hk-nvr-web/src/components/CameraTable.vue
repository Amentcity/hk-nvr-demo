<template>
<el-table :data="cameras">
<el-table-column prop="name" label="摄像头"/>
<el-table-column prop="channel" label="通道"/>
<el-table-column prop="streamUrl" label="流地址"/>
</el-table>
</template>
<script setup lang="ts">
defineProps<{cameras:any[]}>()
</script>
