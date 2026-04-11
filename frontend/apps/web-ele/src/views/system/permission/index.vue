<script lang="ts" setup>
import {onMounted, ref} from 'vue';

import {Page} from '@vben/common-ui';

import {
  ElButton,
  ElCard,
  ElDrawer,
  ElForm,
  ElFormItem,
  ElInput,
  ElInputNumber,
  ElMessage,
  ElMessageBox,
  ElRadio,
  ElRadioGroup,
  ElSpace,
  ElTag,
  ElTree,
} from 'element-plus';

import {
  createPermissionApi,
  deletePermissionApi,
  getPermissionByIdApi,
  getPermissionListApi,
  PERM_TYPE_OPTIONS,
  type Permission,
  updatePermissionApi,
} from '#/api/system/permission';

const tableData = ref<Permission[]>([]);
const treeData = ref<Permission[]>([]);
const loading = ref(false);

const selectedPerm = ref<Permission | null>(null);

const drawerVisible = ref(false);
const formRef = ref();
const currentId = ref<number | null>(null);
const isEdit = ref(false);

const formData = ref({
  parentId: undefined as number | undefined,
  permName: '',
  permCode: '',
  permType: 'MENU' as 'MENU' | 'BUTTON' | 'API',
  path: '',
  icon: '',
  sort: 0,
  status: 1 as 0 | 1,
});

const rules = {
  permName: [{ required: true, message: '请输入权限名称' }],
  permCode: [{ required: true, message: '请输入权限编码' }],
  permType: [{ required: true, message: '请选择权限类型' }],
};

async function loadData() {
  loading.value = true;
  try {
    const resp = await getPermissionListApi(1, 100);
    tableData.value = resp.records;
    treeData.value = buildTree(resp.records);
  } finally {
    loading.value = false;
  }
}

function buildTree(list: Permission[]): Permission[] {
  const map = new Map<number | string, Permission>();
  const result: Permission[] = [];

  list.forEach((item) => {
    map.set(item.id, { ...item, children: [] });
  });

  list.forEach((item) => {
    const node = map.get(item.id)!;
    if (item.parentId === 0 || item.parentId === undefined || !map.has(item.parentId)) {
      result.push(node);
    } else {
      const parent = map.get(item.parentId)!;
      parent.children = parent.children || [];
      parent.children.push(node);
    }
  });

  return result;
}

async function handleAdd(parentId = 0) {
  isEdit.value = false;
  currentId.value = null;
  formData.value = {
    parentId: parentId || undefined,
    permName: '',
    permCode: '',
    permType: 'MENU',
    path: '',
    icon: '',
    sort: 0,
    status: 1,
  };
  drawerVisible.value = true;
}

async function handleEdit(row: Permission) {
  isEdit.value = true;
  currentId.value = row.id;
  const resp = await getPermissionByIdApi(row.id);
  formData.value = {
    parentId: resp.parentId || undefined,
    permName: resp.permName,
    permCode: resp.permCode,
    permType: resp.permType,
    path: resp.path || '',
    icon: resp.icon || '',
    sort: resp.sort || 0,
    status: resp.status,
  };
  drawerVisible.value = true;
}

async function handleSave() {
  await formRef.value?.validate(async (valid: boolean) => {
    if (!valid) return;

    try {
      if (isEdit.value && currentId.value) {
        await updatePermissionApi({ ...formData.value, id: currentId.value } as any);
        ElMessage.success('更新成功');
      } else {
        await createPermissionApi(formData.value as any);
        ElMessage.success('创建成功');
      }
      drawerVisible.value = false;
      loadData();
    } catch {
      // 错误已在请求拦截器中处理
    }
  });
}

async function handleDelete(row: Permission) {
  try {
    await ElMessageBox.confirm(`确定要删除权限「${row.permName}」吗？`, '提示', {
      type: 'warning',
    });
    await deletePermissionApi(row.id);
    ElMessage.success('删除成功');
    loadData();
  } catch {
    // 用户取消
  }
}

function handleNodeClick(data: Permission) {
  selectedPerm.value = data;
}

function getStatusType(status: number) {
  return status === 1 ? 'success' : 'danger';
}

function getStatusText(status: number) {
  return status === 1 ? '正常' : '禁用';
}

function getPermTypeText(type: string) {
  const item = PERM_TYPE_OPTIONS.find((opt) => opt.value === type);
  return item?.label || '未知';
}

function getPermTypeTagType(type: string) {
  switch (type) {
    case 'MENU':
      return 'success';
    case 'BUTTON':
      return 'warning';
    case 'API':
      return 'info';
    default:
      return 'info';
  }
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <Page title="权限管理" description="管理菜单、按钮、API权限">
    <ElCard>
      <template #header>
        <div class="flex items-center">
          <span class="flex-auto">权限列表</span>
          <ElButton type="primary" @click="() => handleAdd(0)">新建权限</ElButton>
        </div>
      </template>
      <div class="flex">
        <!-- 左侧树形结构 -->
        <div class="w-1/2 border-r pr-4">
          <ElTree
            v-loading="loading"
            :data="treeData"
            :props="{ children: 'children', label: 'permName' }"
            node-key="id"
            default-expand-all
            @node-click="handleNodeClick"
            class="min-h-96"
          >
            <template #default="{ data }">
              <span class="flex items-center justify-between flex-auto">
                <span class="flex items-center gap-2">
                  {{ (data as Permission).permName }}
                  <ElTag :type="getPermTypeTagType((data as Permission).permType)" size="small">
                    {{ getPermTypeText((data as Permission).permType) }}
                  </ElTag>
                </span>
                <ElSpace>
                  <ElButton link type="primary" size="small" @click.stop="() => handleAdd((data as Permission).id)">新增</ElButton>
                  <ElButton link type="primary" size="small" @click.stop="() => handleEdit(data as Permission)">编辑</ElButton>
                  <ElButton link type="danger" size="small" @click.stop="() => handleDelete(data as Permission)">删除</ElButton>
                </ElSpace>
              </span>
            </template>
          </ElTree>
        </div>
        <!-- 右侧详情 -->
        <div class="w-1/2 pl-4">
          <div v-if="selectedPerm">
            <ElForm label-width="100" label-suffix="：" size="small">
              <ElFormItem label="权限名称">{{ selectedPerm.permName }}</ElFormItem>
              <ElFormItem label="权限编码">{{ selectedPerm.permCode }}</ElFormItem>
              <ElFormItem label="权限类型">
                <ElTag :type="getPermTypeTagType(selectedPerm.permType)">
                  {{ getPermTypeText(selectedPerm.permType) }}
                </ElTag>
              </ElFormItem>
              <ElFormItem label="路由路径">{{ selectedPerm.path || '-' }}</ElFormItem>
              <ElFormItem label="状态">
                <ElTag :type="getStatusType(selectedPerm.status)">
                  {{ getStatusText(selectedPerm.status) }}
                </ElTag>
              </ElFormItem>
              <ElFormItem label="排序">{{ selectedPerm.sort }}</ElFormItem>
              <ElFormItem label="创建时间">{{ selectedPerm.createTime || '-' }}</ElFormItem>
            </ElForm>
          </div>
          <div v-else class="text-gray-400 text-center py-8">
            请选择左侧权限查看详情
          </div>
        </div>
      </div>
    </ElCard>

    <!-- 权限表单抽屉 -->
    <ElDrawer
      v-model="drawerVisible"
      :title="isEdit ? '编辑权限' : '新建权限'"
      size="450px"
      @closed="formRef?.resetFields()"
    >
      <ElForm ref="formRef" :model="formData" :rules="rules" label-width="90" label-suffix="：">
        <ElFormItem label="上级权限" v-if="formData.parentId">
          <span>{{ formData.parentId }}</span>
        </ElFormItem>
        <ElFormItem label="权限名称" prop="permName">
          <ElInput v-model="formData.permName" placeholder="请输入权限名称" />
        </ElFormItem>
        <ElFormItem label="权限编码" prop="permCode">
          <ElInput v-model="formData.permCode" placeholder="请输入权限编码，如 user:list" />
        </ElFormItem>
        <ElFormItem label="权限类型" prop="permType">
          <ElRadioGroup v-model="formData.permType">
            <ElRadio v-for="opt in PERM_TYPE_OPTIONS" :key="opt.value" :value="opt.value">
              {{ opt.label }}
            </ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="路由路径">
          <ElInput v-model="formData.path" placeholder="请输入路由路径（仅菜单有效）" />
        </ElFormItem>
        <ElFormItem label="菜单图标">
          <ElInput v-model="formData.icon" placeholder="请输入菜单图标" />
        </ElFormItem>
        <ElFormItem label="排序">
          <ElInputNumber v-model="formData.sort" :min="0" :max="9999" />
        </ElFormItem>
        <ElFormItem label="状态">
          <ElRadioGroup v-model="formData.status">
            <ElRadio :value="1">正常</ElRadio>
            <ElRadio :value="0">禁用</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="drawerVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSave">保存</ElButton>
      </template>
    </ElDrawer>
  </Page>
</template>
