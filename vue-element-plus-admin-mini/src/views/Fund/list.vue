<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElCard, ElTable, ElTableColumn, ElButton, ElInput, ElPagination } from 'element-plus'
import { useI18n } from '@/hooks/web/useI18n'
import { getFundPageApi } from '@/api/fund'
import type { FundItem, FundPageResult } from '@/api/fund/types'

const { t } = useI18n()
const loading = ref(false)
const keyword = ref('')
const tableData = ref<FundItem[]>([])
const pageResult = ref<FundPageResult>({
  records: [],
  total: 0,
  pageSize: 10,
  current: 1
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getFundPageApi({
      keyword: keyword.value,
      page: pageResult.value.current,
      size: pageResult.value.pageSize
    })
    if (res) {
      pageResult.value = res.data
      tableData.value = res.data.records
    }
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pageResult.value.current = 1
  loadData()
}

const handlePageChange = (page: number) => {
  pageResult.value.current = page
  loadData()
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="p-20px">
    <ElCard>
      <template #header>
        <div class="flex justify-between items-center">
          <span>{{ t('fund.listTitle') }}</span>
          <div class="flex gap-10px">
            <ElInput v-model="keyword" :placeholder="t('fund.searchPlaceholder')" style="width: 220px" />
            <ElButton type="primary" @click="handleSearch">{{ t('common.search') }}</ElButton>
          </div>
        </div>
      </template>
      <ElTable v-loading="loading" :data="tableData" border>
        <ElTableColumn prop="fundCode" :label="t('fund.fundCode')" />
        <ElTableColumn prop="fundName" :label="t('fund.fundName')" />
        <ElTableColumn prop="fundType" :label="t('fund.fundType')" />
        <ElTableColumn prop="description" :label="t('fund.description')" show-overflow-tooltip />
        <ElTableColumn prop="status" :label="t('common.status')" />
      </ElTable>
      <div class="mt-20px flex justify-end">
        <ElPagination
          v-model:current-page="pageResult.current"
          v-model:page-size="pageResult.pageSize"
          :total="pageResult.total"
          layout="total, prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </ElCard>
  </div>
</template>
