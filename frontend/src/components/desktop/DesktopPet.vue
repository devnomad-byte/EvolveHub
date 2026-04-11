<template>
  <div
    class="pet-stage"
    ref="stageRef"
  >
    <div
      class="pet-creature"
      :class="[
        'state-' + state,
        facing,
        { 'pet-clicked': justClicked }
      ]"
      :style="{
        '--pet-color': pet.color,
        '--pet-color-light': pet.colorLight,
        left: posX + 'px',
        bottom: '0px',
        transition: state === 'walking' ? 'left 2.5s linear' : 'left 0.5s ease'
      }"
      @click="handlePetClick"
    >
      <!-- zZZ when sleeping -->
      <Transition name="zzz">
        <div v-if="state === 'sleeping'" class="pet-zzz">
          <span class="z z1">z</span>
          <span class="z z2">z</span>
          <span class="z z3">z</span>
        </div>
      </Transition>

      <!-- Shadow -->
      <div class="pet-shadow" :class="{ 'shadow-sleep': state === 'sleeping' }"></div>

      <!-- Body -->
      <div class="pet-body" :class="'mood-' + mood">
        <!-- Accessories -->
        <div v-if="pet.accessory === 'antenna'" class="pet-antenna">
          <div class="antenna-stem"></div>
          <div class="antenna-ball"></div>
        </div>
        <div v-if="pet.accessory === 'ears'" class="pet-ears">
          <div class="ear ear-left"></div>
          <div class="ear ear-right"></div>
        </div>
        <div v-if="pet.accessory === 'horn'" class="pet-horn"></div>
        <div v-if="pet.accessory === 'bow'" class="pet-bow"></div>

        <!-- Face -->
        <div class="pet-face">
          <div class="pet-eyes">
            <div class="pet-eye left" :class="{ blink: isBlinking }">
              <div class="pupil" :style="{ transform: 'translate(' + lookX + 'px,' + lookY + 'px)' }"></div>
            </div>
            <div class="pet-eye right" :class="{ blink: isBlinking }">
              <div class="pupil" :style="{ transform: 'translate(' + lookX + 'px,' + lookY + 'px)' }"></div>
            </div>
          </div>
          <div class="pet-mouth" :class="'mouth-' + mood"></div>
          <div v-if="mood === 'blush'" class="pet-blush left"></div>
          <div v-if="mood === 'blush'" class="pet-blush right"></div>
        </div>

        <!-- Arms -->
        <div class="pet-arms">
          <div class="pet-arm left" :class="{ wave: justClicked }"></div>
          <div class="pet-arm right" :class="{ wave: justClicked }"></div>
        </div>
      </div>

      <!-- Speech bubble -->
      <Transition name="bubble">
        <div v-if="speechBubble" class="pet-speech">
          <span>{{ speechBubble.text }}</span>
        </div>
      </Transition>

      <!-- Name tag -->
      <div class="pet-name-tag">{{ pet.name }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useWindowStore } from '../../stores/window'

const props = defineProps<{
  userId: number
  userName: string
}>()

const winStore = useWindowStore()
const stageRef = ref<HTMLElement | null>(null)

// =====================
// Pet Appearance (auto-generated)
// =====================
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
    name: petNames[id % petNames.length],
    level: 3 + (id % 8)
  }
})

// =====================
// State Machine
// =====================
type PetState = 'idle' | 'walking' | 'sleeping' | 'greeting' | 'working'
const state = ref<PetState>('idle')

type Mood = 'idle' | 'happy' | 'blush' | 'sleepy' | 'curious'
const mood = ref<Mood>('idle')

const facing = ref<'face-right' | 'face-left'>('face-right')

// Position (px from left of container)
const posX = ref(200)

// =====================
// Eye tracking
// =====================
const isBlinking = ref(false)
const lookX = ref(0)
const lookY = ref(0)

// =====================
// Click interaction
// =====================
const justClicked = ref(false)
const speechBubble = ref<{ text: string } | null>(null)

const speechesByState: Record<string, string[]> = {
  idle: ['你好呀~', '嘿嘿~', '我在呢！', '要一起玩吗？'],
  walking: ['溜达溜达~', '探索新世界！', '走走更健康~', '这边看看~'],
  sleeping: ['(揉眼睛)...嗯？', '啊...我睡着了？', '做了个好梦~'],
  working: ['加油哦！', '你很棒！', '需要帮忙吗？'],
  greeting: ['你回来啦！', '想你了~', '好久不见！', '呜呜呜好想你~']
}

function handlePetClick() {
  if (state.value === 'sleeping') {
    // Wake up!
    enterState('greeting')
    return
  }

  justClicked.value = true
  mood.value = 'happy'

  const pool = speechesByState[state.value] || speechesByState.idle
  showSpeech(pool[Math.floor(Math.random() * pool.length)])

  setTimeout(() => { justClicked.value = false }, 800)
  setTimeout(() => { mood.value = 'idle' }, 3000)
}

function showSpeech(text: string) {
  speechBubble.value = { text }
  setTimeout(() => { speechBubble.value = null }, 2500)
}

// =====================
// Idle detection
// =====================
let lastActivity = Date.now()
const IDLE_THRESHOLD = 45_000 // 45s → sleep

function markActivity() {
  const wasIdle = Date.now() - lastActivity > IDLE_THRESHOLD
  lastActivity = Date.now()
  if (wasIdle && state.value === 'sleeping') {
    enterState('greeting')
  }
}

// =====================
// Window awareness (working mode)
// =====================
const isChatOpen = computed(() =>
  Object.values(winStore.windows).some(w => w.appId === 'chat' && w.isOpen)
)

watch(isChatOpen, (open) => {
  if (open) {
    enterState('working')
  } else if (state.value === 'working') {
    enterState('idle')
  }
})

// =====================
// State transitions
// =====================
let stateTimer = 0
let walkTimer = 0

function enterState(newState: PetState) {
  clearTimeout(stateTimer)
  clearTimeout(walkTimer)
  state.value = newState

  switch (newState) {
    case 'idle':
      mood.value = 'idle'
      // After 3-8s, decide next action
      stateTimer = window.setTimeout(() => decideAction(), 3000 + Math.random() * 5000)
      break

    case 'walking':
      mood.value = 'idle'
      walkToRandom()
      // Walk for 3-6s then stop
      walkTimer = window.setTimeout(() => enterState('idle'), 3000 + Math.random() * 3000)
      break

    case 'sleeping':
      mood.value = 'sleepy'
      isBlinking.value = true
      // Show sleep speech once
      showSpeech('困了...zzZ')
      break

    case 'greeting':
      mood.value = 'happy'
      justClicked.value = true
      const msgs = speechesByState.greeting
      showSpeech(msgs[Math.floor(Math.random() * msgs.length)])
      setTimeout(() => { justClicked.value = false }, 600)
      stateTimer = window.setTimeout(() => enterState('idle'), 3000)
      break

    case 'working':
      mood.value = 'idle'
      // Move to right corner
      moveToCorner()
      // Occasional encouraging speech
      stateTimer = window.setTimeout(() => {
        if (state.value === 'working') {
          const encouragements = ['加油哦！', '需要帮忙随时叫我~', '你做得很好！', '默默陪伴中...']
          showSpeech(encouragements[Math.floor(Math.random() * encouragements.length)])
          mood.value = 'blush'
          setTimeout(() => { mood.value = 'idle' }, 2000)
          stateTimer = window.setTimeout(() => {
            if (state.value === 'working') enterState('working')
          }, 20000 + Math.random() * 15000)
        }
      }, 8000 + Math.random() * 7000)
      break
  }
}

// Pseudo-AI decision making
function decideAction() {
  if (state.value !== 'idle') return
  if (isChatOpen.value) { enterState('working'); return }

  // Check idle time
  const idleTime = Date.now() - lastActivity
  if (idleTime > IDLE_THRESHOLD) {
    enterState('sleeping')
    return
  }

  // Random action weights
  const rand = Math.random()
  if (rand < 0.4) {
    enterState('walking')       // 40% → walk around
  } else if (rand < 0.55) {
    mood.value = 'curious'      // 15% → look curious
    showSpeech('嗯？')
    stateTimer = window.setTimeout(() => {
      mood.value = 'idle'
      if (state.value === 'idle') decideAction()
    }, 3000)
  } else if (rand < 0.7) {
    mood.value = 'blush'        // 15% → blush
    stateTimer = window.setTimeout(() => {
      mood.value = 'idle'
      if (state.value === 'idle') decideAction()
    }, 4000)
  } else {
    // 30% → stay idle, schedule next
    stateTimer = window.setTimeout(() => decideAction(), 4000 + Math.random() * 4000)
  }
}

// =====================
// Movement
// =====================
function walkToRandom() {
  const stageW = stageRef.value?.parentElement?.clientWidth || 800
  const margin = 100
  const target = margin + Math.random() * (stageW - margin * 2)
  facing.value = target > posX.value ? 'face-right' : 'face-left'
  posX.value = target
}

function moveToCorner() {
  const stageW = stageRef.value?.parentElement?.clientWidth || 800
  facing.value = 'face-right'
  posX.value = stageW - 140
}

// =====================
// Background tick: check idle → sleep
// =====================
let idleCheckTimer = 0
function startIdleCheck() {
  idleCheckTimer = window.setInterval(() => {
    if (state.value === 'idle' && Date.now() - lastActivity > IDLE_THRESHOLD) {
      enterState('sleeping')
    }
  }, 5000)
}

// =====================
// Mouse tracking
// =====================
function handleMouseMove() {
  markActivity()
}

function handleMouseMoveEyes(e: MouseEvent) {
  // Eye tracking (subtle)
  if (state.value === 'sleeping') return
  const rect = stageRef.value?.getBoundingClientRect()
  if (!rect) return
  const cx = rect.left + rect.width / 2
  const cy = rect.top
  const dx = (e.clientX - cx) / window.innerWidth * 6
  const dy = (e.clientY - cy) / window.innerHeight * 4
  lookX.value = Math.max(-4, Math.min(4, dx))
  lookY.value = Math.max(-3, Math.min(3, dy))
}

// =====================
// Blinking
// =====================
let blinkTimer = 0
function startBlinking() {
  const doBlink = () => {
    if (state.value !== 'sleeping') {
      isBlinking.value = true
      setTimeout(() => { isBlinking.value = false }, 150)
    }
    blinkTimer = window.setTimeout(doBlink, 2500 + Math.random() * 3000)
  }
  blinkTimer = window.setTimeout(doBlink, 1500)
}

// =====================
// Lifecycle
// =====================
onMounted(() => {
  window.addEventListener('mousemove', handleMouseMove)
  window.addEventListener('mousemove', handleMouseMoveEyes)
  window.addEventListener('click', markActivity)
  window.addEventListener('keydown', markActivity)

  startBlinking()
  startIdleCheck()

  // Start action loop
  stateTimer = window.setTimeout(() => decideAction(), 3000)
})

onUnmounted(() => {
  window.removeEventListener('mousemove', handleMouseMove)
  window.removeEventListener('mousemove', handleMouseMoveEyes)
  window.removeEventListener('click', markActivity)
  window.removeEventListener('keydown', markActivity)
  clearTimeout(stateTimer)
  clearTimeout(walkTimer)
  clearTimeout(blinkTimer)
  clearInterval(idleCheckTimer)
})
</script>

<style scoped>
/* === Stage (full-width bottom area) === */
.pet-stage {
  position: absolute;
  bottom: 0;
  left: 72px;
  right: 0;
  height: 160px;
  z-index: 12;
  pointer-events: none;
}

/* === Creature container === */
.pet-creature {
  position: absolute;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  pointer-events: auto;
  user-select: none;
  -webkit-user-select: none;
}

/* Walking bounce */
.pet-creature.state-walking .pet-body {
  animation: petWalk 0.5s ease-in-out infinite;
}

@keyframes petWalk {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  25% { transform: translateY(-4px) rotate(-3deg); }
  75% { transform: translateY(-4px) rotate(3deg); }
}

/* Sleeping: gentle breathing */
.pet-creature.state-sleeping .pet-body {
  animation: petBreathe 4s ease-in-out infinite;
}

@keyframes petBreathe {
  0%, 100% { transform: scale(1) translateY(0); }
  50% { transform: scale(1.05) translateY(2px); }
}

/* Greeting: happy bounce */
.pet-creature.state-greeting .pet-body {
  animation: petGreetBounce 0.6s ease;
}

@keyframes petGreetBounce {
  0% { transform: translateY(0) scale(1); }
  30% { transform: translateY(-20px) scale(1.1); }
  50% { transform: translateY(0) scale(0.9); }
  70% { transform: translateY(-10px) scale(1.05); }
  100% { transform: translateY(0) scale(1); }
}

/* Working: subtle pulse */
.pet-creature.state-working .pet-body {
  animation: petWorkPulse 5s ease-in-out infinite;
}

@keyframes petWorkPulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.03); }
}

/* Click bounce override */
.pet-creature.pet-clicked .pet-body {
  animation: petClickBounce 0.4s ease !important;
}

@keyframes petClickBounce {
  0% { transform: scale(1) translateY(0); }
  40% { transform: scale(0.85) translateY(4px); }
  70% { transform: scale(1.15) translateY(-12px); }
  100% { transform: scale(1) translateY(0); }
}

/* Facing direction */
.pet-creature.face-left {
  transform: scaleX(-1);
}

/* === zZZ === */
.pet-zzz {
  position: absolute;
  top: -30px;
  right: -10px;
  display: flex;
  gap: 2px;
}

.z {
  color: rgba(255, 255, 255, 0.4);
  font-weight: 700;
  animation: zFloat 2s ease-in-out infinite;
}

.z1 { font-size: 10px; animation-delay: 0s; }
.z2 { font-size: 13px; animation-delay: 0.4s; }
.z3 { font-size: 16px; animation-delay: 0.8s; }

@keyframes zFloat {
  0%, 100% { transform: translateY(0); opacity: 0.4; }
  50% { transform: translateY(-6px); opacity: 0.15; }
}

/* zzz transition */
.zzz-enter-active { transition: all 0.5s ease; }
.zzz-leave-active { transition: all 0.3s ease; }
.zzz-enter-from { opacity: 0; transform: translateY(10px); }
.zzz-leave-to { opacity: 0; }

/* === Shadow === */
.pet-shadow {
  width: 60px;
  height: 10px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.15);
  filter: blur(4px);
  margin-top: -4px;
  transition: all 0.3s ease;
}

.pet-creature.state-sleeping .pet-shadow {
  width: 70px;
  background: rgba(0, 0, 0, 0.1);
}

.shadow-sleep {
  width: 70px;
}

/* === Body === */
.pet-body {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--pet-color-light), var(--pet-color));
  position: relative;
  animation: petFloat 3s ease-in-out infinite;
  box-shadow:
    0 8px 24px rgba(0, 0, 0, 0.2),
    inset 0 -8px 16px rgba(0, 0, 0, 0.1),
    inset 0 8px 16px rgba(255, 255, 255, 0.15);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.pet-creature:hover .pet-body {
  transform: scale(1.1);
}

@keyframes petFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

/* Override float for state-specific animations */
.pet-creature.state-walking .pet-body,
.pet-creature.state-sleeping .pet-body,
.pet-creature.state-greeting .pet-body,
.pet-creature.state-working .pet-body {
  animation: none;
}

/* === Accessories === */
.pet-antenna {
  position: absolute;
  top: -18px;
  left: 50%;
  transform: translateX(-50%);
}

.antenna-stem {
  width: 3px;
  height: 14px;
  background: var(--pet-color);
  border-radius: 2px;
  margin: 0 auto;
}

.antenna-ball {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--pet-color-light);
  box-shadow: 0 0 8px var(--pet-color);
  margin: -2px auto 0;
  animation: antennaGlow 2s ease-in-out infinite;
}

@keyframes antennaGlow {
  0%, 100% { box-shadow: 0 0 6px var(--pet-color); }
  50% { box-shadow: 0 0 14px var(--pet-color); }
}

.pet-ears {
  position: absolute;
  top: -8px;
  left: 0;
  right: 0;
}

.ear {
  position: absolute;
  width: 20px;
  height: 20px;
  border-radius: 50% 50% 0 0;
  background: var(--pet-color);
}

.ear-left { left: 6px; transform: rotate(-15deg); }
.ear-right { right: 6px; transform: rotate(15deg); }

.pet-horn {
  position: absolute;
  top: -14px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 0;
  border-left: 8px solid transparent;
  border-right: 8px solid transparent;
  border-bottom: 18px solid var(--pet-color-light);
  filter: drop-shadow(0 0 4px var(--pet-color));
}

.pet-bow {
  position: absolute;
  top: -4px;
  right: -2px;
  width: 18px;
  height: 14px;
  background: #FF6B9D;
  border-radius: 50%;
  box-shadow: -8px 0 0 #FF6B9D;
}

.pet-bow::after {
  content: '';
  position: absolute;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #FF453A;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

/* === Face === */
.pet-face {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.pet-eyes {
  display: flex;
  gap: 14px;
}

.pet-eye {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: all 200ms;
}

/* Sleeping: closed eyes */
.pet-creature.state-sleeping .pet-eye {
  height: 3px;
  border-radius: 4px;
}

.pet-eye.blink {
  height: 3px;
  border-radius: 4px;
}

/* Sleepy mood */
.mood-sleepy .pet-eye {
  height: 5px;
  border-radius: 8px;
}

.pupil {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #1a1a2e;
  transition: transform 100ms ease-out;
}

.pupil::after {
  content: '';
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: #fff;
  position: absolute;
  top: 1px;
  right: 1px;
}

/* Sleeping: hide pupils */
.pet-creature.state-sleeping .pupil {
  opacity: 0;
}

/* Mouth */
.pet-mouth {
  width: 8px;
  height: 4px;
  border-radius: 0 0 8px 8px;
  background: rgba(0, 0, 0, 0.2);
  transition: all 200ms;
}

.mouth-idle { width: 6px; height: 3px; }
.mouth-happy { width: 12px; height: 6px; border-radius: 0 0 12px 12px; }
.mouth-blush { width: 8px; height: 2px; border-radius: 4px; }
.mouth-sleepy { width: 10px; height: 10px; border-radius: 50%; background: rgba(0, 0, 0, 0.12); }
.mouth-curious { width: 8px; height: 8px; border-radius: 50%; background: rgba(0, 0, 0, 0.12); }

/* Sleeping mouth */
.pet-creature.state-sleeping .pet-mouth {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.1);
}

/* Blush */
.pet-blush {
  position: absolute;
  width: 10px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 107, 157, 0.4);
  top: 58%;
}

.pet-blush.left { left: 10%; }
.pet-blush.right { right: 10%; }

/* === Arms === */
.pet-arms {
  position: absolute;
  bottom: 8px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: space-between;
}

.pet-arm {
  width: 14px;
  height: 10px;
  border-radius: 8px;
  background: var(--pet-color);
  transition: transform 200ms ease;
}

.pet-arm.left { transform-origin: right center; }
.pet-arm.right { transform-origin: left center; }

.pet-arm.left.wave { animation: waveLeft 0.4s ease 3; }
.pet-arm.right.wave { animation: waveRight 0.4s ease 3; }

@keyframes waveLeft {
  0%, 100% { transform: rotate(0); }
  50% { transform: rotate(-30deg); }
}

@keyframes waveRight {
  0%, 100% { transform: rotate(0); }
  50% { transform: rotate(30deg); }
}

/* === Speech bubble === */
.pet-speech {
  position: absolute;
  bottom: calc(100% + 8px);
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.95);
  color: #1a1a2e;
  font-size: 12px;
  font-weight: 500;
  padding: 6px 14px;
  border-radius: 12px;
  white-space: nowrap;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  pointer-events: none;
}

.pet-speech::after {
  content: '';
  position: absolute;
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
  border-left: 6px solid transparent;
  border-right: 6px solid transparent;
  border-top: 6px solid rgba(255, 255, 255, 0.95);
}

.bubble-enter-active { transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1); }
.bubble-leave-active { transition: all 0.2s ease-in; }
.bubble-enter-from { opacity: 0; transform: translateX(-50%) scale(0.5) translateY(8px); }
.bubble-leave-to { opacity: 0; transform: translateX(-50%) scale(0.8) translateY(-4px); }

/* === Name tag === */
.pet-name-tag {
  font-size: 10px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 4px;
  pointer-events: none;
}
</style>
