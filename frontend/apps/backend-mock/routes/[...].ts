import {defineEventHandler, getQuery, getRequestURL, readBody} from 'h3';

import {generateAccessToken, generateRefreshToken} from '../utils/jwt-utils';
import {MOCK_USERS} from '../utils/mock-data';
import {useResponseError} from '../utils/response';

// 模拟数据
const mockUsers = [
  {
    id: 1,
    username: 'admin',
    nickname: '管理员',
    email: 'admin@evolve.com',
    phone: '13800138000',
    avatar: '',
    deptId: 1,
    status: 1,
    createTime: '2024-01-01 10:00:00'
  },
  {
    id: 2,
    username: 'user1',
    nickname: '用户一',
    email: 'user1@evolve.com',
    phone: '13800138001',
    avatar: '',
    deptId: 2,
    status: 1,
    createTime: '2024-01-02 10:00:00'
  },
  {
    id: 3,
    username: 'user2',
    nickname: '用户二',
    email: 'user2@evolve.com',
    phone: '13800138002',
    avatar: '',
    deptId: 2,
    status: 0,
    createTime: '2024-01-03 10:00:00'
  },
];

const mockDepts = [
  {id: 1, deptName: '研发部', parentId: 0, orderNum: 1},
  {id: 2, deptName: '前端组', parentId: 1, orderNum: 1},
  {id: 3, deptName: '后端组', parentId: 1, orderNum: 2},
  {id: 4, deptName: '运维部', parentId: 0, orderNum: 2},
];

const mockRoles = [
  {
    id: 1,
    roleName: '超级管理员',
    roleKey: 'admin',
    roleCode: 'ROLE_ADMIN',
    dataScope: 1,
    sort: 1,
    status: 1,
    remark: '拥有所有权限',
    createTime: '2024-01-01'
  },
  {
    id: 2,
    roleName: '普通用户',
    roleKey: 'user',
    roleCode: 'ROLE_USER',
    dataScope: 3,
    sort: 2,
    status: 1,
    remark: '普通用户角色',
    createTime: '2024-01-02'
  },
  {
    id: 3,
    roleName: '部门管理员',
    roleKey: 'dept_admin',
    roleCode: 'ROLE_DEPT_ADMIN',
    dataScope: 2,
    sort: 3,
    status: 1,
    remark: '部门管理员角色',
    createTime: '2024-01-03'
  },
];

const mockPermissions = [
  {
    id: 1,
    parentId: 0,
    permName: '系统管理',
    permCode: 'system',
    permType: 'MENU',
    path: '/system',
    icon: 'settings',
    sort: 1,
    status: 1
  },
  {
    id: 11,
    parentId: 1,
    permName: '用户管理',
    permCode: 'system:user',
    permType: 'MENU',
    path: '/system/user',
    icon: 'user',
    sort: 1,
    status: 1
  },
  {
    id: 12,
    parentId: 1,
    permName: '角色管理',
    permCode: 'system:role',
    permType: 'MENU',
    path: '/system/role',
    icon: 'shield',
    sort: 2,
    status: 1
  },
  {
    id: 13,
    parentId: 1,
    permName: '部门管理',
    permCode: 'system:dept',
    permType: 'MENU',
    path: '/system/dept',
    icon: 'building',
    sort: 3,
    status: 1
  },
  {
    id: 14,
    parentId: 1,
    permName: '权限管理',
    permCode: 'system:permission',
    permType: 'MENU',
    path: '/system/permission',
    icon: 'lock',
    sort: 4,
    status: 1
  },
  {
    id: 2,
    parentId: 0,
    permName: '订单管理',
    permCode: 'order',
    permType: 'MENU',
    path: '/order',
    icon: 'shopping-cart',
    sort: 2,
    status: 1
  },
  {
    id: 21,
    parentId: 2,
    permName: '订单列表',
    permCode: 'order:list',
    permType: 'BUTTON',
    sort: 1,
    status: 1
  },
  {
    id: 22,
    parentId: 2,
    permName: '订单详情',
    permCode: 'order:detail',
    permType: 'BUTTON',
    sort: 2,
    status: 1
  },
];

const mockMenu = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: 'BasicLayout',
    meta: {
      title: 'page.dashboard.title',
      icon: 'lucide:home',
      order: 1,
    },
    children: [
      {
        path: '/dashboard/analytics',
        name: 'DashboardAnalytics',
        component: '/dashboard/analytics/index.vue',
        meta: {
          title: 'page.dashboard.analytics',
          icon: 'lucide:chart-line',
        },
      },
      {
        path: '/dashboard/workspace',
        name: 'DashboardWorkspace',
        component: '/dashboard/workspace/index.vue',
        meta: {
          title: 'page.dashboard.workspace',
          icon: 'lucide:layout-dashboard',
        },
      },
    ],
  },
  {
    path: '/system',
    name: 'System',
    component: 'BasicLayout',
    redirect: '/system/user',
    meta: {
      title: 'page.system.title',
      icon: 'lucide:settings',
      order: 200,
    },
    children: [
      {
        path: '/system/user',
        name: 'SystemUser',
        component: '/views/system/user/index.vue',
        meta: {
          title: 'page.system.user',
          icon: 'lucide:user',
        },
      },
      {
        path: '/system/role',
        name: 'SystemRole',
        component: '/views/system/role/index.vue',
        meta: {
          title: 'page.system.role',
          icon: 'lucide:shield',
        },
      },
      {
        path: '/system/dept',
        name: 'SystemDept',
        component: '/views/system/dept/index.vue',
        meta: {
          title: 'page.system.dept',
          icon: 'lucide:building',
        },
      },
      {
        path: '/system/permission',
        name: 'SystemPermission',
        component: '/views/system/permission/index.vue',
        meta: {
          title: 'page.system.permission',
          icon: 'lucide:lock',
        },
      },
    ],
  },
];

function paginate<T>(list: T[], pageNum: number, pageSize: number) {
  const start = (pageNum - 1) * pageSize;
  const end = start + pageSize;
  return {
    records: list.slice(start, end),
    total: list.length,
    pageNum,
    pageSize,
  };
}

// 当前登录的用户
let currentUser = mockUsers[0];

export default defineEventHandler(async (event) => {
  const url = getRequestURL(event);
  const path = url.pathname;
  const method = event.method.toLowerCase();

  // 登录接口
  if (path === '/api/auth/login' && method === 'post') {
    const body = await readBody(event);
    const {username, password} = body;

    // 验证用户 (默认账号: admin/123456)
    if (username === 'admin' && password === '123456') {
      const user = {id: 1, username: 'admin', roles: ['admin']};
      const accessToken = generateAccessToken(user);
      const refreshToken = generateRefreshToken(user);
      return {
        code: 0,
        data: {
          accessToken,
          refreshToken,
          expires: 7 * 24 * 60 * 60,
        },
      };
    }

    // 其他账号
    const foundUser = MOCK_USERS.find((u) => u.username === username && u.password === password);
    if (foundUser) {
      const {password: _, ...userInfo} = foundUser;
      const accessToken = generateAccessToken(userInfo);
      const refreshToken = generateRefreshToken(userInfo);
      return {
        code: 0,
        data: {
          accessToken,
          refreshToken,
          expires: 7 * 24 * 60 * 60,
        },
      };
    }

    return useResponseError('用户名或密码错误');
  }

  // 刷新 Token
  if (path === '/api/auth/refresh' && method === 'post') {
    const body = await readBody(event);
    const {refreshToken} = body;
    // 简化处理：直接生成新token
    const user = {id: 1, username: 'admin', roles: ['admin']};
    const newAccessToken = generateAccessToken(user);
    return {
      code: 0,
      data: newAccessToken,
    };
  }

  // 获取当前用户信息
  if (path === '/api/user/info') {
    return {
      code: 0,
      data: {
        id: currentUser.id,
        username: currentUser.username,
        nickname: currentUser.nickname || currentUser.username,
        email: currentUser.email,
        phone: currentUser.phone,
        avatar: currentUser.avatar,
        roles: ['admin'],
      },
    };
  }

  // 菜单
  if (path === '/api/menu/all') {
    return {code: 0, data: mockMenu};
  }

  // 用户管理
  if (path === '/api/user/list' && method === 'get') {
    const query = getQuery(event);
    const pageNum = Number(query.pageNum) || 1;
    const pageSize = Number(query.pageSize) || 10;
    let filtered = [...mockUsers];
    if (query.username) {
      filtered = filtered.filter((u) => u.username.includes(query.username as string));
    }
    return {code: 0, data: paginate(filtered, pageNum, pageSize)};
  }

  if (path.match(/^\/api\/user\/\d+$/) && method === 'get') {
    const id = Number(path.split('/').pop());
    const user = mockUsers.find((u) => u.id === id);
    if (user) return {code: 0, data: user};
    return {code: 404, message: '用户不存在'};
  }

  if (path === '/api/user/create' && method === 'post') {
    const body = await readBody(event);
    const newUser = {...body, id: mockUsers.length + 1, createTime: new Date().toISOString()};
    mockUsers.push(newUser);
    return {code: 0, data: {id: newUser.id}};
  }

  if (path === '/api/user/update' && method === 'put') {
    const body = await readBody(event);
    const index = mockUsers.findIndex((u) => u.id === body.id);
    if (index !== -1) {
      mockUsers[index] = {...mockUsers[index], ...body};
      return {code: 0, data: {id: body.id}};
    }
    return {code: 404, message: '用户不存在'};
  }

  if (path.match(/^\/api\/user\/\d+$/) && method === 'delete') {
    const id = Number(path.split('/').pop());
    const index = mockUsers.findIndex((u) => u.id === id);
    if (index !== -1) {
      mockUsers.splice(index, 1);
      return {code: 0, data: null};
    }
    return {code: 404, message: '用户不存在'};
  }

  // 部门管理
  if (path === '/api/dept/list' && method === 'get') {
    return {
      code: 0,
      data: {records: mockDepts, total: mockDepts.length, pageNum: 1, pageSize: 100}
    };
  }

  if (path.match(/^\/api\/dept\/\d+$/) && method === 'get') {
    const id = Number(path.split('/').pop());
    const dept = mockDepts.find((d) => d.id === id);
    if (dept) return {code: 0, data: dept};
    return {code: 404, message: '部门不存在'};
  }

  if (path === '/api/dept/create' && method === 'post') {
    const body = await readBody(event);
    const newDept = {...body, id: mockDepts.length + 1, createTime: new Date().toISOString()};
    mockDepts.push(newDept);
    return {code: 0, data: {id: newDept.id}};
  }

  if (path === '/api/dept/update' && method === 'put') {
    const body = await readBody(event);
    const index = mockDepts.findIndex((d) => d.id === body.id);
    if (index !== -1) {
      mockDepts[index] = {...mockDepts[index], ...body};
      return {code: 0, data: {id: body.id}};
    }
    return {code: 404, message: '部门不存在'};
  }

  if (path.match(/^\/api\/dept\/\d+$/) && method === 'delete') {
    const id = Number(path.split('/').pop());
    const index = mockDepts.findIndex((d) => d.id === id);
    if (index !== -1) {
      mockDepts.splice(index, 1);
      return {code: 0, data: null};
    }
    return {code: 404, message: '部门不存在'};
  }

  // 角色管理
  if (path === '/api/role/list' && method === 'get') {
    const query = getQuery(event);
    const pageNum = Number(query.pageNum) || 1;
    const pageSize = Number(query.pageSize) || 10;
    return {code: 0, data: paginate(mockRoles, pageNum, pageSize)};
  }

  if (path.match(/^\/api\/role\/\d+$/) && method === 'get') {
    const id = Number(path.split('/').pop());
    const role = mockRoles.find((r) => r.id === id);
    if (role) return {code: 0, data: role};
    return {code: 404, message: '角色不存在'};
  }

  if (path === '/api/role/create' && method === 'post') {
    const body = await readBody(event);
    const newRole = {...body, id: mockRoles.length + 1, createTime: new Date().toISOString()};
    mockRoles.push(newRole);
    return {code: 0, data: {id: newRole.id}};
  }

  if (path === '/api/role/update' && method === 'put') {
    const body = await readBody(event);
    const index = mockRoles.findIndex((r) => r.id === body.id);
    if (index !== -1) {
      mockRoles[index] = {...mockRoles[index], ...body};
      return {code: 0, data: {id: body.id}};
    }
    return {code: 404, message: '角色不存在'};
  }

  if (path.match(/^\/api\/role\/\d+$/) && method === 'delete') {
    const id = Number(path.split('/').pop());
    const index = mockRoles.findIndex((r) => r.id === id);
    if (index !== -1) {
      mockRoles.splice(index, 1);
      return {code: 0, data: null};
    }
    return {code: 404, message: '角色不存在'};
  }

  // 权限管理
  if (path === '/api/permission/list' && method === 'get') {
    return {
      code: 0,
      data: {records: mockPermissions, total: mockPermissions.length, pageNum: 1, pageSize: 100}
    };
  }

  if (path.match(/^\/api\/permission\/\d+$/) && method === 'get') {
    const id = Number(path.split('/').pop());
    const perm = mockPermissions.find((p) => p.id === id);
    if (perm) return {code: 0, data: perm};
    return {code: 404, message: '权限不存在'};
  }

  if (path === '/api/permission/create' && method === 'post') {
    const body = await readBody(event);
    const newPerm = {...body, id: mockPermissions.length + 1, createTime: new Date().toISOString()};
    mockPermissions.push(newPerm);
    return {code: 0, data: {id: newPerm.id}};
  }

  if (path === '/api/permission/update' && method === 'put') {
    const body = await readBody(event);
    const index = mockPermissions.findIndex((p) => p.id === body.id);
    if (index !== -1) {
      mockPermissions[index] = {...mockPermissions[index], ...body};
      return {code: 0, data: {id: body.id}};
    }
    return {code: 404, message: '权限不存在'};
  }

  if (path.match(/^\/api\/permission\/\d+$/) && method === 'delete') {
    const id = Number(path.split('/').pop());
    const index = mockPermissions.findIndex((p) => p.id === id);
    if (index !== -1) {
      mockPermissions.splice(index, 1);
      return {code: 0, data: null};
    }
    return {code: 404, message: '权限不存在'};
  }

  return {code: 404, message: `API not found: ${path}`};
});
