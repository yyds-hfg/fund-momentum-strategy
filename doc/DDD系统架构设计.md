# ETF 动量轮动系统 — DDD 分层架构设计

> 版本：v1.0
> 依据：《ETF动量轮动策略 .md》
> 设计目标：将策略手册中的选股、过滤、加权、风控逻辑映射为可扩展、可测试、可回测的软件系统。

---

## 一、总体设计思路

### 1.1 采用 DDD 分层而非传统三层

虽然任务描述为“三层架构”，但 DDD 的分层更利于表达复杂的金融策略领域知识。本系统采用 **DDD 四层架构**：

```
┌─────────────────────────────────────────────────────────────┐
│  接口适配层 (Interfaces / Adapter)                          │
│  REST Controller / 页面 / 定时任务 / 外部行情爬虫适配器       │
├─────────────────────────────────────────────────────────────┤
│  应用层 (Application)                                       │
│  AppService：编排领域服务，无业务逻辑，只负责用例流程         │
├─────────────────────────────────────────────────────────────┤
│  领域层 (Domain)   ★ 核心                                   │
│  聚合根、值对象、领域服务、仓储接口、领域事件                 │
├─────────────────────────────────────────────────────────────┤
│  基础设施层 (Infrastructure)                                │
│  仓储实现、MyBatis Mapper、数据库、外部 API 客户端、配置     │
└─────────────────────────────────────────────────────────────┘
```

**依赖原则**：上层依赖下层；领域层不依赖任何其他层，只通过仓储接口和领域事件与外部交互。

### 1.2 项目包结构（与 Maven 工程结构对应）

```
com.hacker.code
├── adapter                         # 接口适配层
│   ├── web                         #   REST / Thymeleaf 页面
│   │   ├── controller
│   │   └── dto
│   ├── job                         #   定时任务入口（周五收盘后、周一开盘前）
│   └── external                    #   外部系统适配器
│       └── sina                    #     新浪财经行情爬虫
├── application                     # 应用层
│   ├── service                     #   应用服务（用例编排）
│   ├── dto                         #   应用层 DTO / Command / Query
│   ├── event                       #   领域事件监听
│   └── assembler                   #   DTO ↔ Domain 组装器
├── domain                          # 领域层 ★
│   ├── fund                        #   基金上下文
│   │   ├── entity
│   │   ├── valueobject
│   │   ├── repository
│   │   └── service
│   ├── strategy                    #   策略上下文
│   │   ├── entity
│   │   ├── valueobject
│   │   ├── repository
│   │   └── service
│   ├── portfolio                   #   组合/持仓上下文
│   │   ├── entity
│   │   ├── valueobject
│   │   ├── repository
│   │   └── service
│   ├── risk                        #   风控上下文
│   │   ├── valueobject
│   │   └── service
│   └── shared                      #   共享内核
│       ├── vo
│       └── event
└── infrastructure                  # 基础设施层
    ├── repository                  #   仓储实现
    ├── mapper                      #   MyBatis Plus Mapper
    ├── external                    #   外部 API 客户端实现
    ├── config                      #   配置类
    └── persistence                 #   PO / 数据库相关
```

---

## 二、领域层设计（核心）

### 2.1 限界上下文划分

根据策略手册，识别出 4 个核心限界上下文：

| 上下文 | 职责 | 关键领域概念 |
|--------|------|-------------|
| **基金上下文 (Fund)** | ETF 基础信息、净值数据采集与管理 | Fund, NavHistory, MarketData |
| **策略上下文 (Strategy)** | 策略参数、信号生成、策略执行 | StrategyConfig, Momentum, UpQuality, Signal |
| **组合上下文 (Portfolio)** | 持仓建议、权重分配、调仓指令 | Position, Allocation, RebalanceOrder |
| **风控上下文 (Risk)** | 仓位上限、止损、冷静期、大盘过滤 | RiskRule, Drawdown, CoolingPeriod |

### 2.2 聚合根与实体

#### 2.2.1 Fund（基金）

```text
Fund
├── fundCode: String          // 如 510300
├── fundName: String          // 沪深300ETF
├── fundType: FundType        // WIDE_BASE / SECTOR / BOND / GOLD
├── tags: Set<FundTag>        // 用户自定义标签，如 "核心资产"、"科技"、"高波动"
├── listedDate: LocalDate
├── status: FundStatus
└── navHistory: List<Nav>     // 净值历史（可懒加载）
```

#### 2.2.2 StrategyConfig（策略配置）

```text
StrategyConfig
├── strategyId: Long
├── strategyType: StrategyType    // ACTIVE(积极型) / BALANCED(平衡型)
├── shortMomentumWindow: int      // 5 / 10
├── longMomentumWindow: int       // 10 / 20
├── upDaysThreshold: BigDecimal   // 0.50 (50%)
├── maWindow: int                 // 20 / 60
├── volatilityWindow: int         // 10 / 20
├── maxHoldingCount: int          // 2 / 3
├── singleWeightCap: BigDecimal   // 0.50 / 0.40
├── rebalancingFrequency: Frequency // WEEKLY
├── coolingPeriodDays: int        // 3
└── allocationRatio: BigDecimal   // 积极型 0.30，平衡型 0.70
```

#### 2.2.3 StrategyResult（策略计算结果）

```text
StrategyResult
├── resultId: Long
├── tradeDate: LocalDate
├── strategyType: StrategyType
├── marketStatus: MarketStatus    // STRONG / WEAK
├── targetPositions: List<Position>
└── createdAt: LocalDateTime
```

#### 2.2.4 Position（持仓建议）

```text
Position
├── fundCode: String
├── fundName: String
├── weight: BigDecimal
├── sourceStrategy: StrategyType
└── reason: String
```

### 2.3 值对象（Value Object）

| 值对象 | 说明 | 关键属性 |
|--------|------|----------|
| `Nav` | 某日净值 | date, closeNav, openNav, highNav, lowNav, volume |
| `Momentum` | 动量指标 | windowDays, value |
| `UpQuality` | 上涨质量 | upDaysRatio |
| `Volatility` | 波动率 | windowDays, annualizedStd |
| `MovingAverage` | 移动平均线 | windowDays, value |
| `MarketSignal` | 大盘环境信号 | benchmarkCode, maWindow, isBullish |
| `Drawdown` | 回撤 | peakNav（持仓期内历史收盘净值最高点）, currentNav, drawdownRatio |
| `CoolingPeriod` | 冷静期 | startDate, minHoldDays |

### 2.4 领域服务（Domain Service）

领域服务封装跨实体或纯计算逻辑，保持实体精简。

#### 2.4.1 基金数据服务

- `FundDomainService`
  - 校验 ETF 是否属于候选池
  - 获取某只 ETF 某段时间的净值序列

#### 2.4.2 动量与质量计算服务

- `MomentumCalculator`
  - 计算指定窗口的绝对动量
  - `calculate(fundCode, windowDays, endDate): Momentum`

- `UpQualityCalculator`
  - 计算上涨天数占比
  - `calculate(fundCode, windowDays, endDate): UpQuality`

- `VolatilityCalculator`
  - 计算日收益率标准差
  - `calculate(fundCode, windowDays, endDate): Volatility`

- `MovingAverageCalculator`
  - 计算均线
  - `calculate(fundCode, windowDays, endDate): MovingAverage`

#### 2.4.3 筛选与信号服务

- `ETFScreeningService`
  - 输入：候选 ETF 池、交易日期、策略配置
  - 处理：
    1. 绝对动量 > 0
    2. 上涨天数占比 > 配置阈值
    3. 收盘价 > 均线
    4. 按动量排序，取前 N
  - 输出：`List<ScreenedETF>`

- `MarketEnvironmentService`
  - 判断中证 A500 大盘状态
  - `isBullish(benchmarkCode, maWindow, tradeDate): MarketSignal`

#### 2.4.4 权重与组合服务

- `LowVolatilityWeightingService`
  - 按波动率倒数加权
  - 应用单只权重上限
  - 归一化到目标总仓位

- `PortfolioMergeService`
  - 合并积极型与平衡型结果
  - 同一 ETF 取较高权重
  - 不同 ETF 按比例分配

#### 2.4.5 风控服务

- `RiskControlService`
  - 检查单只权重上限
  - 检查整体仓位上限（大盘强势 100%，弱势 50%）
  - 止损判断：单只 ETF 自建议持仓日以来，收盘净值从历史最高点回撤 > 8% 则触发清仓
  - 冷静期检查（新买入未满 3 交易日不换仓，止损例外）

---

## 三、应用层设计

### 3.1 应用服务（Application Service）

应用服务只负责**用例编排**，不实现具体算法。

#### 3.1.1 `StrategyExecutionAppService`

核心用例：每周执行一次策略，生成调仓建议。

```text
executeWeeklyStrategy(tradeDate)
  1. 获取所有策略配置（积极型、平衡型）
  2. 同步/校验基金净值数据
  3. 计算中证 A500 大盘状态
  4. 对每个策略：
       a. 候选池筛选
       b. 低波加权
       c. 风控检查
       d. 生成 StrategyResult
  5. 合并两个策略结果
  6. 生成最终 RebalanceAdvice
  7. 保存结果并发布 StrategyCalculatedEvent
```

#### 3.1.2 `FundDataSyncAppService`

- `syncNavData(fundCodes, startDate, endDate)`
- 从新浪财经抓取 ETF 历史净值并持久化。

#### 3.1.3 `BacktestAppService`

- `runBacktest(startDate, endDate, strategyTypes)`
- 按历史数据逐周执行策略，生成回测结果。

#### 3.1.4 `ReportAppService`

- `generateWeeklyReport(tradeDate)`
- 生成 HTML/PDF 格式的调仓建议报告。

### 3.2 DTO / Command / Query

- `ExecuteStrategyCommand`：执行策略命令
- `SyncNavCommand`：同步净值命令
- `StrategyResultDTO`：策略结果
- `RebalanceAdviceDTO`：调仓建议
- `BacktestRequest` / `BacktestResponse`

### 3.3 领域事件

- `StrategyCalculatedEvent`：策略计算完成
- `RebalanceNeededEvent`：需要调仓
- `StopLossTriggeredEvent`：止损触发

---

## 四、接口适配层设计

### 4.1 Web 接口

| 接口 | 说明 |
|------|------|
| `GET /api/funds` | 候选 ETF 列表（支持按标签筛选） |
| `POST /api/funds` | 新增候选 ETF |
| `PUT /api/funds/{code}` | 编辑 ETF 信息/标签 |
| `DELETE /api/funds/{code}` | 移除候选 ETF |
| `GET /api/funds/tags` | 获取所有标签 |
| `POST /api/funds/tags` | 新建标签 |
| `PUT /api/funds/{code}/tags` | 设置 ETF 标签 |
| `GET /api/funds/{code}/nav` | 某只 ETF 净值历史 |
| `POST /api/strategy/execute` | 手动执行策略 |
| `GET /api/strategy/latest` | 最新策略结果 |
| `POST /api/backtest/run` | 执行回测 |
| `GET /api/report/weekly` | 下载周报 |

### 4.2 定时任务

- `StrategyJob`
  - 每周五收盘后（如 15:35）执行 `StrategyExecutionAppService`
- `NavSyncJob`
  - 每日收盘后同步净值
- `StopLossCheckJob`
  - 盘中或收盘后检查止损条件

### 4.3 外部数据源适配器

- `SinaFinanceNavFetcher`
  - 使用 Jsoup 抓取新浪财经 ETF 净值
  - 接口：`NavDataFetcher`（领域层定义的防腐层接口）

---

## 五、基础设施层设计

### 5.1 数据库表设计

#### 5.1.1 `fund` — 基金基础信息

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| fund_code | VARCHAR(20) | 基金代码，唯一 |
| fund_name | VARCHAR(100) | 基金名称 |
| fund_type | VARCHAR(20) | WIDE_BASE / SECTOR / BOND / GOLD |
| listed_date | DATE | 上市日期 |
| status | TINYINT | 0-禁用 1-启用 |
| create_time | DATETIME | |
| update_time | DATETIME | |

#### 5.1.1a `fund_tag` — ETF 标签定义

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| tag_code | VARCHAR(50) | 标签编码，唯一 |
| tag_name | VARCHAR(100) | 标签名称 |
| color | VARCHAR(20) | 前端展示颜色 |
| create_time | DATETIME | |

#### 5.1.1b `fund_tag_relation` — ETF 与标签多对多关系

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| fund_code | VARCHAR(20) | 基金代码 |
| tag_id | BIGINT | 标签 ID |

唯一索引：`fund_code + tag_id`

#### 5.1.2 `fund_nav` — 基金净值数据

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| fund_code | VARCHAR(20) | 基金代码 |
| nav_date | DATE | 净值日期 |
| close_nav | DECIMAL(10,4) | 收盘净值 |
| open_nav | DECIMAL(10,4) | 开盘净值 |
| high_nav | DECIMAL(10,4) | 最高净值 |
| low_nav | DECIMAL(10,4) | 最低净值 |
| volume | BIGINT | 成交量 |
| create_time | DATETIME | |

唯一索引：`fund_code + nav_date`

#### 5.1.3 `strategy_config` — 策略配置

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| strategy_type | VARCHAR(20) | ACTIVE / BALANCED |
| short_momentum_window | INT | 短期动量窗口 |
| long_momentum_window | INT | 长期动量窗口 |
| up_days_threshold | DECIMAL(5,4) | 上涨天数占比阈值 |
| ma_window | INT | 均线周期 |
| volatility_window | INT | 波动率窗口 |
| max_holding_count | INT | 最大持仓数 |
| single_weight_cap | DECIMAL(5,4) | 单只权重上限 |
| rebalancing_frequency | VARCHAR(20) | WEEKLY / MONTHLY |
| cooling_period_days | INT | 冷静期天数 |
| allocation_ratio | DECIMAL(5,4) | 资金占比 |
| status | TINYINT | 0-禁用 1-启用 |

#### 5.1.4 `strategy_result` — 策略计算结果

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| trade_date | DATE | 交易日期 |
| strategy_type | VARCHAR(20) | 策略类型 |
| market_status | VARCHAR(20) | STRONG / WEAK |
| total_weight | DECIMAL(5,4) | 目标总仓位 |
| create_time | DATETIME | |

#### 5.1.5 `strategy_position` — 策略持仓建议明细

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| result_id | BIGINT | 外键 |
| fund_code | VARCHAR(20) | |
| fund_name | VARCHAR(100) | |
| weight | DECIMAL(5,4) | 权重 |
| source_strategy | VARCHAR(20) | 来源策略 |
| reason | VARCHAR(500) | 入选原因 |

#### 5.1.6 `backtest_record` — 回测记录

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| start_date | DATE | |
| end_date | DATE | |
| strategy_types | VARCHAR(100) | |
| annual_return | DECIMAL(10,4) | 年化收益 |
| max_drawdown | DECIMAL(10,4) | 最大回撤 |
| sharpe_ratio | DECIMAL(10,4) | 夏普比率 |
| detail_json | TEXT | 详细结果 JSON |
| create_time | DATETIME | |

### 5.2 仓储实现

- `FundRepositoryImpl`
- `NavDataRepositoryImpl`
- `StrategyConfigRepositoryImpl`
- `StrategyResultRepositoryImpl`
- `BacktestRecordRepositoryImpl`

### 5.3 外部 API 客户端

- `SinaFinanceNavClient`
  - 负责 HTTP 请求、HTML 解析、异常重试
- `NavDataFetcher` 接口在领域层定义，实现类在基础设施层。

---

## 六、核心算法流程设计

### 6.1 单策略计算流程

```text
输入：strategyConfig, tradeDate, candidatePool, marketData

Step 1: 数据准备
  navMap = 获取每只 ETF [tradeDate - maxWindow, tradeDate] 的净值序列

Step 2: 绝对动量过滤
  for each ETF in candidatePool:
    momentum = (nav_today - nav_Ndays_ago) / nav_Ndays_ago
    if momentum <= 0: 剔除

Step 3: 上涨质量过滤
  for each ETF in remaining:
    upDaysRatio = 窗口内上涨天数 / 总交易日数
    if upDaysRatio <= upDaysThreshold: 剔除

Step 4: 均线过滤
  for each ETF in remaining:
    if closeNav <= movingAverage: 剔除

Step 5: 相对动量排序
  remaining = 按 longMomentum 或 shortMomentum 降序排列
  remaining = 取前 maxHoldingCount 只

Step 6: 低波加权
  for each ETF in remaining:
    inverseVol = 1 / volatility
  weight_i = inverseVol_i / sum(inverseVol)
  应用 singleWeightCap 上限
  归一化

Step 7: 大盘仓位调整
  if 中证A500 > 120日均线:
    totalWeight = 1.0
  else:
    totalWeight = 0.5
  所有 weight *= totalWeight

Step 8: 风控检查
  检查单只上限
  检查冷静期（如适用）
  检查止损：以该 ETF 自进入建议持仓以来的历史最高收盘净值为基准，当前回撤 > 8% 则触发清仓

输出：StrategyResult（含 Position 列表）
```

### 6.2 混用策略合并流程

```text
balancedResult = execute(BALANCED, tradeDate)  // 独立计算
activeResult   = execute(ACTIVE, tradeDate)    // 独立计算

// 两套结果分别保存，不互相覆盖
// 每只 ETF 在各自策略内独立占有权重

finalAdvice = RebalanceAdvice
├── tradeDate
├── marketStatus
├── subResults:
│   ├── BALANCED positions (已按 70% 缩放)
│   └── ACTIVE positions (已按 30% 缩放)
└── totalExposure: sum of all weights

// 同一 ETF 出现在两个策略中时，分别保留各自权重
// 例如：BALANCED 持有 A 30%，ACTIVE 持有 A 20%
// 则最终 A 的总暴露为 50%，展示为两条来源不同的持仓
// 不取高、不合并，各自独立计算和风控

输出：RebalanceAdvice
```

---

## 七、开发阶段规划

### 第一阶段：基础骨架（1-2 天）

1. 完善 DDD 四层包结构。
2. 配置 MyBatis Plus、数据库连接、日志。
3. 创建基础实体、值对象、仓储接口。
4. 初始化候选 ETF 池数据（SQL 脚本）。

### 第二阶段：数据采集（1-2 天）

1. 实现 `SinaFinanceNavFetcher`。
2. 实现 `FundDataSyncAppService`。
3. 实现每日净值同步定时任务。
4. 添加净值缺失校验与告警。

### 第三阶段：策略核心（3-4 天）

1. 实现动量、上涨质量、波动率、均线计算服务。
2. 实现筛选服务 `ETFScreeningService`。
3. 实现低波加权与组合合并服务。
4. 实现风控服务。
5. 实现 `StrategyExecutionAppService` 用例编排。

### 第四阶段：应用与接口（2-3 天）

1. 实现 RESTful API。
2. 实现策略执行、结果查询、手动触发。
3. 实现 Thymeleaf 周报页面。
4. 实现定时任务调度。

### 第五阶段：回测与报告（2-3 天）

1. 实现 `BacktestAppService`。
2. 设计回测绩效指标（年化收益、最大回撤、夏普比率）。
3. 实现周报生成与下载。
4. 补充单元测试与集成测试。

---

## 八、关键技术决策

| 决策项 | 方案 | 理由 |
|--------|------|------|
| 数据精度 | `BigDecimal` | 金融计算避免浮点误差 |
| 日期处理 | `java.time.LocalDate` | Java 8+ 标准日期 API |
| 收益率计算 | 对数收益率 vs 简单收益率 | 波动率用简单日收益率，动量用净值涨幅 |
| 权重归一化 | 截断后二次归一化 | 保证总和为 100% 且不超过单只上限 |
| 数据缺失 | 剔除该 ETF（不参与当日计算） | 避免用插值引入噪声 |
| 数据源 | 新浪财经（Jsoup） | 文档指定，先实现免费数据源 |
| 扩展性 | 策略配置化 | 后续可新增策略类型而不改核心代码 |

---

## 九、边界与约束

1. **当前版本为单机单体应用**，不涉及分布式事务。
2. **行情数据依赖外部免费数据源**，需做限流与容错。
3. **策略执行以收盘后 T+1 执行模型为主**；盘中止损检查可后续扩展。
4. **回测采用周线/日线级别再平衡**，暂不支持分钟级。
5. **不直接对接券商交易接口**，只生成调仓建议。
6. **回测与绩效指标不含交易成本及滑点**，按净值直接计算。

---

## 十、已确认业务规则

| 序号 | 问题 | 确认结论 |
|------|------|----------|
| 1 | 混用策略中同一 ETF 的权重处理方式 | **两套策略独立计算**，同一 ETF 在积极型、平衡型中分别占有权重，不取高、不合并；最终展示各自来源及总暴露 |
| 2 | 止损的“高点”定义 | **自该 ETF 进入建议持仓以来的历史收盘净值最高点**，回撤 > 8% 触发无条件清仓 |
| 3 | 是否维护真实持仓 | V1 **只维护建议持仓**，真实持仓由用户在交易软件中自行执行，后续可扩展对接 |
| 4 | 回测绩效指标口径 | **不含交易成本、滑点**，直接按收盘净值计算收益、回撤与夏普 |
| 5 | 候选 ETF 池维护 | 支持页面动态维护，每只 ETF **可打多个标签**，可按标签筛选 |

---

**下一步**：设计文档已最终确认，开始按“开发阶段规划”逐步实现代码。第一阶段为“基础骨架”，将完成包结构、数据库表、实体与仓储接口。
