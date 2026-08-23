<template>
  <el-table :data="users">
    <el-table-column prop="username" label="用户名" />
    <el-table-column prop="role" label="角色" />
  </el-table>
</template>

<script setup lang="ts">
import { ref } from 'vue'
const users = ref([])
</script>
