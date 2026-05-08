<script setup lang='ts'>
import { computed, ref, watch } from 'vue'
import { NDropdown, useMessage } from 'naive-ui'
import AvatarComponent from './Avatar.vue'
import TextComponent from './Text.vue'
import { useAppStore, useAuthStore } from '@/store'
import { SvgIcon } from '@/components/common'
import { useIconRender } from '@/hooks/useIconRender'
import { AUDIO_SYNTHESIZER_SIDE } from '@/utils/constant'
import { t } from '@/locales'
import { useBasicLayout } from '@/hooks/useBasicLayout'
import { emptyAudioPlayState } from '@/utils/functions'
import AudioQueue from '@/utils/AudioQueue'
import api from '@/api'

const props = withDefaults(defineProps<Props>(), {
  audioPlayState: () => emptyAudioPlayState(),
  showAvatar: true,
})
const emit = defineEmits<Emit>()

interface Props {
  conversation: Chat.Conversation
  dateTime?: string
  inversion?: boolean
  showAvatar?: boolean
  error?: boolean
  loading?: boolean
  aiModelPlatform?: string // openai,dashscope,qianfan,ollama,deepseek
  messageUuid: string
  audioPlayState: AudioPlayState
  duration: number // In seconds
}

interface Emit {
  (ev: 'delete'): void
}
const ms = useMessage()
const appStore = useAppStore()
const authStore = useAuthStore()
const { isMobile } = useBasicLayout()
const { iconRender } = useIconRender()
const asRawText = ref(props.inversion)
const token = ref<string>(authStore.token)
let audioQueue: AudioQueue | null = null

let synthesis: SpeechSynthesis | null = null
if ('speechSynthesis' in window)
  synthesis = window.speechSynthesis
else
  console.log('浏览器不支持语音合成')

function playAudioByClick() {
  // 如果系统设置的是浏览器端合成音频，则调用浏览器的api播放文本，不需要检查是否有音频文件
  if (appStore.audioSynthesizerSide === AUDIO_SYNTHESIZER_SIDE.client && props.audioPlayState.text) {
    speekText(props.audioPlayState.text)
    return
  }
  // 查检音频文件是否存在
  const audioPlayState = props.audioPlayState
  if (!audioPlayState.audioUrl) {
    ms.warning('音频文件不可用或已被删除')
    return
  }
  if (!audioPlayState.playing) {
    audioPlayState.playDuration = 0
    audioPlayState.playing = true
    if (!audioPlayState.audio) {
      audioPlayState.audio = new Audio(`/api${audioPlayState.audioUrl}?token=${token.value}`)
      audioPlayState.audio.addEventListener('ended', () => {
        console.log('Audio playback finished')
        audioPlayState.playDuration = 0
        audioPlayState.playing = false
        audioPlayState.audio = null
      }, {
        once: true,
      })
    }
    playDurationCount()
    audioPlayState.audio.play().catch((error: any) => {
      console.error('Audio play error:', error)
      audioPlayState.playDuration = 0
      audioPlayState.playing = false
      audioPlayState.audio = null
      ms.error('播放音频失败，请检查浏览器设置或音频文件是否可�?)
    })
  } else if (audioPlayState.playing && audioPlayState.audio) {
    audioPlayState.audio.pause()
    audioPlayState.playing = false
  } else if (audioPlayState.playing && !audioPlayState.audio) {
    ms.warning('音频文件不可用或已被删除')
  }
}

/**
 * 由浏览器朗读文本
 * @description 该方法使用浏览器的语音合成功能朗读文本，
 *              需要浏览器支持SpeechSynthesis API�? *              如果浏览器不支持，则会输出警告信息�? * @param synthesis 浏览器的语音合成对象
 * @param utterance 语音合成的实例，包含要朗读的文本和其他参�? * @param text 要朗读的文本
 */
function speekText(text: string) {
  if (!synthesis) {
    console.warn('浏览器不支持语音合成')
    return
  }
  const utterance = new SpeechSynthesisUtterance(text)
  utterance.lang = 'zh-CN'
  // 开始播放语�?  synthesis.speak(utterance)
}

/**
 * 播放后端传输过来的音频数�?base64)
 * @param audioFrame PCM数据
 */
async function speekAudioFrame(audioFrame: string) {
  if (!synthesis) {
    console.warn('浏览器不支持语音合成')
    return
  }
  if (!audioFrame)
    console.warn('audioFrame is empty')

  console.log('Received audio frame of length:', audioFrame.length)
  const binaryString = window.atob(audioFrame) // 使用window.atob()进行Base64解码
  const binaryLen = binaryString.length
  const bytes = new Uint8Array(binaryLen)
  for (let i = 0; i < binaryLen; i++) {
    const ascii = binaryString.charCodeAt(i)
    bytes[i] = ascii
  }
  if (!audioQueue)
    audioQueue = new AudioQueue(new (window.AudioContext || (window as any).webkitAudioContext)())

  audioQueue.addChunk(bytes.buffer)
  const ap = props.audioPlayState
  if (!ap.playing)
    ap.playing = true
}

function playDurationCount() {
  const audioPlayState = props.audioPlayState
  if (!audioPlayState.playing)
    return
  if (audioPlayState.playDuration + 1 > props.duration)
    return

  audioPlayState.playDuration = Math.round(audioPlayState.audio.currentTime)
  setTimeout(playDurationCount, 500)
}

const options = computed(() => {
  let txtOrAudio = {
    label: '显示文字',
    key: 'showText',
    icon: iconRender({ icon: 'carbon:text-scale' }),
  }
  if (props.audioPlayState.showText) {
    txtOrAudio = {
      label: '显示音频',
      key: 'showAudio',
      icon: iconRender({ icon: 'lucide:audio-lines' }),
    }
  }
  const common = [
    txtOrAudio,
    {
      label: t('common.delete'),
      key: 'delete',
      icon: iconRender({ icon: 'ri:delete-bin-line' }),
    },
  ]

  return common
})

function handleSelect(key: 'delete' | 'showText' | 'showAudio') {
  const audioPlayState = props.audioPlayState
  switch (key) {
    case 'showText':
      audioPlayState.showText = true
      if (!audioPlayState.text) {
        asRawText.value = false
        api.messageTextByAudio(audioPlayState.audioUuid).then((res) => {
          audioPlayState.text = res.data
        })
      }
      return
    case 'showAudio':
      audioPlayState.showText = false
      return
    case 'delete':
      emit('delete')
  }
}

watch(() => props.audioPlayState.msgPart, (newVal, oldVal) => {
  if (newVal && !props.loading)
    return
  speekText(props.audioPlayState.msgPart)
}, { immediate: true })

watch(() => props.audioPlayState.audioFrame, (audioFrame) => {
  if (!audioFrame || !props.loading)
    return
  speekAudioFrame(audioFrame)
})

watch(() => props.loading, (loading) => {
  const ap = props.audioPlayState
  if (!loading) {
    ap.playing = false
    audioQueue?.complete()
  }
})

// watch(() => props.loading, (loadding) => {
//   if (!loadding && props.audioPlayState.audioUrl && props.conversation.isAutoplayAnswer && !hasAutoPlayed.value)
//     playAudioByClick()

//   hasAutoPlayed.value = false
//   audioContext.close()
// })
</script>

<template>
  <div ref="messageRef" class="flex w-full mb-6 overflow-hidden" :class="[{ 'flex-row-reverse': inversion }]">
    <div
      v-if="showAvatar"
      class="flex items-center justify-center flex-shrink-0 h-8 overflow-hidden rounded-full basis-8"
      :class="[inversion ? 'ml-2' : 'mr-2']"
    >
      <AvatarComponent :name="inversion ? 'user' : aiModelPlatform" />
    </div>
    <div class="overflow-hidden text-sm " :class="[inversion ? 'items-end' : 'items-start']">
      <p class="text-xs text-[#b4bbc4]" :class="[inversion ? 'text-right' : 'text-left']">
        {{ dateTime }}
      </p>
      <div class="flex items-start gap-1 mt-2" :class="[inversion ? 'flex-row-reverse' : 'flex-row']">
        <TextComponent
          v-if="props.audioPlayState.showText" ref="textRef" :inversion="inversion" :error="error"
          :text="props.audioPlayState.text" :loading="loading" :as-raw-text="asRawText"
        />
        <div
          v-else class="bg bg-[#d2f9d1] rounded-md py-1 px-2 mb-2 cursor-pointer flex items-center"
          @click="playAudioByClick"
        >
          <SvgIcon class="text-2xl cursor-pointer custom-hover mr-2" icon="mingcute:voice-2-line" />
          <span v-if="props.audioPlayState.playDuration && duration > 0" class="text-xs pl-2">{{
            props.audioPlayState.playDuration
          }}/{{ duration }}</span>
          <span v-else-if="duration > 0" class="text-xs px-2">{{ duration }}</span>
        </div>
        <!-- 消息框侧边下拉选择列表 -->
        <div class="flex flex-col">
          <NDropdown
            :trigger="isMobile ? 'click' : 'hover'" :placement="!inversion ? 'right' : 'left'"
            :options="options" @select="handleSelect"
          >
            <button class="transition text-neutral-400 hover:text-neutral-800 dark:hover:text-neutral-200">
              <SvgIcon icon="ri:more-2-fill" />
            </button>
          </NDropdown>
        </div>
      </div>
      <slot /><!-- 消息框底部插�?-->
    </div>
  </div>
</template>
