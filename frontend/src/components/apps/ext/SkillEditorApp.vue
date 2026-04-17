<template>
  <div class="skill-editor-app">
    <SkillEditorPanel
      v-if="skillData && skillData.skillId !== null"
      :skill-id="skillData.skillId"
      :skill-name="skillData.skillName"
    />
    <div v-else class="no-skill">
      <FileText :size="48" />
      <p>未指定要编辑的技能</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { FileText } from 'lucide-vue-next'
import { useWindowStore } from '@/stores/window'
import SkillEditorPanel from './SkillEditorPanel.vue'

const winStore = useWindowStore()
const skillData = ref<{ skillId: number | null; skillName: string } | null>(null)

onMounted(() => {
  // 获取要编辑的技能信息
  skillData.value = winStore.pendingSkillToEdit
  // 清除待编辑数据，防止下次打开时残留
  winStore.pendingSkillToEdit = null
})
</script>

<style scoped>
.skill-editor-app {
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
