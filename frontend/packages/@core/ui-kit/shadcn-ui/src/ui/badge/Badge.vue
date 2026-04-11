<script setup lang="ts">
import type {BadgeVariants} from './badge';
import {badgeVariants} from './badge';

import {cn} from '@vben-core/shared/utils';

const props = defineProps<{
  class?: any;
  variant?: BadgeVariants['variant'];
}>();
</script>

<template>
  <div :class="cn(badgeVariants({ variant }), props.class)">
    <slot></slot>
  </div>
</template>
