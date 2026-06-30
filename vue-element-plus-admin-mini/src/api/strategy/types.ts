export interface StrategyConfigItem {
  id: number
  strategyType: string
  strategyName: string
  shortMomentumWindow: number
  longMomentumWindow: number
  upDaysThreshold: number
  maWindow: number
  volatilityWindow: number
  maxHoldingCount: number
  singleWeightCap: number
  rebalancingFrequency: string
  coolingPeriodDays: number
  allocationRatio: number
  status: number
}

export interface PositionItem {
  fundCode: string
  fundName: string
  weight: number
  sourceStrategy: string
  reason: string
}

export interface RebalanceAdvice {
  tradeDate: string
  marketStatus: string
  mergedPositions: PositionItem[]
}
