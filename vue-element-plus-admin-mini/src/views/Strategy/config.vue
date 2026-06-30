<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElCard, ElForm, ElFormItem, ElButton, ElInputNumber, ElMessage } from 'element-plus'
import { useI18n } from '@/hooks/web/useI18n'
import { getStrategyConfigsApi, updateStrategyConfigApi } from '@/api/strategy'
import type { StrategyConfigItem } from '@/api/strategy/types'

const { t } = useI18n()
const loading = ref(false)
const configs = ref<StrategyConfigItem[]>([])

const loadData = async () => {
  loading.value = true
  try {
    const res = await getStrategyConfigsApi()
    if (res) {
      configs.value = res.data
    }
  } finally {
    loading.value = false
  }
}

const saveConfig = async (config: StrategyConfigItem) => {
  await updateStrategyConfigApi(config.id, config)
  ElMessage.success(t('common.saveSuccess'))
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div v-loading="loading" class="p-20px">
    <ElCard v-for="config in configs" :key="config.id" class="mb-20px">
      <template #header>
        <span>{{ config.strategyName }} ({{ config.strategyType }})</span>
      </template>
      <ElForm :model="config" label-width="160px">
        <ElFormItem :label="t('strategy.shortMomentumWindow')">
          <ElInputNumber v-model="config.shortMomentumWindow" :min="1" />
        </ElFormItem>
        <ElFormItem :label="t('strategy.longMomentumWindow')">
          <ElInputNumber v-model="config.longMomentumWindow" :min="1" />
        </ElFormItem>
        <ElFormItem :label="t('strategy.maWindow')">
          <ElInputNumber v-model="config.maWindow" :min="1" />
        </ElFormItem>
        <ElFormItem :label="t('strategy.volatilityWindow')">
          <ElInputNumber v-model="config.volatilityWindow" :min="1" />
        </ElFormItem>
        <ElFormItem :label="t('strategy.maxHoldingCount')">
          <ElInputNumber v-model="config.maxHoldingCount" :min="1" />
        </ElFormItem>
        <ElFormItem :label="t('strategy.singleWeightCap')">
          <ElInputNumber v-model="config.singleWeightCap" :min="0" :max="1" :step="0.05" />
        </ElFormItem>
        <ElFormItem :label="t('strategy.allocationRatio')">
          <ElInputNumber v-model="config.allocationRatio" :min="0" :max="1" :step="0.05" />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="saveConfig(config)">{{ t('common.save') }}</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>
  </div>
</template>
