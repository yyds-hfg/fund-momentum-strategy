<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElCard, ElRow, ElCol, ElTable, ElTableColumn, ElTag } from 'element-plus'
import { useI18n } from '@/hooks/web/useI18n'
import { getDashboardDataApi } from '@/api/dashboard'
import type { DashboardData } from '@/api/dashboard/types'

const { t } = useI18n()
const loading = ref(false)
const dashboardData = ref<DashboardData>()

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDashboardDataApi()
    if (res) {
      dashboardData.value = res.data
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div v-loading="loading" class="p-20px">
    <ElRow :gutter="20">
      <ElCol :span="6">
        <ElCard>
          <div class="text-gray-500 text-sm">{{ t('dashboard.tradeDate') }}</div>
          <div class="text-2xl font-bold mt-2">{{ dashboardData?.tradeDate || '-' }}</div>
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard>
          <div class="text-gray-500 text-sm">{{ t('dashboard.marketStatus') }}</div>
          <div class="text-2xl font-bold mt-2">
            <ElTag :type="dashboardData?.marketStatus === 'STRONG' ? 'success' : 'danger'">
              {{ dashboardData?.marketStatus === 'STRONG' ? t('dashboard.strong') : t('dashboard.weak') }}
            </ElTag>
          </div>
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard>
          <div class="text-gray-500 text-sm">{{ t('dashboard.totalWeight') }}</div>
          <div class="text-2xl font-bold mt-2">{{ dashboardData?.totalWeight != null ? `${(dashboardData.totalWeight * 100).toFixed(2)}%` : '-' }}</div>
        </ElCard>
      </ElCol>
      <ElCol :span="6">
        <ElCard>
          <div class="text-gray-500 text-sm">{{ t('dashboard.positionCount') }}</div>
          <div class="text-2xl font-bold mt-2">{{ dashboardData?.recommendedPositions?.length || 0 }}</div>
        </ElCard>
      </ElCol>
    </ElRow>

    <ElCard class="mt-20px">
      <template #header>
        <span>{{ t('dashboard.recommendedPositions') }}</span>
      </template>
      <ElTable :data="dashboardData?.recommendedPositions || []" border>
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
    </ElCard>
  </div>
</template>
