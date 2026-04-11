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
  createDeptApi,
  deleteDeptApi,
  type Dept,
  getDeptByIdApi,
  getDeptListApi,
  updateDeptApi,
} from '#/api/system/dept';

const tableData = ref<Dept[]>([]);
const treeData = ref<Dept[]>([]);
const loading = ref(false);
const expandedKeys = ref<(string | number)[]>([]);

const selectedDept = ref<Dept | null>(null);

const drawerVisible = ref(false);
const formRef = ref();
const currentId = ref<number | null>(null);
const isEdit = ref(false);

const formData = ref({
  parentId: undefined as number | undefined,
  deptName: '',
  sort: 0,
  status: 1 as 0 | 1,
});

const rules = {
  deptName: [{ required: true, message: '请输入部门名称' }],
};

async function loadData() {
  loading.value = true;
  try {
    const resp = await getDeptListApi(1, 100);
    tableData.value = resp.records;
    treeData.value = buildTree(resp.records);
    expandedKeys.value = treeData.value.map((item) => String(item.id));
  } finally {
    loading.value = false;
  }
}

function buildTree(list: Dept[]): Dept[] {
  const map = new Map<number | string, Dept>();
  const result: Dept[] = [];

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
    deptName: '',
    sort: 0,
    status: 1,
  };
  drawerVisible.value = true;
}

async function handleEdit(row: Dept) {
  isEdit.value = true;
  currentId.value = row.id;
  const resp = await getDeptByIdApi(row.id);
  formData.value = {
    parentId: resp.parentId || undefined,
    deptName: resp.deptName,
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
        await updateDeptApi({ ...formData.value, id: currentId.value } as any);
        ElMessage.success('更新成功');
      } else {
        await createDeptApi(formData.value as any);
        ElMessage.success('创建成功');
      }
      drawerVisible.value = false;
      loadData();
    } catch {
      // 错误已在请求拦截器中处理
    }
  });
}

async function handleDelete(row: Dept) {
  try {
    await ElMessageBox.confirm(`确定要删除部门「${row.deptName}」吗？`, '提示', {
      type: 'warning',
    });
    await deleteDeptApi(row.id);
    ElMessage.success('删除成功');
    loadData();
  } catch {
    // 用户取消
  }
}

function handleNodeClick(data: Dept) {
  selectedDept.value = data;
}

function getStatusType(status: number) {
  return status === 1 ? 'success' : 'danger';
}

function getStatusText(status: number) {
  return status === 1 ? '正常' : '禁用';
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <Page title="部门管理" description="管理系统部门结构">
    <ElCard>
      <template #header>
        <div class="flex items-center">
          <span class="flex-auto">部门列表</span>
          <ElButton type="primary" @click="() => handleAdd(0)">新建部门</ElButton>
        </div>
      </template>
      <div class="flex">
        <!-- 左侧树形结构 -->
        <div class="w-1/2 border-r pr-4">
          <ElTree
            v-loading="loading"
            :data="treeData"
            :props="{ children: 'children', label: 'deptName' }"
            node-key="id"
            default-expand-all
            @node-click="handleNodeClick"
            class="min-h-96"
          >
            <template #default="{ data }">
              <span class="flex items-center justify-between flex-auto">
                <span>{{ (data as Dept).deptName }}</span>
                <ElSpace>
                  <ElButton link type="primary" size="small" @click.stop="() => handleAdd((data as Dept).id)">新增</ElButton>
                  <ElButton link type="primary" size="small" @click.stop="() => handleEdit(data as Dept)">编辑</ElButton>
                  <ElButton link type="danger" size="small" @click.stop="() => handleDelete(data as Dept)">删除</ElButton>
                </ElSpace>
              </span>
            </template>
          </ElTree>
        </div>
        <!-- 右侧详情 -->
        <div class="w-1/2 pl-4">
          <div v-if="selectedDept">
            <ElForm label-width="100" label-suffix="：" size="small">
              <ElFormItem label="部门名称">{{ selectedDept.deptName }}</ElFormItem>
              <ElFormItem label="状态">
                <ElTag :type="getStatusType(selectedDept.status)">
                  {{ getStatusText(selectedDept.status) }}
                </ElTag>
              </ElFormItem>
              <ElFormItem label="排序">{{ selectedDept.sort }}</ElFormItem>
              <ElFormItem label="创建时间">{{ selectedDept.createTime || '-' }}</ElFormItem>
            </ElForm>
          </div>
          <div v-else class="text-gray-400 text-center py-8">
            请选择左侧部门查看详情
          </div>
        </div>
      </div>
    </ElCard>

    <!-- 部门表单抽屉 -->
    <ElDrawer
      v-model="drawerVisible"
      :title="isEdit ? '编辑部门' : '新建部门'"
      size="400px"
      @closed="formRef?.resetFields()"
    >
      <ElForm ref="formRef" :model="formData" :rules="rules" label-width="80" label-suffix="：">
        <ElFormItem v-if="formData.parentId" label="上级部门">
          <span>{{ formData.parentId }}</span>
        </ElFormItem>
        <ElFormItem label="部门名称" prop="deptName">
          <ElInput v-model="formData.deptName" placeholder="请输入部门名称" />
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
