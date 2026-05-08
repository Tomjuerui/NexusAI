<script setup lang='ts'>
import { ref, watch } from 'vue'
import { NAvatar, NButton, NDivider, NFlex, NIcon, NModal, NPopconfirm, NTag, NTooltip } from 'naive-ui'
import { Star24Filled, Star24Regular } from '@vicons/fluent'
import { Bookmarks, VectorBeizer2 } from '@vicons/tabler'
import { useKbStore } from '@/store'
import { knowledgeBaseEmptyInfo } from '@/utils/functions'
import defaultAvatar from '@/assets/avatar.jpg'
import api from '@/api'

interface Props {
  showModal: boolean
  knowledgeBase: KnowledgeBase.Info
}
interface Emit {
  (ev: 'showModal', show: boolean): void
}
const props = withDefaults(defineProps<Props>(), {
  showModal: false,
  knowledgeBase: () => knowledgeBaseEmptyInfo(),
})
const emit = defineEmits<Emit>()
const kbStore = useKbStore()
const innerShow = ref<boolean>(props.showModal)

async function clearHistory(kbInfo: KnowledgeBase.Info) {
  await api.knowledgeBaseQaRecordClear<boolean>()
  kbStore.clearRecords(kbInfo.uuid)
}
async function handleClickStar(kbInfo: KnowledgeBase.Info) {
  const starOrUnstarResp = await api.knowledgeBaseStar<boolean>(kbInfo.uuid)
  const starOrUnstar = starOrUnstarResp.data
  kbStore.insertOrUpdateStarInfo({ kbUuid: kbInfo.uuid, kbTitle: kbInfo.title, star: starOrUnstar })
  kbInfo.starCount = starOrUnstar ? kbInfo.starCount + 1 : kbInfo.starCount - 1
}

watch(() => props.showModal, (val) => {
  innerShow.value = val
})
watch(() => innerShow.value, (val) => {
  if (!val)
    emit('showModal', false)
})
</script>

<template>
  <NModal v-model:show="innerShow" :title="knowledgeBase.title" style="width: 90%; max-width: 640px" preset="card">
    <NFlex vertical>
      <NFlex justify="space-between">
        <NTag size="large" :bordered="false" :color="{ color: '#ff000000' }">
          {{ knowledgeBase.ownerName }}
          <template #avatar>
            <NAvatar
              :src="`/api/user/avatar/${knowledgeBase.ownerUuid}`" size="large" :fallback-src="defaultAvatar"
              color="#ff0000000"
            />
          </template>
        </NTag>
        <NFlex>
          <NTooltip trigger="hover">
            <template #trigger>
              <NTag size="medium" :bordered="false" round :color="{ color: '#ff000000' }">
                {{ knowledgeBase.itemCount }}
                <template #icon>
                  <NIcon :component="Bookmarks" depth="2" />
                </template>
              </NTag>
            </template>
            知识�?          </NTooltip>
          <NTooltip trigger="hover">
            <template #trigger>
              <NTag size="medium" :bordered="false" round :color="{ color: '#ff000000' }">
                {{ knowledgeBase.embeddingCount }}
                <template #icon>
                  <NIcon :component="VectorBeizer2" depth="2" />
                </template>
              </NTag>
            </template>
            向量
          </NTooltip>
          <NTag
            size="medium" :bordered="false" round :color="{ color: '#ff000000' }" checkable
            @click="handleClickStar(knowledgeBase)"
          >
            {{ knowledgeBase.starCount }}
            <template #icon>
              <NIcon v-show="!kbStore.kbUuidToStarInfo.get(knowledgeBase.uuid)?.star" :component="Star24Regular" />
              <NIcon
                v-show="kbStore.kbUuidToStarInfo.get(knowledgeBase.uuid)?.star" :component="Star24Filled"
                color="#eac54f"
              />
            </template>
          </NTag>
        </NFlex>
      </NFlex>
      <NFlex>
        <NTooltip trigger="hover">
          <template #trigger>
            <NTag size="small" :bordered="false">
              {{ knowledgeBase.isPublic ? '公开' : '私有' }}
            </NTag>
          </template>
          公开：所有人可见并使用；<br>
          私有：仅创建者可见并使用�?        </NTooltip>
        <NTooltip trigger="hover">
          <template #trigger>
            <NTag size="small" :bordered="false">
              {{ knowledgeBase.isStrict ? '严格模式' : '宽松模式' }}
            </NTag>
          </template>
          严格模式：严格匹配知识库，知识库中如无搜索结果，直接返回无答案；<br>
          宽松模式：知识库中如无搜索结果，则将用户提问传给LLM继续请求答案�?        </NTooltip>
        <NTooltip trigger="hover">
          <template #trigger>
            <NTag size="small" :bordered="false">
              {{ `最大招回数量：${knowledgeBase.retrieveMaxResults === 0 ? '-' : knowledgeBase.retrieveMaxResults}` }}
            </NTag>
          </template>
          向量搜索时，召回的文档数量不能超过该�?br>
        </NTooltip>
        <NTooltip trigger="hover">
          <template #trigger>
            <NTag size="small" :bordered="false">
              {{ `最小招回分数：${knowledgeBase.retrieveMinScore === 0 ? '-' : knowledgeBase.retrieveMinScore}` }}
            </NTag>
          </template>
          向量搜索时，召回的向量分数需大于该�?        </NTooltip>
      </NFlex>
      <NDivider />
      <div>{{ knowledgeBase.remark }}</div>
    </NFlex>
    <template #footer>
      <NPopconfirm placement="top" @positive-click="clearHistory(knowledgeBase)">
        <template #trigger>
          <NButton size="small" text type="primary">
            清除历史记录
          </NButton>
        </template>
        删除后不可恢复，请谨慎操�?      </NPopconfirm>
    </template>
  </NModal>
</template>
