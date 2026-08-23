<template>
  <CameraTable :cameras="cameras" />
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CameraTable from '@/components/CameraTable.vue'
const cameras = ref([])
</script>
