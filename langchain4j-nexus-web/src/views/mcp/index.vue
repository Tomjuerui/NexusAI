<script setup lang='ts'>
import { ref, watch } from 'vue'
import { NAlert, NButton, NIcon, NInput, NModal, NRadio, NRadioGroup, NTabPane, NTable, NTabs, NTooltip, useLoadingBar, useMessage } from 'naive-ui'
import { QuestionCircle16Regular } from '@vicons/fluent'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import mdKatex from '@traptitech/markdown-it-katex'
import mila from 'markdown-it-link-attributes'
import McpInfoList from './McpInfoList.vue'
import UserMcpList from './UserMcpList.vue'
import { t } from '@/locales'
import { useAuthStore, useMcpStore } from '@/store'
import { emptyMcp, emptyUserMcp } from '@/utils/functions'
import api from '@/api'

const authStore = useAuthStore()
const ms = useMessage()
const mcpStore = useMcpStore()
const showConfigModal = ref<boolean>(false)
const selectedMcp = ref<Mcp.McpInfo>(emptyMcp())
const selectedUserMcp = ref<Mcp.UserMcp>(emptyUserMcp())
const publicOrUser = ref<string>('serversView')
const loaddingBar = useLoadingBar()
const selectedTab = ref<string>('configTab')

const mdi = new MarkdownIt({
  linkify: true,
  highlight(code, language) {
    const validLang = !!(language && hljs.getLanguage(language))
    if (validLang) {
      const lang = language ?? ''
      return highlightBlock(hljs.highlight(code, { language: lang }).value, lang)
    }
    return highlightBlock(hljs.highlightAuto(code).value, '')
  },
})

mdi.use(mila, { attrs: { target: '_blank', rel: 'noopener' } })
mdi.use(mdKatex, { blockClass: 'katexmath-block rounded-md p-[10px]', errorColor: ' #cc0000' })

function highlightBlock(str: string, lang?: string) {
  return `<pre class="code-block-wrapper"><div class="code-block-header"><span class="code-block-header__lang">${lang}</span><span class="code-block-header__copy">${t('chat.copyCode')}</span></div><code class="hljs code-block-body ${lang}">${str}</code></pre>`
}

function onShowInfoModal(mcpInfo: Mcp.McpInfo) {
  selectedMcp.value = mcpInfo
  selectedTab.value = 'introTab'
  showConfigModal.value = true
}

function onShowConfigModal(mcpInfo: Mcp.McpInfo) {
  if (!authStore.checkLoginOrShow())
    return
  const userMcp = mcpStore.myUserMcpList.find(mcp => mcp.mcpId === mcpInfo.id)
  if (userMcp)
    selectedUserMcp.value = userMcp
  else
    selectedUserMcp.value = emptyUserMcp()
  selectedTab.value = 'configTab'
  selectedMcp.value = mcpInfo
  showConfigModal.value = true
}

async function onSaveConfig() {
  if (!authStore.checkLoginOrShow())
    return
  const customizedParams = selectedMcp.value.customizedParamDefinitions.map(param => ({
    name: param.name,
    value: param.value,
    enctrypted: param.enctrypted,
  }))
  const params = {
    mcpId: selectedMcp.value.id,
    mcpCustomizedParams: customizedParams,
    isEnable: selectedUserMcp.value.isEnable,
  }
  try {
    await api.userMcpSaveOrUpdate(params)
    await loadMyUserMcpList(false)
    ms.success('保存配置成功', {
      duration: 3000,
    })
  } catch (error) {
    console.error(error)
    ms.error('保存配置失败，请稍后重试')
  } finally {
    showConfigModal.value = false
  }
}

async function loadMyUserMcpList(showLoaddingBar = true) {
  if (mcpStore.userMcpLoading || !authStore.token || mcpStore.myUserMcpList.length > 0)
    return
  if (showLoaddingBar)
    loaddingBar.start()
  try {
    mcpStore.setUserMcpLoading(true)
    const { data } = await api.userMcpList<Mcp.UserMcpListResp>(1, 200)
    if (data.records.length > 0)
      mcpStore.appendMyUserMcpList(data.records)
  } catch (error) {
    console.error(error)
  } finally {
    mcpStore.setUserMcpLoading(false)
    if (showLoaddingBar)
      loaddingBar.finish()
  }
}

watch(
  () => authStore.token,
  () => {
    if (authStore.token)
      loadMyUserMcpList()
  },
  { immediate: true },
)
</script>

<template>
  <div class="flex flex-col w-full h-full">
    <header class="left-0 top-0 z-30 border-b dark:border-neutral-800 bg-white/80 dark:bg-black/20 backdrop-blur">
      <div class="relative flex items-center justify-between max-w-screen-xl px-4 m-auto h-10">
        <div class="flex items-center flex-col mx-2">
          <NRadioGroup v-model:value="publicOrUser" name="displayStyleRadioGroup" size="small">
            <NRadio value="serversView">
              服务与工�?            </NRadio>
            <NRadio value="userView">
              我的工具
            </NRadio>
          </NRadioGroup>
        </div>
      </div>
    </header>

    <main class="flex-1 overflow-y-auto h-full w-full max-w-screen-xl m-auto">
      <McpInfoList
        v-show="publicOrUser === 'serversView'" @show-info-modal="onShowInfoModal"
        @show-config-modal="onShowConfigModal"
      />
      <UserMcpList
        v-show="publicOrUser === 'userView'" @show-info-modal="onShowInfoModal"
        @show-config-modal="onShowConfigModal"
      />
    </main>

    <NModal v-model:show="showConfigModal" style="width: 90%; max-width: 1000px;" preset="card">
      <template #header>
        <h2 class="text-xl font-bold">
          {{ selectedMcp.title }}-<span v-if="selectedTab === 'configTab'">配置</span><span
            v-if="selectedTab === 'introTab'"
          >介绍</span>
        </h2>
      </template>
      <NTabs type="line" justify-content="space-evenly" :value="selectedTab" @update:value="val => selectedTab = val">
        <NTabPane name="introTab" tab="介绍">
          <div class="flex flex-col space-y-2 max-h-[720px] overflow-y-auto p-2">
            <div>
              <div class="w-full markdown-body" v-html="mdi.render(selectedMcp.remark)" />
            </div>
            <NAlert v-if="selectedMcp.website" :show-icon="false" type="info">
              相关网址：{{ selectedMcp.website }}
            </NAlert>
          </div>
        </NTabPane>
        <NTabPane name="configTab" tab="配置">
          <div class="flex flex-col space-y-1 max-h-[720px] overflow-y-auto">
            <NAlert v-if="selectedMcp.customizedParamDefinitions.length === 0" :show-icon="false" type="info">
              该服务无需配置参数即可使用�?            </NAlert>
            <div v-if="selectedMcp.customizedParamDefinitions.length > 0" class="flex flex-col space-y-2">
              <div class="font-bold text-base">
                服务参数<span class="text-sm text-gray-500">（请参考[介绍]页签进行配置�?/span>
              </div>
              <NTable :bordered="false" :single-line="false">
                <thead>
                  <tr>
                    <th>参数</th>
                    <th>�?/th>
                    <th class="flex justify-center">
                      敏感信息
                      <NTooltip trigger="hover">
                        <template #trigger>
                          <NIcon style="padding-top: 0.1rem">
                            <QuestionCircle16Regular />
                          </NIcon>
                        </template>
                        <span>敏感信息会进行加密存�?/span>
                      </NTooltip>
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="uninitParam in selectedMcp.customizedParamDefinitions" :key="uninitParam.name">
                    <td class="max-w-[200px]">
                      {{ uninitParam.title }}
                    </td>
                    <td>
                      <NInput
                        v-model:value="uninitParam.value" class="flex-1"
                        :placeholder="`请输入变量�?{uninitParam.name}`"
                      />
                    </td>
                    <td class="flex justify-center">
                      <span v-if="uninitParam.require_encrypt">�?/span>
                      <span v-if="!uninitParam.require_encrypt">�?/span>
                    </td>
                  </tr>
                </tbody>
              </NTable>
            </div>
            <div class="pt-4 flex flex-col space-y-2">
              <div class="font-bold text-base">
                状�?              </div>
              <NRadioGroup v-model:value="selectedUserMcp.isEnable" name="enableGroup">
                <NRadio :value="true">
                  启用
                </NRadio>
                <NRadio :value="false">
                  暂存
                </NRadio>
              </NRadioGroup>
            </div>
            <div class="flex justify-end p-2">
              <NButton type="primary" @click="onSaveConfig">
                确定
              </NButton>
            </div>
          </div>
        </NTabPane>
      </NTabs>
    </NModal>
  </div>
</template>

<style lang="less" scoped>
:deep(.markdown-body ul) {
  list-style-type: inherit;
}

:deep(.markdown-body ol) {
  list-style-type: inherit;
}
</style>
