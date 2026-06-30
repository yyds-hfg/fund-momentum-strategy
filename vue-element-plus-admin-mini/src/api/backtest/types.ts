export interface BacktestResponse {
  id: number
  startDate: string
  endDate: string
  annualReturn: number
  maxDrawdown: number
  sharpeRatio: number
}
