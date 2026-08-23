<template>
  <el-radio-group v-model="layout" @change="changeLayout">
    <el-radio-button :label="1">1画面</el-radio-button>
    <el-radio-button :label="4">4画面</el-radio-button>
    <el-radio-button :label="9">9画面</el-radio-button>
    <el-radio-button :label="16">16画面</el-radio-button>
  </el-radio-group>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const layout = ref(4)
const emit = defineEmits(['change'])

function changeLayout(){
  emit('change', layout.value)
}
</script>
