<template>
  <LogTable :logs="logs" />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import LogTable from '@/components/LogTable.vue'
const logs = ref([])
</script>
