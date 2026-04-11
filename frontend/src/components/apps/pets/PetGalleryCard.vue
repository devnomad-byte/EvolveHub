<template>
  <div class="pet-mini" :style="{ '--pet-color': pet.color, '--pet-color-light': pet.colorLight }">
    <div class="pet-mini-body">
      <!-- Accessory -->
      <div v-if="pet.accessory === 'antenna'" class="pet-mini-antenna">
        <div class="mini-antenna-stem"></div>
        <div class="mini-antenna-ball"></div>
      </div>
      <div v-if="pet.accessory === 'ears'" class="pet-mini-ears">
        <div class="mini-ear mini-ear-left"></div>
        <div class="mini-ear mini-ear-right"></div>
      </div>
      <div v-if="pet.accessory === 'horn'" class="pet-mini-horn"></div>
      <div v-if="pet.accessory === 'bow'" class="pet-mini-bow"></div>

      <!-- Face -->
      <div class="pet-mini-eyes">
        <div class="pet-mini-eye">
          <div class="mini-pupil"></div>
        </div>
        <div class="pet-mini-eye">
          <div class="mini-pupil"></div>
        </div>
      </div>
      <div class="pet-mini-mouth"></div>
    </div>
    <div class="pet-mini-name">{{ pet.name }}</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  userId: number
  userName: string
}>()

const colorPalettes = [
  { color: '#0A84FF', colorLight: '#5EB5FF' },
  { color: '#BF5AF2', colorLight: '#DA9AFF' },
  { color: '#30D158', colorLight: '#7AE89A' },
  { color: '#FF9F0A', colorLight: '#FFC56E' },
  { color: '#FF6B9D', colorLight: '#FFA3C4' },
  { color: '#64D2FF', colorLight: '#A0E2FF' },
  { color: '#FFD60A', colorLight: '#FFE873' },
  { color: '#FF453A', colorLight: '#FF8A83' }
]

const accessories = ['antenna', 'ears', 'horn', 'bow', 'none'] as const
const petNames = [
  '小蓝', '星尘', '棉花', '泡泡', '闪闪', '团子', '云朵', '豆豆',
  '糯米', '小橙', '雪球', '芒果', '糖糖', '奶茶', '果冻', '布丁'
]

const pet = computed(() => {
  const id = props.userId
  return {
    color: colorPalettes[id % colorPalettes.length].color,
    colorLight: colorPalettes[id % colorPalettes.length].colorLight,
    accessory: accessories[id % accessories.length],
    name: petNames[id % petNames.length]
  }
})
</script>

<style scoped>
.pet-mini {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.pet-mini-body {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--pet-color-light), var(--pet-color));
  position: relative;
  animation: miniFloat 4s ease-in-out infinite;
  box-shadow:
    0 6px 20px rgba(0, 0, 0, 0.2),
    inset 0 -6px 12px rgba(0, 0, 0, 0.1),
    inset 0 6px 12px rgba(255, 255, 255, 0.15);
}

@keyframes miniFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

/* Eyes */
.pet-mini-eyes {
  position: absolute;
  top: 36%;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 12px;
}

.pet-mini-eye {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mini-pupil {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #1a1a2e;
}

.pet-mini-mouth {
  position: absolute;
  bottom: 26%;
  left: 50%;
  transform: translateX(-50%);
  width: 8px;
  height: 4px;
  border-radius: 0 0 8px 8px;
  background: rgba(0, 0, 0, 0.15);
}

/* Mini accessories */
.pet-mini-antenna {
  position: absolute;
  top: -14px;
  left: 50%;
  transform: translateX(-50%);
}

.mini-antenna-stem {
  width: 2px;
  height: 10px;
  background: var(--pet-color);
  margin: 0 auto;
}

.mini-antenna-ball {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--pet-color-light);
  box-shadow: 0 0 6px var(--pet-color);
  margin: -1px auto 0;
}

.pet-mini-ears {
  position: absolute;
  top: -6px;
  left: 0;
  right: 0;
}

.mini-ear {
  position: absolute;
  width: 16px;
  height: 16px;
  border-radius: 50% 50% 0 0;
  background: var(--pet-color);
}

.mini-ear-left { left: 5px; transform: rotate(-15deg); }
.mini-ear-right { right: 5px; transform: rotate(15deg); }

.pet-mini-horn {
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 6px solid transparent;
  border-right: 6px solid transparent;
  border-bottom: 14px solid var(--pet-color-light);
}

.pet-mini-bow {
  position: absolute;
  top: -3px;
  right: -1px;
  width: 14px;
  height: 10px;
  background: #FF6B9D;
  border-radius: 50%;
  box-shadow: -6px 0 0 #FF6B9D;
}

.pet-mini-bow::after {
  content: '';
  position: absolute;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #FF453A;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.pet-mini-name {
  font-size: 11px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 8px;
}
</style>
