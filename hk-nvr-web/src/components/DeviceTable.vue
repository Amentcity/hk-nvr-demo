<template>
<el-table :data="devices">
<el-table-column prop="name" label="设备名称"/>
<el-table-column prop="ip" label="IP"/>
<el-table-column prop="status" label="状态"/>
</el-table>
</template>
<script setup lang="ts">
defineProps<{devices:any[]}>()
</script>
