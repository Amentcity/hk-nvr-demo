<template>
  <el-row :gutter="20">
    <el-col :span="6"><StatisticCard title="NVR设备" value="12"/></el-col>
    <el-col :span="6"><StatisticCard title="摄像头" value="256"/></el-col>
    <el-col :span="6"><StatisticCard title="在线流" value="128"/></el-col>
    <el-col :span="6"><StatisticCard title="告警" value="32"/></el-col>
  </el-row>
</template>

<script setup lang="ts">
import StatisticCard from '@/components/StatisticCard.vue'
</script>
