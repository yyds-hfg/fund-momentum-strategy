export interface FundItem {
  fundCode: string
  fundName: string
  fundType: string
  description: string
  status: string
}

export interface FundPageResult {
  records: FundItem[]
  total: number
  pageSize: number
  current: number
}
