<template>
  <div class="skill-edit-app">
    <SkillEditPanel
      v-if="skillData !== undefined"
      :skill-id="skillData?.skillId ?? null"
      :skill-name="skillData?.skillName ?? ''"
    />
    <div v-else class="no-skill">
      <Sparkles :size="48" />
      <p>未指定要编辑的技能</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Sparkles } from 'lucide-vue-next'
import { useWindowStore } from '@/stores/window'
import SkillEditPanel from './SkillEditPanel.vue'

const winStore = useWindowStore()
const skillData = ref<{ skillId: number | null; skillName: string } | undefined>(undefined)

onMounted(() => {
  skillData.value = winStore.pendingSkillToEdit ?? undefined
  winStore.pendingSkillToEdit = null
})
</script>

<style scoped>
.skill-edit-app {
  height: 100%;
  background: #1C1C1E;
}

.no-skill {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-disabled);
  gap: 16px;
}

.no-skill svg {
  opacity: 0.3;
}

.no-skill p {
  font-size: 14px;
  margin: 0;
}
</style>
