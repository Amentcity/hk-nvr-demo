<template>
  <div class="timeline">
    <div v-for="item in records" :key="item.id" class="segment" @click="select(item)">
      {{ item.startTime }} - {{ item.endTime }}
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{records:any[]}>()
const emit = defineEmits(['select'])
function select(item:any){ emit('select', item) }
</script>

<style scoped>
.timeline{display:flex;gap:8px;background:#222;padding:10px}.segment{background:#087f5b;color:#fff;padding:8px;cursor:pointer}
</style>
