<template>
  <el-radio-group v-model="layout" @change="change">
    <el-radio-button :label="1">1分屏</el-radio-button>
    <el-radio-button :label="4">4分屏</el-radio-button>
    <el-radio-button :label="9">9分屏</el-radio-button>
    <el-radio-button :label="16">16分屏</el-radio-button>
  </el-radio-group>
</template>

<script setup lang="ts">
import { ref } from 'vue'
const layout=ref(4)
const emit=defineEmits(['change'])
function change(){emit('change',layout.value)}
</script>
