<script setup lang='ts'>
import { ref } from 'vue'
import { NAlert, NCard, NCol, NCollapseTransition, NIcon, NRow, NSpace, NSwitch, NTooltip, NUpload, useMessage } from 'naive-ui'
import type { UploadFileInfo } from 'naive-ui'
import { QuestionCircle16Regular } from '@vicons/fluent'
import SearchInput from '@/views/draw/components/SearchInput.vue'
import { checkProcess } from '@/views/draw/helper'
import { useAppStore, useAuthStore, useDrawStore } from '@/store'
import api from '@/api'
import { emptyDraw } from '@/utils/functions'

interface Emit {
  (e: 'submitted'): void
}
const emit = defineEmits<Emit>()
const ms = useMessage()
const drawStore = useDrawStore()
const authStore = useAuthStore()
const appStore = useAppStore()
const showTip = ref<boolean>(false)
const selectedImageSize = ref<string>('')
const baseImageList = ref<UploadFileInfo[]>()
const baseImage = ref<UploadResult>({ uuid: '', url: '' })
const refImageList = ref<UploadFileInfo[]>()
const refImage = ref<UploadResult>({ uuid: '', url: '' })
const isProd = import.meta.env.PROD
interface UploadResult {
  uuid: string
  url: string
}

async function beforeUpload(data: { file: UploadFileInfo; fileList: UploadFileInfo[] }) {
  if (!authStore.checkLoginOrShow())
    return false
  if (data.file.file?.type !== 'image/png') {
    ms.error('只能上传png格式的图片文件，请重新上�?)
    return false
  }
  return true
}

async function beforeUpload2(data: { file: UploadFileInfo; fileList: UploadFileInfo[] }) {
  if (!authStore.checkLoginOrShow())
    return false
  return true
}

// Upload original image finish
function handleBaseImageFinish({ file, event }: { file: UploadFileInfo; event?: ProgressEvent }) {
  const res = JSON.parse((event?.target as XMLHttpRequest).response)
  if (res.success) {
    baseImage.value = res.data
    baseImageList.value?.push(file)
    console.log(`image url:${baseImage.value}`)
  } else {
    console.log(`handleBaseImageFinish err:${res.data}`)
  }
}

function handleRefImageFinish({ file, event }: { file: UploadFileInfo; event?: ProgressEvent }) {
  const res = JSON.parse((event?.target as XMLHttpRequest).response)
  if (res.success) {
    refImage.value = res.data
    refImageList.value?.push(file)
    console.log(`ref image uuid:${refImage.value}`)
  } else {
    console.log(`handleRefImageFinish err:${res.data}`)
  }
}

// Delete original image
function removeBaseImage({ file }: { file: UploadFileInfo }) {
  if (baseImage.value.uuid)
    api.fileDel(baseImage.value.uuid)
  baseImage.value.url = ''
  baseImage.value.uuid = ''
}

function removeRefImage({ file }: { file: UploadFileInfo }) {
  if (refImage.value.uuid)
    api.fileDel(refImage.value.uuid)
  refImage.value.uuid = ''
  refImage.value.url = ''
}

async function handleSubmit(prompt: string) {
  if (!baseImage.value.url) {
    ms.error('请上传主�?)
    return
  }
  if (!refImage.value.url && !prompt) {
    ms.error('请上传引导图或填写提示词')
    return
  }
  try {
    const dynamicParams = {
      base_image_url: baseImage.value.url,
      ref_image_url: refImage.value.url,
      ref_prompt: prompt,
    }
    const params = {
      interactingMethod: 4,
      modelName: appStore.selectedImageModel.modelName,
      prompt,
      size: selectedImageSize.value,
      number: 1,
      seed: -1,
      dynamicParams,
    }
    const resp = await api.imageGenerate<CreateImageResult>(params)
    const uuid = resp.data.uuid

    const draw = emptyDraw()
    draw.uuid = uuid
    draw.prompt = prompt
    draw.interactingMethod = 4
    draw.aiModelName = appStore.selectedImageModel.modelName
    draw.dynamicParams = dynamicParams
    drawStore.setLoadingUuid(uuid)
    drawStore.pushOne(draw)

    emit('submitted')
    setTimeout(() => {
      checkProcess(uuid)
    }, 3000)
  } catch (error) {
    const e = error as { message: string }
    ms.error(e.message)
  }
}
</script>

<template>
  <div>
    <NAlert v-if="!isProd" title="提示" type="error">
      1. 如果开启了阿里云OSS存储，需要确保图片的访问权限为公共读�?br>
      2. 如果开启了本地存储，需要确保上传的主图及引导图可公网访问，本地开发时本功能不可用�?br>
      3. OSS开启方式：管理后台=》系统配�?》存储位�?br>
      原因：【通义万相-背景生成】模型需要通过公网获取主体图片及引导图片，因此请确保主体图片及引导图片均为公网可访问的图片链接�?    </NAlert>
    <NSpace vertical>
      <NSwitch v-model:value="showTip">
        <template #checked>
          使用说明
        </template>
        <template #unchecked>
          使用说明
        </template>
      </NSwitch>
      <NCollapseTransition :show="showTip">
        <NCard :bordered="true" embedded>
          主体图片：给该图片生成背景的主图，不可为�?br>
          引导图片：给AI参考的图片<br>
          提示词：背景描述�?br>
          引导图片与提示词至少选择一�?        </NCard>
      </NCollapseTransition>
    </NSpace>
    <NRow>
      <NCol :span="2" class="min-w-fit">
        主图<span class="text-red-500">*</span>
        <NTooltip trigger="hover">
          <template #trigger>
            <NIcon style="padding-top: 0.1rem">
              <QuestionCircle16Regular />
            </NIcon>
          </template>
          <span>
            背景透明的图像（带透明背景的RGBA四通道图像�?          </span>
        </NTooltip>
      </NCol>
      <NCol :span="12">
        <NUpload
          :action="`/api/image/upload?token=${authStore.token}`" :max="1" response-type="text"
          list-type="image-card" :default-file-list="baseImageList" @before-upload="beforeUpload"
          @finish="handleBaseImageFinish" @remove="removeBaseImage"
        >
          PNG图片，图像长边不超过2048像素
        </NUpload>
      </NCol>
    </NRow>
    <NRow>
      <NCol :span="2" class="min-w-fit">
        引导�?        <NTooltip trigger="hover">
          <template #trigger>
            <NIcon style="padding-top: 0.1rem">
              <QuestionCircle16Regular />
            </NIcon>
          </template>
          <span>
            图像要求：jpg、png、webp等常见格式�?br>引导图像可以�?RGB 图像或带透明背景�?RGBA 图像。对于RGBA图像，Alpha通道值为0的区域将不参与引导过程的生成，适用于带有主体的引导图�?          </span>
        </NTooltip>
      </NCol>
      <NCol :span="12">
        <NUpload
          :action="`/api/image/upload?token=${authStore.token}`" :max="1" response-type="text"
          list-type="image-card" :default-file-list="refImageList" @before-upload="beforeUpload2"
          @finish="handleRefImageFinish" @remove="removeRefImage"
        >
          jpg、png、webp等常见格�?        </NUpload>
      </NCol>
    </NRow>
    <SearchInput @submit="handleSubmit" />
  </div>
</template>
