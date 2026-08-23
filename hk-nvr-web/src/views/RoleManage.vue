<template>
  <PermissionTree :data="permissions" />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PermissionTree from '@/components/PermissionTree.vue'
const permissions = ref([])
</script>
