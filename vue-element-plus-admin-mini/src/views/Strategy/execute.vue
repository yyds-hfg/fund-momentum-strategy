<script setup lang="ts">
import { ref } from 'vue'
import { ElCard, ElButton, ElTable, ElTableColumn, ElTag } from 'element-plus'
import { useI18n } from '@/hooks/web/useI18n'
import { executeStrategyApi } from '@/api/strategy'
import type { RebalanceAdvice } from '@/api/strategy/types'

const { t } = useI18n()
const loading = ref(false)
const advice = ref<RebalanceAdvice>()

const handleExecute = async () => {
  loading.value = true
  try {
    const res = await executeStrategyApi()
    if (res) {
      advice.value = res.data
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="p-20px">
    <ElCard>
      <template #header>
        <div class="flex justify-between items-center">
          <span>{{ t('strategy.executeTitle') }}</span>
          <ElButton type="primary" :loading="loading" @click="handleExecute">
            {{ t('strategy.execute') }}
          </ElButton>
        </div>
      </template>
      <div v-if="advice" class="mt-10px">
        <div class="mb-10px">
          <span class="text-gray-500">{{ t('strategy.tradeDate') }}:</span> {{ advice.tradeDate }}
          <span class="ml-20px text-gray-500">{{ t('strategy.marketStatus') }}:</span>
          <ElTag :type="advice.marketStatus === 'STRONG' ? 'success' : 'danger'">
            {{ advice.marketStatus }}
          </ElTag>
        </div>
        <ElTable :data="advice.mergedPositions" border>
          <ElTableColumn prop="fundCode" :label="t('fund.fundCode')" />
          <ElTableColumn prop="fundName" :label="t('fund.fundName')" />
          <ElTableColumn prop="weight" :label="t('fund.weight')">
            <template #default="{ row }">
              {{ (row.weight * 100).toFixed(2) }}%
            </template>
          </ElTableColumn>
          <ElTableColumn prop="sourceStrategy" :label="t('fund.sourceStrategy')" />
          <ElTableColumn prop="reason" :label="t('fund.reason')" show-overflow-tooltip />
        </ElTable>
      </div>
      <div v-else class="text-gray-400 text-center py-40px">
        {{ t('strategy.executeTip') }}
      </div>
    </ElCard>
  </div>
</template>
