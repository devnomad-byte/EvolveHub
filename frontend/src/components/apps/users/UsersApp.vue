<template>
  <div class="users-app">
    <div class="users-content">
      <!-- Toolbar -->
      <div class="users-toolbar">
        <button class="btn btn-primary btn-sm">+ 创建用户</button>
        <input v-model="searchUser" placeholder="搜索用户" class="search-input" />
      </div>

      <!-- Users table -->
      <div class="users-table-wrapper">
        <table class="users-table">
          <thead>
            <tr>
              <th>头像</th>
              <th>用户名</th>
              <th>角色</th>
              <th>部门</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in filteredUsers" :key="u.id">
              <td><div class="user-avatar">{{ u.displayName.charAt(0) }}</div></td>
              <td>{{ u.username }}</td>
              <td><span class="role-badge" :class="roleColor(u.role)">{{ roleLabel(u.role) }}</span></td>
              <td>{{ u.deptName }}</td>
              <td><span class="status-dot" :class="{ on: u.status }"></span></td>
              <td><button class="btn btn-outline btn-sm">编辑</button></td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Department tree -->
      <div class="dept-section">
        <div class="dept-title">部门管理</div>
        <div class="dept-tree">
          <div class="dept-node" v-for="dept in departments" :key="dept.name" :style="{ paddingLeft: dept.level * 24 + 'px' }">
            <span class="dept-arrow">{{ dept.children ? '▼' : '' }}</span>
            <span>📂 {{ dept.name }}</span>
          </div>
          <button class="btn-add-dept">+ 添加部门</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { UserInfo } from '../../../types'

const searchUser = ref('')

const users = ref<UserInfo[]>([
  { id: 1, username: 'admin', displayName: '张', role: 'SUPER_ADMIN', deptName: '总经办', status: true },
  { id: 2, username: 'lisi', displayName: '李', role: 'DEPT_HEAD', deptName: '研发部', status: true },
  { id: 3, username: 'wangwu', displayName: '王', role: 'USER', deptName: '市场部', status: false }
])

const departments = ref([
  { name: '总经办', level: 0, children: true },
  { name: '研发部', level: 1, children: true },
  { name: '5G 项目组', level: 2, children: false },
  { name: 'DNA 项目组', level: 2, children: false },
  { name: '市场部', level: 1, children: false },
  { name: '人事部', level: 1, children: false }
])

const filteredUsers = computed(() =>
  users.value.filter(u => u.username.includes(searchUser.value) || u.displayName.includes(searchUser.value))
)

function roleLabel(role: string) {
  const map: Record<string, string> = { SUPER_ADMIN: '超管', LEADER: '高层', DEPT_HEAD: '部门负责', ADMIN: '管理员', USER: '普通员工' }
  return map[role] || role
}

function roleColor(role: string) {
  const map: Record<string, string> = { SUPER_ADMIN: 'red', LEADER: 'orange', DEPT_HEAD: 'purple', ADMIN: 'blue', USER: 'green' }
  return map[role] || ''
}
</script>

<style scoped>
.users-app {
  height: 100%;
  overflow-y: auto;
}

.users-content {
  padding: 16px;
}

.users-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.search-input {
  width: 200px;
  height: 32px;
  font-size: 13px;
  margin-left: auto;
}

.users-table-wrapper {
  border-radius: 10px;
  overflow: hidden;
  border: 1px solid var(--border-subtle);
  margin-bottom: 20px;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.users-table th {
  text-align: left;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.04);
  color: var(--text-secondary);
  font-weight: 500;
  font-size: 12px;
}

.users-table td {
  padding: 10px 12px;
  border-top: 1px solid var(--border-subtle);
}

.users-table tr:hover td {
  background: rgba(255, 255, 255, 0.04);
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0A84FF, #5E5CE6);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
}

.role-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
}

.role-badge.red { background: rgba(255, 69, 58, 0.2); color: #FF453A; }
.role-badge.orange { background: rgba(255, 159, 10, 0.2); color: #FF9F0A; }
.role-badge.purple { background: rgba(191, 90, 242, 0.2); color: #BF5AF2; }
.role-badge.blue { background: rgba(10, 132, 255, 0.2); color: #0A84FF; }
.role-badge.green { background: rgba(48, 209, 88, 0.2); color: #30D158; }

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #FF453A;
}

.status-dot.on {
  background: #30D158;
}

.dept-section {
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  padding: 16px;
  border: 1px solid var(--border-subtle);
}

.dept-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
}

.dept-tree {
  font-size: 13px;
}

.dept-node {
  padding: 4px 8px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.dept-arrow {
  font-size: 10px;
  width: 12px;
  color: var(--text-disabled);
}

.btn-add-dept {
  background: none;
  border: none;
  color: var(--color-primary);
  font-size: 13px;
  cursor: pointer;
  margin-top: 8px;
  padding-left: 8px;
}

.btn-sm {
  padding: 4px 10px;
  font-size: 12px;
}
</style>
