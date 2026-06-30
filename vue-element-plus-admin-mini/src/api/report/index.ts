import request from '@/axios'

export const downloadWeeklyReportApi = (): Promise<Blob> => {
  return request.get({ url: '/report/weekly/download', responseType: 'blob' })
}
