<template>
  <div class="env-var-app">
    <div class="app-header">
      <h3>环境变量</h3>
      <button class="btn btn-primary" @click="openAddModal">
        <Plus :size="16" /> 新增变量
      </button>
    </div>

    <!-- 分组筛选 -->
    <div class="group-tabs">
      <button
        v-for="g in groups"
        :key="g.value"
        :class="['tab-btn', { active: selectedGroup === g.value }]"
        @click="selectGroup(g.value)"
      >
        {{ g.label }}
      </button>
    </div>

    <!-- 表格 -->
    <div class="table-container">
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <span>加载中...</span>
      </div>
      <div v-else-if="records.length === 0" class="empty-state">
        <Settings :size="32" />
        <span>暂无环境变量</span>
        <button class="btn btn-sm" @click="openAddModal">新增第一个变量</button>
      </div>
      <table v-else class="env-table">
        <thead>
          <tr>
            <th style="width: 40px"><input type="checkbox" v-model="allChecked" @change="toggleAll" /></th>
            <th>变量名</th>
            <th>值</th>
            <th>分组</th>
            <th>描述</th>
            <th>状态</th>
            <th style="width: 120px">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in records" :key="item.id">
            <td><input type="checkbox" v-model="checkedIds" :value="item.id" /></td>
            <td class="key-cell">
              <code>{{ item.varKey }}</code>
              <span v-if="item.isSensitive === 1" class="sensitive-badge">敏感</span>
            </td>
            <td class="value-cell">
              <div class="value-wrapper">
                <input
                  :type="isVisible[item.id] ? 'text' : 'password'"
                  :value="item.varValue"
                  readonly
                  class="value-input"
                />
                <button class="toggle-btn" @click="toggleVisible(item.id)" :title="isVisible[item.id] ? '隐藏' : '显示'">
                  <Eye v-if="!isVisible[item.id]" :size="14" />
                  <EyeOff v-else :size="14" />
                </button>
              </div>
            </td>
            <td>
              <span class="group-badge" :style="getGroupStyle(item.varGroup)">{{ item.varGroup }}</span>
            </td>
            <td class="desc-cell">{{ item.description || '-' }}</td>
            <td>
              <span :class="['status-badge', item.status === 1 ? 'status-on' : 'status-off']">
                {{ item.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td class="action-cell">
              <button class="icon-btn" @click="openEditModal(item)" title="编辑">
                <Pencil :size="15" />
              </button>
              <button class="icon-btn danger" @click="deleteOne(item)" title="删除">
                <Trash2 :size="15" />
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 底部操作栏 -->
    <div v-if="records.length > 0" class="bottom-bar">
      <span class="checked-count">已选 {{ checkedIds.length }} 项</span>
      <button class="btn btn-sm btn-danger" @click="deleteChecked" :disabled="checkedIds.length === 0">
        <Trash2 :size="14" /> 删除选中
      </button>
    </div>

    <!-- 编辑弹窗 -->
    <Teleport to="body">
      <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
        <div class="modal env-modal">
          <div class="modal-header">
            <h3>{{ editingItem ? '编辑环境变量' : '新增环境变量' }}</h3>
            <button class="modal-close" @click="closeModal"><X :size="18" /></button>
          </div>
          <div class="modal-body">
            <div class="form-row">
              <label>变量名</label>
              <input
                v-model="form.varKey"
                type="text"
                placeholder="如 OPENAI_API_KEY"
                :disabled="!!editingItem"
                :class="['form-input', { error: errors.varKey }]"
              />
              <span v-if="errors.varKey" class="error-text">{{ errors.varKey }}</span>
            </div>
            <div class="form-row">
              <label>变量值</label>
              <div class="value-input-wrap">
                <input
                  v-model="form.varValue"
                  :type="formVisible ? 'text' : 'password'"
                  placeholder="变量值"
                  :class="['form-input', { error: errors.varValue }]"
                />
                <button class="toggle-btn" @click="formVisible = !formVisible">
                  <Eye v-if="!formVisible" :size="14" />
                  <EyeOff v-else :size="14" />
                </button>
              </div>
              <span v-if="errors.varValue" class="error-text">{{ errors.varValue }}</span>
            </div>
            <div class="form-row">
              <label>分组</label>
              <select v-model="form.varGroup" class="form-input">
                <option value="DEFAULT">DEFAULT</option>
                <option value="OPENAI">OPENAI</option>
                <option value="MODELSCOPE">MODELSCOPE</option>
                <option value="S3">S3</option>
                <option value="CUSTOM">CUSTOM</option>
              </select>
            </div>
            <div class="form-row">
              <label>描述</label>
              <input v-model="form.description" type="text" placeholder="描述说明" class="form-input" />
            </div>
            <div class="form-row inline">
              <label>
                <input type="checkbox" v-model="form.isSensitive" :true-value="1" :false-value="0" />
                敏感值（前端默认隐藏）
              </label>
              <label>
                <input type="checkbox" v-model="form.status" :true-value="1" :false-value="0" />
                启用
              </label>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn" @click="closeModal">取消</button>
            <button class="btn btn-primary" @click="submitForm" :disabled="saving">
              {{ saving ? '保存中...' : '确定' }}
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, Settings, Eye, EyeOff, Pencil, Trash2, X } from 'lucide-vue-next'
import { envVarApi, type EnvVarItem } from '@/api/envVar'
import { useDesktopStore } from '@/stores/desktop'
import { useConfirm } from '@/composables/useConfirm'

const desktop = useDesktopStore()
const { confirm } = useConfirm()

const loading = ref(false)
const records = ref<EnvVarItem[]>([])
const selectedGroup = ref('')
const checkedIds = ref<number[]>([])
const allChecked = ref(false)
const showModal = ref(false)
const editingItem = ref<EnvVarItem | null>(null)
const saving = ref(false)
const formVisible = ref(false)
const isVisible = ref<Record<number, boolean>>({})

const groups = [
  { label: '全部', value: '' },
  { label: 'OPENAI', value: 'OPENAI' },
  { label: 'MODELSCOPE', value: 'MODELSCOPE' },
  { label: 'S3', value: 'S3' },
  { label: 'CUSTOM', value: 'CUSTOM' },
  { label: 'DEFAULT', value: 'DEFAULT' }
]

const form = reactive({
  varKey: '',
  varValue: '',
  varGroup: 'DEFAULT',
  description: '',
  isSensitive: 0,
  status: 1
})

const errors = reactive({
  varKey: '',
  varValue: ''
})

function getGroupStyle(group: string) {
  const styles: Record<string, string> = {
    OPENAI: 'background: rgba(16, 163, 74, 0.3)',
    MODELSCOPE: 'background: rgba(56, 239, 125, 0.3)',
    S3: 'background: rgba(100, 210, 255, 0.3)',
    CUSTOM: 'background: rgba(255, 159, 10, 0.3)',
    DEFAULT: 'background: rgba(255, 255, 255, 0.1)'
  }
  return styles[group] || styles.DEFAULT
}

function toggleVisible(id: number) {
  isVisible.value[id] = !isVisible.value[id]
}

function toggleAll() {
  if (allChecked.value) {
    checkedIds.value = records.value.map(r => r.id)
  } else {
    checkedIds.value = []
  }
}

async function loadData() {
  loading.value = true
  try {
    const res = await envVarApi.list(1, 100, selectedGroup.value || undefined)
    records.value = res.records || []
    checkedIds.value = []
    allChecked.value = false
  } catch (e) {
    console.error('加载失败', e)
  } finally {
    loading.value = false
  }
}

function selectGroup(group: string) {
  selectedGroup.value = group
  loadData()
}

function openAddModal() {
  editingItem.value = null
  form.varKey = ''
  form.varValue = ''
  form.varGroup = 'DEFAULT'
  form.description = ''
  form.isSensitive = 0
  form.status = 1
  formVisible.value = false
  errors.varKey = ''
  errors.varValue = ''
  showModal.value = true
}

function openEditModal(item: EnvVarItem) {
  editingItem.value = item
  form.varKey = item.varKey
  form.varValue = item.varValue
  form.varGroup = item.varGroup || 'DEFAULT'
  form.description = item.description || ''
  form.isSensitive = item.isSensitive || 0
  form.status = item.status || 1
  formVisible.value = false
  errors.varKey = ''
  errors.varValue = ''
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

function validateForm(): boolean {
  errors.varKey = ''
  errors.varValue = ''

  if (!form.varKey.trim()) {
    errors.varKey = '变量名不能为空'
  } else if (!/^[A-Za-z_][A-Za-z0-9_]*$/.test(form.varKey.trim())) {
    errors.varKey = '格式：以字母或下划线开头'
  }

  if (!form.varValue) {
    errors.varValue = '变量值不能为空'
  }

  return !errors.varKey && !errors.varValue
}

async function submitForm() {
  if (!validateForm()) return
  saving.value = true
  try {
    if (editingItem.value) {
      await envVarApi.update({
        id: editingItem.value.id,
        varKey: form.varKey.trim(),
        varValue: form.varValue,
        varGroup: form.varGroup,
        description: form.description,
        isSensitive: form.isSensitive,
        status: form.status
      })
      desktop.addToast('更新成功', 'success')
    } else {
      await envVarApi.create({
        varKey: form.varKey.trim(),
        varValue: form.varValue,
        varGroup: form.varGroup,
        description: form.description,
        isSensitive: form.isSensitive,
        status: form.status
      })
      desktop.addToast('创建成功', 'success')
    }
    closeModal()
    loadData()
  } catch (e: any) {
    desktop.addToast(e.message || '操作失败', 'error')
  } finally {
    saving.value = false
  }
}

async function deleteOne(item: EnvVarItem) {
  if (!await confirm('删除变量', `确定删除变量 "${item.varKey}" 吗？`, { danger: true })) return
  try {
    await envVarApi.delete(item.id)
    desktop.addToast('删除成功', 'success')
    loadData()
  } catch (e: any) {
    desktop.addToast(e.message || '删除失败', 'error')
  }
}

async function deleteChecked() {
  if (checkedIds.value.length === 0) return
  if (!await confirm('删除变量', `确定删除选中的 ${checkedIds.value.length} 个变量吗？`, { danger: true })) return
  try {
    await Promise.all(checkedIds.value.map(id => envVarApi.delete(id)))
    desktop.addToast('删除成功', 'success')
    loadData()
  } catch (e: any) {
    desktop.addToast(e.message || '删除失败', 'error')
  }
}

onMounted(loadData)
</script>

<style scoped>
.env-var-app {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: var(--bg-primary);
  overflow: hidden;
}

.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

.app-header h3 {
  font-size: 15px;
  font-weight: 600;
  margin: 0;
}

.group-tabs {
  display: flex;
  gap: 8px;
  padding: 12px 20px;
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
  flex-wrap: wrap;
}

.tab-btn {
  padding: 5px 14px;
  border-radius: 6px;
  border: 1px solid var(--border-subtle);
  background: rgba(255, 255, 255, 0.03);
  color: var(--text-secondary);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.15s;
}

.tab-btn:hover {
  background: rgba(255, 255, 255, 0.08);
}

.tab-btn.active {
  background: rgba(10, 132, 255, 0.2);
  border-color: rgba(10, 132, 255, 0.4);
  color: #0A84FF;
}

.table-container {
  flex: 1;
  overflow-y: auto;
  padding: 12px 20px;
}

.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: var(--text-disabled);
  gap: 12px;
}

.loading .spinner {
  width: 24px;
  height: 24px;
  border: 2px solid var(--border-subtle);
  border-top-color: #0A84FF;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-state {
  border: 1px dashed var(--border-subtle);
  border-radius: 12px;
}

.env-table {
  width: 100%;
  border-collapse: collapse;
}

.env-table th,
.env-table td {
  padding: 10px 12px;
  text-align: left;
  border-bottom: 1px solid var(--border-subtle);
  font-size: 13px;
}

.env-table th {
  font-weight: 500;
  color: var(--text-secondary);
  font-size: 12px;
}

.env-table tbody tr:hover {
  background: rgba(255, 255, 255, 0.03);
}

.key-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.key-cell code {
  font-family: 'SF Mono', monospace;
  font-size: 12px;
  color: var(--text-primary);
  background: rgba(255, 255, 255, 0.06);
  padding: 2px 6px;
  border-radius: 4px;
}

.sensitive-badge {
  font-size: 10px;
  padding: 2px 6px;
  background: rgba(255, 159, 10, 0.2);
  color: #FF9F0A;
  border-radius: 4px;
}

.value-cell {
  min-width: 200px;
}

.value-wrapper {
  display: flex;
  align-items: center;
  gap: 4px;
}

.value-input {
  flex: 1;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--border-subtle);
  border-radius: 6px;
  padding: 5px 10px;
  color: var(--text-primary);
  font-size: 12px;
  font-family: monospace;
  width: 100%;
}

.toggle-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 6px;
  color: var(--text-secondary);
  cursor: pointer;
  flex-shrink: 0;
}

.toggle-btn:hover {
  background: rgba(255, 255, 255, 0.12);
  color: var(--text-primary);
}

.group-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  color: #fff;
}

.desc-cell {
  max-width: 200px;
  color: var(--text-secondary);
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-badge {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
}

.status-on {
  background: rgba(48, 209, 88, 0.2);
  color: #30D158;
}

.status-off {
  background: rgba(255, 69, 58, 0.2);
  color: #FF453A;
}

.action-cell {
  display: flex;
  gap: 6px;
}

.icon-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border: none;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 6px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.15s;
}

.icon-btn:hover {
  background: rgba(255, 255, 255, 0.12);
  color: var(--text-primary);
}

.icon-btn.danger:hover {
  background: rgba(255, 69, 58, 0.2);
  color: #FF453A;
}

.bottom-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border-top: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

.checked-count {
  font-size: 12px;
  color: var(--text-secondary);
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: #1E1E22;
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.5);
}

.env-modal {
  width: 480px;
  max-width: 95%;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-subtle);
}

.modal-header h3 {
  font-size: 15px;
  font-weight: 600;
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  display: flex;
}

.modal-close:hover {
  background: rgba(255, 255, 255, 0.1);
  color: var(--text-primary);
}

.modal-body {
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-row.inline {
  flex-direction: row;
  align-items: center;
  gap: 16px;
}

.form-row label {
  font-size: 13px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 6px;
}

.form-input {
  padding: 9px 12px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid var(--border-subtle);
  border-radius: 8px;
  color: var(--text-primary);
  font-size: 13px;
}

.form-input:focus {
  outline: none;
  border-color: #0A84FF;
}

.form-input.error {
  border-color: #FF453A;
}

.form-input:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.error-text {
  font-size: 11px;
  color: #FF453A;
}

.value-input-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
}

.value-input-wrap .form-input {
  flex: 1;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid var(--border-subtle);
}

/* Btn styles */
.btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  background: rgba(255, 255, 255, 0.08);
  color: var(--text-primary);
  transition: all 0.15s;
}

.btn:hover {
  background: rgba(255, 255, 255, 0.12);
}

.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
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

.btn-danger {
  background: rgba(255, 69, 58, 0.2);
  color: #FF453A;
}

.btn-danger:hover {
  background: rgba(255, 69, 58, 0.3);
}

.btn-danger:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
</style>
