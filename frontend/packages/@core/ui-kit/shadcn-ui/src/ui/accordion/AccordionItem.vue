<script setup lang="ts">
import type {AccordionItemProps} from 'reka-ui';
import {AccordionItem, useForwardProps} from 'reka-ui';

import {computed} from 'vue';

import {cn} from '@vben-core/shared/utils';

const props = defineProps<AccordionItemProps & { class?: any }>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});

const forwardedProps = useForwardProps(delegatedProps);
</script>

<template>
  <AccordionItem v-bind="forwardedProps" :class="cn('border-b', props.class)">
    <slot></slot>
  </AccordionItem>
</template>
