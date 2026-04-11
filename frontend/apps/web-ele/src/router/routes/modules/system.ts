import type {RouteRecordRaw} from 'vue-router';

import {$t} from '#/locales';

const routes: RouteRecordRaw[] = [
  {
    meta: {
      icon: 'lucide:settings',
      order: 200,
      title: $t('page.system.title'),
    },
    name: 'System',
    path: '/system',
    children: [
      {
        name: 'SystemUser',
        path: '/system/user',
        component: () => import('#/views/system/user/index.vue'),
        meta: {
          icon: 'lucide:user',
          title: $t('page.system.user'),
        },
      },
      {
        name: 'SystemRole',
        path: '/system/role',
        component: () => import('#/views/system/role/index.vue'),
        meta: {
          icon: 'lucide:shield',
          title: $t('page.system.role'),
        },
      },
      {
        name: 'SystemDept',
        path: '/system/dept',
        component: () => import('#/views/system/dept/index.vue'),
        meta: {
          icon: 'lucide:building',
          title: $t('page.system.dept'),
        },
      },
      {
        name: 'SystemPermission',
        path: '/system/permission',
        component: () => import('#/views/system/permission/index.vue'),
        meta: {
          icon: 'lucide:lock',
          title: $t('page.system.permission'),
        },
      },
    ],
  },
];

export default routes;
