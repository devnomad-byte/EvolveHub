<template>
  <div class="pet-gallery">
    <div class="pg-header">
      <h2 class="pg-title">宠物管理</h2>
      <span class="pg-subtitle">查看所有用户的 AI 宠物</span>
    </div>

    <div class="pg-grid">
      <div v-for="p in allPets" :key="p.userId" class="pg-card">
        <!-- Pet display -->
        <div class="pg-card-pet">
          <DesktopPetMini :user-id="p.userId" :user-name="p.userName" />
        </div>

        <!-- Info -->
        <div class="pg-card-info">
          <div class="pg-user-row">
            <span class="pg-avatar">{{ p.userAvatar }}</span>
            <div>
              <div class="pg-user-name">{{ p.userName }}</div>
              <div class="pg-user-dept">{{ p.userDept }}</div>
            </div>
          </div>
          <div class="pg-stats">
            <div class="pg-stat">
              <span class="pg-stat-label">等级</span>
              <span class="pg-stat-value">Lv.{{ p.level }}</span>
            </div>
            <div class="pg-stat">
              <span class="pg-stat-label">心情</span>
              <span class="pg-stat-value">{{ p.mood }}</span>
            </div>
            <div class="pg-stat">
              <span class="pg-stat-label">经验值</span>
              <span class="pg-stat-value">{{ p.exp }}</span>
            </div>
          </div>
          <div class="pg-exp-bar">
            <div class="pg-exp-fill" :style="{ width: p.expPercent + '%', background: p.color }"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import DesktopPetMini from './PetGalleryCard.vue'

interface PetInfo {
  userId: number
  userName: string
  userAvatar: string
  userDept: string
  level: number
  exp: number
  expPercent: number
  mood: string
  color: string
}

const allPets = ref<PetInfo[]>([
  { userId: 1, userName: '张管理', userAvatar: '🧑‍💼', userDept: '总经办', level: 11, exp: 2450, expPercent: 72, mood: '开心', color: '#0A84FF' },
  { userId: 2, userName: '李用户', userAvatar: '👤', userDept: '技术部', level: 8, exp: 1820, expPercent: 45, mood: '好奇', color: '#BF5AF2' },
  { userId: 3, userName: '王产品', userAvatar: '👩‍💻', userDept: '产品部', level: 6, exp: 980, expPercent: 60, mood: '平静', color: '#30D158' },
  { userId: 4, userName: '刘设计', userAvatar: '🎨', userDept: '设计部', level: 5, exp: 720, expPercent: 35, mood: '困倦', color: '#FF9F0A' },
  { userId: 5, userName: '陈运维', userAvatar: '🔧', userDept: '运维部', level: 9, exp: 2100, expPercent: 88, mood: '兴奋', color: '#FF6B9D' },
  { userId: 6, userName: '赵数据', userAvatar: '📊', userDept: '数据部', level: 7, exp: 1560, expPercent: 55, mood: '开心', color: '#64D2FF' }
])
</script>

<style scoped>
.pet-gallery {
  height: 100%;
  overflow-y: auto;
  background: #0a0a14;
}

.pet-gallery::-webkit-scrollbar {
  width: 4px;
}

.pet-gallery::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}

.pg-header {
  padding: 20px 24px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.pg-title {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  margin: 0;
}

.pg-subtitle {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.35);
  margin-top: 4px;
  display: block;
}

.pg-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  padding: 20px 24px;
}

.pg-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  overflow: hidden;
  transition: all 200ms ease;
}

.pg-card:hover {
  border-color: rgba(10, 132, 255, 0.2);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  transform: translateY(-2px);
}

.pg-card-pet {
  display: flex;
  justify-content: center;
  padding: 24px 16px 12px;
  background: rgba(0, 0, 0, 0.2);
}

.pg-card-info {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pg-user-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.pg-avatar {
  font-size: 24px;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.06);
  display: flex;
  align-items: center;
  justify-content: center;
}

.pg-user-name {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

.pg-user-dept {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.35);
  margin-top: 1px;
}

.pg-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.pg-stat {
  text-align: center;
  padding: 6px 0;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
}

.pg-stat-label {
  display: block;
  font-size: 10px;
  color: rgba(255, 255, 255, 0.3);
  margin-bottom: 2px;
}

.pg-stat-value {
  font-size: 13px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
}

.pg-exp-bar {
  height: 3px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 3px;
  overflow: hidden;
}

.pg-exp-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 1s ease;
}
</style>
