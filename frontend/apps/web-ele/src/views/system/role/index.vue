<script lang="ts" setup>
import {onMounted, ref} from 'vue';

import {Page, useVbenDrawer} from '@vben/common-ui';
import {
  ElButton,
  ElCard,
  ElInput,
  ElMessage,
  ElMessageBox,
  ElPagination,
  ElTable,
  ElTableColumn,
  ElTag,
} from 'element-plus';

import {useVbenForm} from '#/adapter/form';
import {
  createRoleApi,
  DATA_SCOPE_OPTIONS,
  deleteRoleApi,
  getRoleByIdApi,
  getRoleListApi,
  type Role,
  updateRoleApi,
} from '#/api/system/role';

const tableData = ref<Role[]>([]);
const loading = ref(false);
const pagination = ref({ pageNum: 1, pageSize: 10, total: 0 });
const searchForm = ref({ roleName: '', roleCode: '', status: '' });

// 状态选项
const statusOptions = [
  { value: 1, label: '正常' },
  { value: 0, label: '禁用' },
];

// 当前编辑行ID
const currentId = ref<number | null>(null);
const isEdit = ref(false);

// 表单配置
const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
  },
  layout: 'horizontal',
  wrapperClass: 'grid-cols-1',
  schema: [
    {
      component: 'Input',
      fieldName: 'roleName',
      label: '角色名称',
      componentProps: {
        placeholder: '请输入角色名称',
      },
      rules: [{ required: true, message: '请输入角色名称' }],
    },
    {
      component: 'Input',
      fieldName: 'roleCode',
      label: '角色编码',
      componentProps: {
        placeholder: '请输入角色编码，如 ROLE_ADMIN',
      },
      rules: [{ required: true, message: '请输入角色编码' }],
    },
    {
      component: 'RadioGroup',
      fieldName: 'dataScope',
      label: '数据范围',
      componentProps: {
        options: DATA_SCOPE_OPTIONS,
      },
      defaultValue: 1,
    },
    {
      component: 'InputNumber',
      fieldName: 'sort',
      label: '排序',
      componentProps: {
        min: 0,
        max: 9999,
      },
      defaultValue: 0,
    },
    {
      component: 'RadioGroup',
      fieldName: 'status',
      label: '状态',
      componentProps: {
        options: statusOptions,
      },
      defaultValue: 1,
    },
    {
      component: 'Input',
      fieldName: 'remark',
      label: '备注',
      componentProps: {
        type: 'textarea',
        rows: 3,
        placeholder: '请输入备注',
      },
    },
  ],
  showDefaultActions: false,
});

// 抽屉配置
const [Drawer, drawerApi] = useVbenDrawer();

// 加载表格数据
async function loadData() {
  loading.value = true;
  try {
    const resp = await getRoleListApi(pagination.value.pageNum, pagination.value.pageSize);
    tableData.value = resp.records;
    pagination.value.total = resp.total;
  } finally {
    loading.value = false;
  }
}

// 搜索相关
function handleSearch() {
  pagination.value.pageNum = 1;
  loadData();
}

function handleReset() {
  searchForm.value = { roleName: '', roleCode: '', status: '' };
  handleSearch();
}

function handlePageChange(page: number) {
  pagination.value.pageNum = page;
  loadData();
}

// 新增
async function handleAdd() {
  isEdit.value = false;
  currentId.value = null;
  await formApi.resetForm();
  drawerApi.setState({ title: '新建角色' });
  drawerApi.open();
}

// 编辑
async function handleEdit(row: Role) {
  isEdit.value = true;
  currentId.value = row.id;
  const resp = await getRoleByIdApi(row.id);
  await formApi.setValues({
    roleName: resp.roleName,
    roleCode: resp.roleCode,
    dataScope: resp.dataScope,
    sort: resp.sort || 0,
    status: resp.status,
    remark: resp.remark || '',
  });
  drawerApi.setState({ title: '编辑角色' });
  drawerApi.open();
}

// 保存
async function handleSave() {
  const { valid, values } = await formApi.validate();
  if (!valid) return;

  try {
    if (isEdit.value && currentId.value) {
      await updateRoleApi({ ...values, id: currentId.value });
      ElMessage.success('更新成功');
    } else {
      await createRoleApi(values);
      ElMessage.success('创建成功');
    }
    drawerApi.close();
    loadData();
  } catch {
    // 错误已在请求拦截器中处理
  }
}

// 删除
async function handleDelete(row: Role) {
  try {
    await ElMessageBox.confirm(`确定要删除角色「${row.roleName}」吗？`, '提示', {
      type: 'warning',
    });
    await deleteRoleApi(row.id);
    ElMessage.success('删除成功');
    loadData();
  } catch {
    // 用户取消
  }
}

// 状态显示
function getStatusType(status: number) {
  return status === 1 ? 'success' : 'danger';
}

function getStatusText(status: number) {
  return status === 1 ? '正常' : '禁用';
}

function getDataScopeText(dataScope: number) {
  const item = DATA_SCOPE_OPTIONS.find((opt) => opt.value === dataScope);
  return item?.label || '未知';
}

onMounted(() => {
  loadData();
});
</script>

<template>
  <Page title="角色管理" description="管理角色及权限">
    <!-- 搜索区域 -->
    <ElCard class="mb-4">
      <div class="flex gap-4 items-center flex-wrap">
        <div class="flex items-center gap-2">
          <span class="text-sm text-gray-600 whitespace-nowrap">角色名称：</span>
          <ElInput v-model="searchForm.roleName" placeholder="请输入角色名称" clearable class="w-48" />
        </div>
        <div class="flex items-center gap-2">
          <span class="text-sm text-gray-600 whitespace-nowrap">角色编码：</span>
          <ElInput v-model="searchForm.roleCode" placeholder="请输入角色编码" clearable class="w-48" />
        </div>
        <div class="flex items-center gap-2">
          <span class="text-sm text-gray-600 whitespace-nowrap">状态：</span>
          <ElInput v-model="searchForm.status" placeholder="请选择状态" clearable class="w-48" />
        </div>
        <div class="flex gap-2">
          <ElButton type="primary" @click="handleSearch">查询</ElButton>
          <ElButton @click="handleReset">重置</ElButton>
        </div>
      </div>
    </ElCard>

    <!-- 表格区域 -->
    <ElCard>
      <template #header>
        <div class="flex items-center">
          <span class="flex-auto">角色列表</span>
          <ElButton type="primary" @click="handleAdd">新建角色</ElButton>
        </div>
      </template>
      <ElTable v-loading="loading" :data="tableData" stripe>
        <ElTableColumn type="index" label="序号" width="60" />
        <ElTableColumn prop="roleName" label="角色名称" min-width="120" />
        <ElTableColumn prop="roleCode" label="角色编码" min-width="140" />
        <ElTableColumn prop="dataScope" label="数据范围" min-width="120">
          <template #default="{ row }">
            <ElTag>{{ getDataScopeText(row.dataScope) }}</ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="sort" label="排序" width="80" />
        <ElTableColumn prop="status" label="状态" width="100">
          <template #default="{ row }">
            <ElTag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <ElTableColumn prop="createTime" label="创建时间" width="180" />
        <ElTableColumn label="操作" width="200">
          <template #default="{ row }">
            <ElButton link type="primary" @click="handleEdit(row)">编辑</ElButton>
            <ElButton link type="danger" @click="handleDelete(row)">删除</ElButton>
          </template>
        </ElTableColumn>
      </ElTable>
      <div class="mt-4 flex justify-end">
        <ElPagination
          :current-page="pagination.pageNum"
          :page-size="pagination.pageSize"
          :total="pagination.total"
          background
          layout="total, prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </ElCard>

    <!-- 角色表单抽屉 -->
    <Drawer title="新建角色" class="w-120">
      <Form />
      <template #footer>
        <ElButton @click="drawerApi.close()">取消</ElButton>
        <ElButton type="primary" @click="handleSave">保存</ElButton>
      </template>
    </Drawer>
  </Page>
</template>
