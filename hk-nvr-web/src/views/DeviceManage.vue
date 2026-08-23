<template>
  <el-card>
    <el-button type="primary">新增NVR设备</el-button>
    <DeviceTable :devices="devices" />
  </el-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import DeviceTable from '@/components/DeviceTable.vue'
const devices = ref([])
</script>
