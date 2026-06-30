<script setup lang="ts">
import { ref } from 'vue'
import { ElCard, ElButton } from 'element-plus'
import { useI18n } from '@/hooks/web/useI18n'
import { downloadWeeklyReportApi } from '@/api/report'

const { t } = useI18n()
const loading = ref(false)

const handleDownload = async () => {
  loading.value = true
  try {
    const res = await downloadWeeklyReportApi()
    const blob = new Blob([res], { type: 'text/html' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = 'weekly-report.html'
    link.click()
    URL.revokeObjectURL(url)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="p-20px">
    <ElCard>
      <template #header>
        <span>{{ t('report.weeklyTitle') }}</span>
      </template>
      <ElButton type="primary" :loading="loading" @click="handleDownload">
        {{ t('report.downloadWeekly') }}
      </ElButton>
    </ElCard>
  </div>
</template>
