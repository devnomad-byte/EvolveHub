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
  createUserApi,
  deleteUserApi,
  getUserByIdApi,
  getUserListApi,
  updateUserApi,
  type User,
} from '#/api/system/user';
import {getDeptListApi} from '#/api/system/dept';

const tableData = ref<User[]>([]);
const loading = ref(false);
const pagination = ref({ pageNum: 1, pageSize: 10, total: 0 });
const searchForm = ref({ username: '', status: '' });

// 用户状态选项
const statusOptions = [
  { value: 1, label: '正常' },
  { value: 0, label: '禁用' },
];

// 部门选项
const deptOptions = ref<{ label: string; value: number }[]>([]);

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
      fieldName: 'username',
      label: '用户名',
      componentProps: {
        placeholder: '请输入用户名',
      },
      rules: [{ required: true, message: '请输入用户名' }],
    },
    {
      component: 'Input',
      fieldName: 'password',
      label: '密码',
      componentProps: {
        type: 'password',
        showPassword: true,
        placeholder: '请输入密码',
      },
      rules: [{ required: true, message: '请输入密码' }],
    },
    {
      component: 'Input',
      fieldName: 'nickname',
      label: '昵称',
      componentProps: {
        placeholder: '请输入昵称',
      },
      rules: [{ required: true, message: '请输入昵称' }],
    },
    {
      component: 'Input',
      fieldName: 'email',
      label: '邮箱',
      componentProps: {
        placeholder: '请输入邮箱',
      },
    },
    {
      component: 'Input',
      fieldName: 'phone',
      label: '手机号',
      componentProps: {
        placeholder: '请输入手机号',
      },
    },
    {
      component: 'Select',
      fieldName: 'deptId',
      label: '部门',
      componentProps: {
        placeholder: '请选择部门',
        options: deptOptions,
        clearable: true,
      },
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
  ],
  showDefaultActions: false,
});

// 抽屉配置
const [Drawer, drawerApi] = useVbenDrawer();

// 加载表格数据
async function loadData() {
  loading.value = true;
  try {
    const resp = await getUserListApi(pagination.value.pageNum, pagination.value.pageSize);
    tableData.value = resp.records;
    pagination.value.total = resp.total;
  } finally {
    loading.value = false;
  }
}

// 加载部门选项
async function loadDeptOptions() {
  const resp = await getDeptListApi(1, 100);
  deptOptions.value = resp.records.map((dept) => ({
    label: dept.deptName,
    value: dept.id,
  }));
}

// 搜索相关
function handleSearch() {
  pagination.value.pageNum = 1;
  loadData();
}

function handleReset() {
  searchForm.value = { username: '', status: '' };
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
  await loadDeptOptions();
  await formApi.resetForm();
  // 编辑时密码非必填
  formApi.updateSchema([
    {
      fieldName: 'password',
      componentProps: { placeholder: '请输入密码' },
      rules: [{ required: true, message: '请输入密码' }],
    },
  ]);
  drawerApi.setState({ title: '新建用户' });
  drawerApi.open();
}

// 编辑
async function handleEdit(row: User) {
  isEdit.value = true;
  currentId.value = row.id;
  await loadDeptOptions();
  const resp = await getUserByIdApi(row.id);
  await formApi.setValues({
    username: resp.username,
    password: '',
    nickname: resp.nickname,
    email: resp.email || '',
    phone: resp.phone || '',
    deptId: resp.deptId || undefined,
    status: resp.status,
  });
  // 编辑时密码非必填
  formApi.updateSchema([
    {
      fieldName: 'password',
      componentProps: { placeholder: '请输入新密码（不填则不修改）' },
      rules: [],
    },
  ]);
  drawerApi.setState({ title: '编辑用户' });
  drawerApi.open();
}

// 保存
async function handleSave() {
  const { valid, values } = await formApi.validate();
  if (!valid) return;

  try {
    if (isEdit.value && currentId.value) {
      // 编辑时移除空密码
      const data = { ...values };
      if (!data.password) {
        delete data.password;
      }
      await updateUserApi({ ...data, id: currentId.value });
      ElMessage.success('更新成功');
    } else {
      await createUserApi(values);
      ElMessage.success('创建成功');
    }
    drawerApi.close();
    loadData();
  } catch {
    // 错误已在请求拦截器中处理
  }
}

// 删除
async function handleDelete(row: User) {
  try {
    await ElMessageBox.confirm(`确定要删除用户「${row.username}」吗？`, '提示', {
      type: 'warning',
    });
    await deleteUserApi(row.id);
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

onMounted(() => {
  loadData();
});
</script>

<template>
  <Page title="用户管理" description="管理系统用户">
    <!-- 搜索区域 -->
    <ElCard class="mb-4">
      <div class="flex gap-4 items-center flex-wrap">
        <div class="flex items-center gap-2">
          <span class="text-sm text-gray-600 whitespace-nowrap">用户名：</span>
          <ElInput v-model="searchForm.username" placeholder="请输入用户名" clearable class="w-48" />
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
          <span class="flex-auto">用户列表</span>
          <ElButton type="primary" @click="handleAdd">新建用户</ElButton>
        </div>
      </template>
      <ElTable v-loading="loading" :data="tableData" stripe>
        <ElTableColumn type="index" label="序号" width="60" />
        <ElTableColumn prop="username" label="用户名" min-width="120" />
        <ElTableColumn prop="nickname" label="昵称" min-width="120" />
        <ElTableColumn prop="email" label="邮箱" min-width="160" />
        <ElTableColumn prop="phone" label="手机号" min-width="120" />
        <ElTableColumn prop="status" label="状态" width="100">
          <template #default="{ row }">
            <ElTag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </ElTag>
          </template>
        </ElTableColumn>
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

    <!-- 用户表单抽屉 -->
    <Drawer title="新建用户" class="w-120">
      <Form />
      <template #footer>
        <ElButton @click="drawerApi.close()">取消</ElButton>
        <ElButton type="primary" @click="handleSave">保存</ElButton>
      </template>
    </Drawer>
  </Page>
</template>
