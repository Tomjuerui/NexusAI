<script setup lang="ts">
import { NInput, NInputNumber, NRadio, NRadioGroup } from 'naive-ui'
import NodePropertyInput from '../NodePropertyInput.vue'
import ReferComment from '../ReferComment.vue'
import ReferTooltip from '../ReferTooltip.vue'

interface Props {
  workflow: Workflow.WorkflowInfo
  wfNode: Workflow.WorkflowNode
}
const props = defineProps<Props>()
const nodeConfig = props.wfNode.nodeConfig as Workflow.NodeConfigMailSend
</script>

<template>
  <div class="flex flex-col w-full">
    <NodePropertyInput :workflow="workflow" :wf-node="wfNode" />
    <div class="mt-6">
      <div class="text-xl mb-1">
        发送人
      </div>
      <div class="text-sm">
        <NRadioGroup v-model:value="nodeConfig.sender_type">
          <NRadio key="sys" :value="1">
            系统
          </NRadio>
          <NRadio key="custom" :value="2">
            自定�?          </NRadio>
        </NRadioGroup>
      </div>
    </div>
    <div v-show="nodeConfig.sender_type === 2">
      <div class="flex flex-col space-y-2 text-sm border border-gray-200 rounded-md p-2 bg-gray-100 mt-2">
        <div>SMTP服务�?/div>
        <NInput v-model:value="nodeConfig.smtp.host" placeholder="eg: smtp.exmail.qq.com" />
        <div>SMTP端口</div>
        <NInputNumber v-model:value="nodeConfig.smtp.port" />
        <div>发送人名称</div>
        <NInput v-model:value="nodeConfig.sender.name" />
        <div>发送人邮箱</div>
        <NInput v-model:value="nodeConfig.sender.mail" />
        <div>发送人密码</div>
        <NInput v-model:value="nodeConfig.sender.password" type="password" show-password-on="mousedown" />
      </div>
    </div>
    <div class="mt-6">
      <div class="text-xl mb-1 flex align-center items-center">
        接收人邮�?ReferTooltip /><span class="text-red-500 text-base">*</span>
      </div>
      <div>
        <NInput v-model:value="nodeConfig.to_mails" placeholder="多个邮箱以逗号隔开" />
      </div>
    </div>
    <div class="mt-6">
      <div class="text-xl mb-1 flex align-center items-center">
        抄送人邮箱<ReferTooltip />
      </div>
      <div>
        <NInput v-model:value="nodeConfig.cc_mails" placeholder="多个邮箱以逗号隔开" />
      </div>
    </div>
    <div class="mt-6">
      <div class="text-xl mb-1 flex align-center items-center">
        邮件标题
        <ReferTooltip :brief="true" />
        <span class="text-red-500 text-base">*</span>
      </div>
      <div class="flex flex-col">
        <ReferComment />
        <NInput v-model:value="nodeConfig.subject" type="textarea" :autosize="{ minRows: 1, maxRows: 3 }" />
      </div>
    </div>
    <div class="mt-6 mb-12">
      <div class="text-xl mb-1 flex align-center items-center">
        邮件内容
        <ReferTooltip :brief="true" />
        <span class="text-red-500 text-base">*</span>
      </div>
      <div class="flex flex-col">
        <ReferComment />
        <NInput v-model:value="nodeConfig.content" type="textarea" :autosize="{ minRows: 3, maxRows: 10 }" />
      </div>
    </div>
  </div>
</template>
