import type {Language} from 'element-plus/es/locale';

import type {App} from 'vue';
import {ref} from 'vue';

import type {LocaleSetupOptions} from '@vben/locales';
import {$t, loadLocalesMapFromDir, setupI18n as coreSetup,} from '@vben/locales';

import dayjs from 'dayjs';
import defaultLocale from 'element-plus/es/locale/lang/zh-cn';

const elementLocale = ref<Language>(defaultLocale);

const modules = import.meta.glob('./langs/**/*.json');

const localesMap = loadLocalesMapFromDir(
  /\.\/langs\/([^/]+)\/(.*)\.json$/,
  modules,
);

/**
 * 加载应用特有的语言包
 * 这里也可以改造为从服务端获取翻译数据
 * @param lang
 */
async function loadMessages(lang: string) {
  const [appLocaleMessages] = await Promise.all([
    localesMap[lang]?.(),
    loadThirdPartyMessage(),
  ]);
  return appLocaleMessages?.default;
}

/**
 * 加载第三方组件库的语言包
 */
async function loadThirdPartyMessage() {
  await Promise.all([loadElementLocale(), loadDayjsLocale()]);
}

/**
 * 加载dayjs的语言包
 */
async function loadDayjsLocale() {
  const locale = await import('dayjs/locale/zh-cn');
  dayjs.locale(locale);
}

/**
 * 加载element-plus的语言包
 */
async function loadElementLocale() {
  elementLocale.value = defaultLocale;
}

async function setupI18n(app: App, options: LocaleSetupOptions = {}) {
  await coreSetup(app, {
    defaultLocale: 'zh-CN',
    loadMessages,
    missingWarn: !import.meta.env.PROD,
    ...options,
  });
}

export { $t, elementLocale, setupI18n };
