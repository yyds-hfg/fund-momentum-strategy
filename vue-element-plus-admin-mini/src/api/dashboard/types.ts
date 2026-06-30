export interface PositionItem {
  fundCode: string
  fundName: string
  weight: number
  sourceStrategy: string
  reason: string
}

export interface DashboardData {
  tradeDate: string
  marketStatus: string
  totalWeight: number
  recommendedPositions: PositionItem[]
  positions: PositionItem[]
}
