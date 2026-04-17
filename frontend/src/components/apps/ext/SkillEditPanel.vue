<template>
  <div class="skill-edit-panel">
    <div class="editor-header">
      <div class="editor-title">
        <Sparkles :size="20" />
        <span>{{ isCreate ? '创建技能' : skillName }}</span>
      </div>
      <div class="editor-actions">
        <button class="btn btn-outline btn-sm" @click="loadSkill" v-if="!isCreate">
          <RefreshCw :size="14" /> 刷新
        </button>
        <button class="btn btn-primary btn-sm" @click="saveSkill" :disabled="saving">
          <Save :size="14" /> {{ isCreate ? '创建' : '保存' }}
        </button>
      </div>
    </div>

    <div class="editor-content">
      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <span>加载中...</span>
      </div>
      <div v-else class="form-content">
        <div class="form-section">
          <h3>基本信息</h3>
          <div class="form-group">
            <label>技能名称 *</label>
            <input v-model="form.name" type="text" placeholder="输入技能名称" />
          </div>
          <div class="form-group">
            <label>技能类型</label>
            <select v-model="form.skillType">
              <option value="CODER">💻 CODER - 代码助手</option>
              <option value="WRITER">📝 WRITER - 文档撰写</option>
              <option value="ANALYST">📊 ANALYST - 数据分析</option>
              <option value="TRANSLATOR">🌐 TRANSLATOR - 翻译助手</option>
              <option value="TESTER">🧪 TESTER - 测试工程师</option>
              <option value="ARCHITECT">🏗️ ARCHITECT - 架构师</option>
              <option value="OPS">⚙️ OPS - 运维助手</option>
              <option value="PM">📋 PM - 产品经理</option>
            </select>
          </div>
          <div class="form-group">
            <label>描述</label>
            <textarea v-model="form.description" rows="3" placeholder="输入技能描述"></textarea>
          </div>
        </div>

        <div class="form-section">
          <h3>权限配置</h3>
          <div class="form-group">
            <label>可见范围</label>
            <select v-model="form.scope">
              <option value="SYSTEM">系统级 - 所有用户可用</option>
              <option value="DEPT">部门级 - 指定部门可用</option>
              <option value="USER">个人级 - 指定用户可用</option>
            </select>
          </div>
          <div class="form-group" v-if="form.scope === 'DEPT'">
            <label>指定部门 *</label>
            <select v-model="form.deptId">
              <option :value="null" disabled>请选择部门</option>
              <option v-for="dept in deptList" :key="dept.id" :value="dept.id">
                {{ dept.deptName }}
              </option>
            </select>
          </div>
          <div class="form-group" v-if="form.scope === 'USER'">
            <label>授权用户 * <small>搜索并选择可使用此技能的用户</small></label>
            <input v-model="userSearch" type="text" class="user-search-input" placeholder="搜索用户名或昵称..." />
            <div class="user-pick-list">
              <div
                v-for="user in filteredUsers"
                :key="user.id"
                class="user-pick-item"
                :class="{ picked: selectedUserIds.has(user.id) }"
                @click="toggleUser(user.id)"
              >
                <div class="user-pick-avatar">{{ (user.nickname || user.username).charAt(0) }}</div>
                <div class="user-pick-info">
                  <span class="user-pick-name">{{ user.nickname || user.username }}</span>
                  <span class="user-pick-dept">{{ user.deptName }}</span>
                </div>
                <span v-if="selectedUserIds.has(user.id)" class="user-pick-check">✓</span>
              </div>
            </div>
            <div v-if="selectedUserIds.size > 0" class="user-picked-summary">
              已选 {{ selectedUserIds.size }} 人
            </div>
          </div>
          <div class="form-group">
            <label>启用状态</label>
            <label class="toggle-switch">
              <input type="checkbox" v-model="form.enabled" />
              <span class="toggle-slider"></span>
            </label>
            <span class="toggle-label">{{ form.enabled ? '启用' : '禁用' }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="editor-footer">
      <span class="status-text" :class="{ saved: lastSaved }">
        {{ lastSaved ? '已保存' : '未保存' }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { Sparkles, RefreshCw, Save } from 'lucide-vue-next'
import { adminSkillConfigApi, type SkillScope } from '@/api/adminSkillConfig'
import { adminDeptApi, type DeptInfo } from '@/api/adminDept'
import { adminUserApi, type UserInfo } from '@/api/adminUser'
import { resourceGrantApi } from '@/api/resourceGrant'
import { useDesktopStore } from '@/stores/desktop'
import { useWindowStore } from '@/stores/window'

const desktop = useDesktopStore()
const winStore = useWindowStore()

const props = defineProps<{
  skillId: number | null
  skillName: string
}>()

const loading = ref(false)
const saving = ref(false)
const lastSaved = ref(false)
const deptList = ref<DeptInfo[]>([])
const allUsers = ref<UserInfo[]>([])
const userSearch = ref('')
const selectedUserIds = ref<Set<number>>(new Set())

const isCreate = computed(() => props.skillId === null)

const filteredUsers = computed(() => {
  const kw = userSearch.value.toLowerCase()
  return allUsers.value.filter(u => {
    if (!kw) return true
    return (u.nickname || '').toLowerCase().includes(kw)
      || (u.username || '').toLowerCase().includes(kw)
  })
})

function toggleUser(userId: number) {
  const s = new Set(selectedUserIds.value)
  if (s.has(userId)) s.delete(userId)
  else s.add(userId)
  selectedUserIds.value = s
}

const form = ref({
  name: '',
  skillType: 'CODER',
  description: '',
  scope: 'SYSTEM' as SkillScope,
  deptId: null as number | null,
  enabled: true
})

async function loadSkill() {
  if (isCreate.value) {
    loading.value = false
    return
  }
  loading.value = true
  try {
    const res = await adminSkillConfigApi.get(props.skillId!)
    form.value = {
      name: res.name || '',
      skillType: res.skillType || 'CODER',
      description: res.description || '',
      scope: res.scope || 'SYSTEM',
      deptId: res.deptId || null,
      enabled: res.enabled === 1
    }
  } catch (e) {
    console.error('加载技能失败', e)
    desktop.addToast('加载失败', 'error')
  } finally {
    loading.value = false
  }
}

async function saveSkill() {
  if (!form.value.name) {
    desktop.addToast('请填写必填项', 'error')
    return
  }
  if (form.value.scope === 'DEPT' && !form.value.deptId) {
    desktop.addToast('请选择部门', 'error')
    return
  }
  if (form.value.scope === 'USER' && selectedUserIds.value.size === 0) {
    desktop.addToast('请选择至少一个授权用户', 'error')
    return
  }
  saving.value = true
  try {
    if (isCreate.value) {
      const res = await adminSkillConfigApi.create({
        name: form.value.name,
        skillType: form.value.skillType,
        scope: form.value.scope,
        deptId: form.value.deptId || undefined,
        description: form.value.description,
        enabled: form.value.enabled ? 1 : 0
      })
      // USER scope: 批量授权
      if (form.value.scope === 'USER' && res.id) {
        await Promise.all(
          Array.from(selectedUserIds.value).map(userId =>
            resourceGrantApi.assign(userId, 'SKILL', res.id)
          )
        )
      }
      desktop.addToast('创建成功', 'success')
      window.dispatchEvent(new CustomEvent('skill-list-changed'))
      winStore.closeByAppId('skill-edit')    } else {
      await adminSkillConfigApi.update({
        id: props.skillId!,
        name: form.value.name,
        skillType: form.value.skillType,
        scope: form.value.scope,
        deptId: form.value.deptId || undefined,
        description: form.value.description,
        enabled: form.value.enabled ? 1 : 0
      })

      // USER scope: 同步授权用户
      if (form.value.scope === 'USER') {
        const existingGrants = await resourceGrantApi.listByResource('SKILL', props.skillId!)
        const existingIds = new Set(existingGrants.map(g => g.userId))
        const toAdd = Array.from(selectedUserIds.value).filter(id => !existingIds.has(id))
        const toRemove = existingGrants.filter(g => !selectedUserIds.value.has(g.userId))
        await Promise.all([
          ...toAdd.map(userId => resourceGrantApi.assign(userId, 'SKILL', props.skillId!)),
          ...toRemove.map(g => resourceGrantApi.revoke(g.userId, 'SKILL', props.skillId!))
        ])
      }

      lastSaved.value = true
      setTimeout(() => { lastSaved.value = false }, 2000)
      desktop.addToast('保存成功', 'success')
    }
  } catch (e: any) {
    if (e.code === 4001 && e.data) {
      desktop.addToast('安全扫描未通过，请检查技能内容', 'error')
    } else {
      console.error('保存技能失败', e)
      desktop.addToast('保存失败', 'error')
    }
  } finally {
    saving.value = false
  }
}

async function loadDeptList() {
  try {
    const res = await adminDeptApi.list(1, 100)
    deptList.value = res.records
  } catch (e) {
    console.error('加载部门列表失败', e)
  }
}

async function loadUsers() {
  try {
    allUsers.value = await adminUserApi.list()
  } catch (e) {
    console.error('加载用户列表失败', e)
  }
}

async function loadGrantedUsers() {
  if (isCreate.value) return
  try {
    const grants = await resourceGrantApi.listByResource('SKILL', props.skillId!)
    if (grants.length > 0) {
      selectedUserIds.value = new Set(grants.map(g => g.userId))
    }
  } catch (e) {
    console.error('加载授权用户失败', e)
  }
}

onMounted(() => {
  loadSkill()
  loadDeptList()
  loadUsers()
  loadGrantedUsers()
})
</script>

<style scoped>
.skill-edit-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #1C1C1E;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: rgba(0, 0, 0, 0.3);
  border-bottom: 1px solid var(--border-subtle);
}

.editor-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.editor-title svg {
  color: #30D158;
}

.editor-actions {
  display: flex;
  gap: 8px;
}

.editor-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 12px;
  color: var(--text-disabled);
}

.spinner {
  width: 24px;
  height: 24px;
  border: 2px solid var(--border-subtle);
  border-top-color: #30D158;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.form-content {
  max-width: 500px;
}

.form-section {
  margin-bottom: 24px;
}

.form-section h3 {
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-disabled);
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--border-subtle);
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.form-group input[type="text"],
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px 14px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 14px;
  box-sizing: border-box;
}

.form-group input[type="text"]:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #0A84FF;
}

.form-group textarea {
  resize: vertical;
  min-height: 70px;
}

/* Toggle */
.toggle-switch {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 24px;
  vertical-align: middle;
  margin-right: 10px;
}

.toggle-switch input {
  opacity: 0;
  width: 0;
  height: 0;
}

.toggle-slider {
  position: absolute;
  inset: 0;
  background: #3A3A3C;
  border-radius: 24px;
  cursor: pointer;
  transition: 0.2s;
}

.toggle-slider::before {
  content: '';
  position: absolute;
  height: 18px;
  width: 18px;
  left: 3px;
  bottom: 3px;
  background: white;
  border-radius: 50%;
  transition: 0.2s;
}

.toggle-switch input:checked + .toggle-slider {
  background: #30D158;
}

.toggle-switch input:checked + .toggle-slider::before {
  transform: translateX(20px);
}

.toggle-label {
  font-size: 13px;
  color: var(--text-secondary);
  vertical-align: middle;
}

.editor-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 12px 20px;
  background: rgba(0, 0, 0, 0.2);
  border-top: 1px solid var(--border-subtle);
  font-size: 12px;
  color: var(--text-disabled);
}

.editor-footer .saved {
  color: #30D158;
}

/* Buttons */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
  border: none;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 11px;
}

.btn-primary {
  background: #0A84FF;
  color: white;
}

.btn-primary:hover {
  background: #0070E0;
}

.btn-primary:disabled {
  background: rgba(10, 132, 255, 0.3);
  cursor: not-allowed;
}

.btn-outline {
  background: rgba(255, 255, 255, 0.08);
  color: var(--text-primary);
  border: 1px solid var(--border-subtle);
}

.btn-outline:hover {
  background: rgba(255, 255, 255, 0.12);
}

/* User Picker */
.user-search-input {
  width: 100%;
  padding: 10px 14px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 14px;
  box-sizing: border-box;
  margin-bottom: 8px;
}
.user-search-input:focus { outline: none; border-color: #0A84FF; }

.user-pick-list {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.15);
}
.user-pick-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  cursor: pointer;
  transition: background 0.15s;
}
.user-pick-item:hover { background: rgba(255, 255, 255, 0.05); }
.user-pick-item.picked { background: rgba(10, 132, 255, 0.1); }

.user-pick-avatar {
  width: 28px; height: 28px; border-radius: 50%;
  background: linear-gradient(135deg, #0A84FF, #5AC8FA);
  display: flex; align-items: center; justify-content: center;
  font-size: 12px; font-weight: 600; color: white; flex-shrink: 0;
}
.user-pick-info { flex: 1; min-width: 0; }
.user-pick-name { display: block; font-size: 13px; color: var(--text-primary); }
.user-pick-dept { display: block; font-size: 11px; color: var(--text-disabled); }
.user-pick-check { color: #0A84FF; font-weight: 600; font-size: 14px; }
.user-picked-summary { margin-top: 8px; font-size: 12px; color: #0A84FF; }
</style>
