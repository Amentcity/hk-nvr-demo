<template>
<el-table :data="logs">
<el-table-column prop="username" label="用户"/>
<el-table-column prop="operation" label="操作"/>
<el-table-column prop="createTime" label="时间"/>
</el-table>
</template>
<script setup lang="ts">
defineProps<{logs:any[]}>()
</script>
