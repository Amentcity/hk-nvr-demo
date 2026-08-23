<template>
  <el-container class="layout">
    <el-header>Enterprise NVR</el-header>
    <el-container>
      <el-aside width="220px">
        <el-menu router>
          <el-menu-item index="/monitor">实时监控</el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout{height:100vh}
.el-header{background:#001529;color:white;display:flex;align-items:center;font-size:22px}
</style>
