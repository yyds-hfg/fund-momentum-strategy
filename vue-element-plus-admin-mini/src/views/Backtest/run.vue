<script setup lang="ts">
import { ref } from 'vue'
import { ElCard, ElForm, ElFormItem, ElDatePicker, ElButton, ElMessage } from 'element-plus'
import { useI18n } from '@/hooks/web/useI18n'
import { runBacktestApi } from '@/api/backtest'
import type { BacktestResponse } from '@/api/backtest/types'

const { t } = useI18n()
const loading = ref(false)
const form = ref({
  startDate: '',
  endDate: ''
})
const result = ref<BacktestResponse>()

const handleRun = async () => {
  if (!form.value.startDate || !form.value.endDate) {
    ElMessage.warning(t('backtest.dateRequired'))
    return
  }
  loading.value = true
  try {
    const res = await runBacktestApi(form.value)
    if (res) {
      result.value = res.data
      ElMessage.success(t('backtest.runSuccess'))
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
        <span>{{ t('backtest.runTitle') }}</span>
      </template>
      <ElForm :model="form" inline>
        <ElFormItem :label="t('backtest.startDate')">
          <ElDatePicker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" />
        </ElFormItem>
        <ElFormItem :label="t('backtest.endDate')">
          <ElDatePicker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" :loading="loading" @click="handleRun">{{ t('backtest.run') }}</ElButton>
        </ElFormItem>
      </ElForm>
      <div v-if="result" class="mt-20px">
        <div><span class="text-gray-500">{{ t('backtest.annualReturn') }}:</span> {{ (result.annualReturn * 100).toFixed(2) }}%</div>
        <div><span class="text-gray-500">{{ t('backtest.maxDrawdown') }}:</span> {{ (result.maxDrawdown * 100).toFixed(2) }}%</div>
        <div><span class="text-gray-500">{{ t('backtest.sharpeRatio') }}:</span> {{ result.sharpeRatio }}</div>
      </div>
    </ElCard>
  </div>
</template>
