# 策略看板重构设计文档（修订版 v1.1）

> 目标：把“策略看板”从“动量排名展示”转向“宏观市场感知（成交量 + 资金流向）+ ETF 持仓推荐”。
> 原则：**先设计、后实现**；本阶段只产出文档，不写业务代码。

---

## 一、需求澄清（基于用户反馈）

| 原始需求 | 用户反馈 | 最终设计 |
|---------|---------|---------|
| 石油 ECharts 图表 | 打错字，删除 | **不展示石油图表** |
| 最近 A 股成交量 | 用 ECharts 折线图展示最近 **3 个月** | 展示沪深两市近 60 个交易日成交额/成交量折线 |
| 全部动量排名 tab | 保留 | 保留，计算逻辑继续为推荐模块复用 |
| 数据来源 | 东方财富 + 新浪财经，需要存库 | EastMoney 为主，Sina 兜底；市场数据、资金流向均落库 |
| ETF 持仓占比推荐 | 实时计算 | 推荐逻辑**复用现有策略执行链路**，确保与策略执行结果一致；仅在有效日期上按 15:00 规则动态选择 |
| 时间规则 | 所有数据：15:00 前展示 T-1，15:00 后展示 T | 统一使用 `TradeDateUtil.determineEffectiveTradeDate()` |
| 回测 | 暂时不做 | 现有回测记录/曲线保留展示，但本次改造不涉及新增回测功能 |

---

## 二、为什么“推荐权重”要和“策略执行结果”一致？

如果推荐模块另起一套权重算法（例如简单按动量分占比），就会出现：
- 策略执行结果建议 A:30%、B:20%、C:15%……
- 看板推荐却给出 A:40%、B:10%……

这会让使用者困惑，并且推荐失去了策略本身的风控意义（总仓位、低波加权、市场状态过滤）。

因此设计为：
> **推荐模块 = 对指定有效交易日重新跑一次“策略执行计算”，但不持久化结果。**

这样只要有效日期相同，推荐权重和最近一次策略执行结果完全一致。

---

## 三、总体架构

```text
┌──────────────────────────────────────────────────────────────┐
│                    前端 dashboard.html                        │
│  ┌──────────┐ ┌─────────────────────┐ ┌────────────────────┐ │
│  │ 顶部指标卡 │ │ A股成交量趋势(3个月) │ │ 资金流向看板 + 趋势 │ │
│  └──────────┘ └─────────────────────┘ └────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │            ETF 推荐持仓（基于最新动量排名）              │ │
│  └─────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │            回测记录 + 曲线（保留，本次不改）              │ │
│  └─────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
┌───────────────┐   ┌─────────────────┐   ┌───────────────────┐
│ DashboardController │   MarketDataController │   StrategyController    │
└───────────────┘   └─────────────────┘   └───────────────────┘
        │                     │                     │
        ▼                     ▼                     ▼
┌─────────────────┐ ┌─────────────────────┐ ┌─────────────────────┐
│ DashboardAppService│ │ MarketDataAppService   │ │ StrategyExecutionAppService │
└─────────────────┘ └─────────────────────┘ └─────────────────────┘
        │                     │                     │
        ▼                     ▼                     ▼
┌──────────────────────┐ ┌──────────────────────────────┐ ┌─────────────────────┐
│ PortfolioRecommendationService │ │ CompositeMarketDataFetcher   │ │ （已有领域服务）      │
│ （复用策略执行逻辑）  │ │ EastMoney + Sina             │ │                     │
└──────────────────────┘ └──────────────────────────────┘ └─────────────────────┘
        │                     │
        ▼                     ▼
┌─────────────────┐ ┌─────────────────────┐
│ strategy_position │ │ market_overview     │
│ (已有，复用)      │ │ commodity_price (本次删除石油，保留表结构可复用) │
└─────────────────┘ └─────────────────────┘
```

---

## 四、数据模型

### 4.1 新增表：`market_overview`（A 股市场概况 + 资金流向）

```sql
CREATE TABLE market_overview (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trade_date DATE NOT NULL,

    -- 沪深两市成交量/成交额
    sh_volume BIGINT COMMENT '沪市成交量（股）',
    sh_amount DECIMAL(20,4) COMMENT '沪市成交额（元）',
    sz_volume BIGINT COMMENT '深市成交量（股）',
    sz_amount DECIMAL(20,4) COMMENT '深市成交额（元）',
    total_volume BIGINT COMMENT '两市总成交量（股）',
    total_amount DECIMAL(20,4) COMMENT '两市总成交额（元）',

    -- 资金流向（单位：元）
    main_inflow DECIMAL(20,4) COMMENT '主力净流入 = 超大单+大单',
    super_large_inflow DECIMAL(20,4) COMMENT '超大单净流入（映射：机构）',
    large_inflow DECIMAL(20,4) COMMENT '大单净流入（映射：大户）',
    medium_inflow DECIMAL(20,4) COMMENT '中单净流入',
    small_inflow DECIMAL(20,4) COMMENT '小单净流入（映射：散户）',
    north_bound_inflow DECIMAL(20,4) COMMENT '北向资金净流入（nullable，可能无数据）',

    -- 大盘指数收盘（用于展示）
    sh_close DECIMAL(10,4) COMMENT '上证指数收盘',
    sz_close DECIMAL(10,4) COMMENT '深证成指收盘',

    source VARCHAR(50) COMMENT 'eastmoney / sina',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_market_overview_date (trade_date)
);
```

> 注：用户已删除石油图表，因此本次不新增 `commodity_price` 表。若未来需要，可复用同结构。

### 4.2 复用已有表

- `fund_nav`：基金/ETF 日线数据。
- `strategy_result` / `strategy_position`：策略执行结果；推荐模块直接复用计算逻辑。

---

## 五、时间规则（核心）

所有看板数据、推荐持仓、资金流向、成交量统一遵循：

```java
LocalDate effectiveDate = TradeDateUtil.determineEffectiveTradeDate();
// 15:00 前 = T-1，15:00 后 = T
```

| 场景 | 展示数据日期 | 说明 |
|------|-------------|------|
| 工作日 09:30-14:59 | T-1 | 当日收盘数据尚未生成 |
| 工作日 15:00 后 | T | 当日收盘后，等待 16:35 同步任务完成后展示 |
| 非交易日 | T-1（T 为当天） | 同 15:00 前规则 |

> 若 15:00 后数据尚未同步，则前端暂时展示最近有数据的一天（通常是 T-1），避免空白。

---

## 六、外部数据抓取层

### 6.1 设计原则

- **主数据源：东方财富**（接口稳定、字段丰富）。
- **兜底数据源：新浪财经**（用于指数最新快照；资金流向若新浪无合适接口，则仅做部分兜底）。
- 抓取结果统一转换为领域对象后存库。
- 失败时记录 warn，不影响其他模块。

### 6.2 新增接口：`MarketDataFetcher`

```java
public interface MarketDataFetcher {
    // 单日市场概况（含资金流向）
    MarketOverview fetchMarketOverview(LocalDate date);

    // 历史市场概况
    List<MarketOverview> fetchMarketOverviewHistory(LocalDate start, LocalDate end);

    // 当日实时资金流向（盘中用）
    MarketOverview fetchRealtimeCapitalFlow();
}
```

### 6.3 实现：`EastMoneyMarketDataFetcher`

| 数据 | URL / 说明 |
|------|-----------|
| 沪深指数 K 线（成交量/成交额） | `https://push2his.eastmoney.com/api/qt/stock/kline/get?secid=1.000001`（上证）<br>`https://push2his.eastmoney.com/api/qt/stock/kline/get?secid=0.399001`（深证） |
| 资金流向历史 | `https://push2his.eastmoney.com/api/qt/stock/fflow/daykline/get?secid=1.000001&secid2=0.399001&fields2=f51,...,f65` |
| 当日实时资金流向 | `https://push2.eastmoney.com/api/qt/ulist.np/get?secids=1.000001,0.399001&fields=f62,f184,f66,f69,f72,f75,f78,f81,f84,f87` |

字段映射：
- `f62` → `main_inflow`（主力净流入）
- `f66` → `super_large_inflow`（超大单，映射机构）
- `f72` → `large_inflow`（大单，映射大户）
- `f78` → `medium_inflow`（中单）
- `f84` → `small_inflow`（小单，映射散户）
- 北向资金：若接口无数据，保持 `null`。

### 6.4 实现：`SinaMarketDataFetcher`（兜底）

用于指数最新快照：
- `https://hq.sinajs.cn/list=s_sh000001,s_sz399001`
- 返回字段可解析出最新价、成交量、成交额。

资金流向：新浪公开接口不稳定，若无法获取则返回空，由东方财富主数据源覆盖。

### 6.5 组合实现：`CompositeMarketDataFetcher`

```java
public class CompositeMarketDataFetcher implements MarketDataFetcher {
    private final EastMoneyMarketDataFetcher eastMoneyFetcher;
    private final SinaMarketDataFetcher sinaFetcher;

    @Override
    public MarketOverview fetchMarketOverview(LocalDate date) {
        // 1. 先尝试 EastMoney
        // 2. 失败或字段缺失则回退 Sina
    }
}
```

---

## 七、应用服务层

### 7.1 新建 `MarketDataAppService`

```java
@Service
public class MarketDataAppService {

    // 同步最近一个有效交易日的市场数据
    public SyncResult syncLatestMarketData();

    // 补录历史
    public SyncResult syncMarketDataHistory(LocalDate startDate, LocalDate endDate);

    // 获取指定有效日期的市场概况
    public MarketOverviewDTO getMarketOverview(LocalDate tradeDate);

    // 获取成交量趋势（默认 60 个交易日）
    public List<MarketOverviewDTO> getVolumeTrend(LocalDate endDate, int days);

    // 获取资金流向趋势（默认 60 个交易日）
    public List<CapitalFlowDTO> getCapitalFlowTrend(LocalDate endDate, int days);
}
```

### 7.2 新建 `PortfolioRecommendationService`

```java
@Service
public class PortfolioRecommendationService {

    /**
     * 基于指定交易日的最新动量排名，复用策略执行逻辑生成推荐持仓。
     * 不保存结果到 strategy_result/strategy_position。
     */
    public RebalanceAdvice recommend(LocalDate tradeDate) {
        // 1. 调用 StrategyExecutionAppService 中抽取的纯计算逻辑
        // 2. 返回 RebalanceAdvice（含 positions 和 totalWeight）
    }
}
```

具体做法：
1. 将 `StrategyExecutionAppService.executeWeeklyStrategy()` 拆出：
   - `RebalanceAdvice calculateWeeklyStrategy(LocalDate tradeDate)`：纯计算，不保存。
   - `executeWeeklyStrategy(LocalDate tradeDate)`：调用 calculate + 持久化 + 发布事件。
2. `PortfolioRecommendationService.recommend(tradeDate)` 直接调用 `calculateWeeklyStrategy(tradeDate)`。

这样推荐结果与策略执行结果**完全一致**。

### 7.3 改造 `DashboardAppService`

`getDashboardData()` 改为：

```java
public DashboardDTO getDashboardData() {
    LocalDate effectiveDate = TradeDateUtil.determineEffectiveTradeDate();

    DashboardDTO dto = new DashboardDTO();
    dto.setTradeDate(effectiveDate);
    dto.setMarketStatus(resolveMarketStatus(effectiveDate));

    // 成交量趋势（近 60 个交易日）
    dto.setVolumeTrend(marketDataAppService.getVolumeTrend(effectiveDate, 60));

    // 资金流向（最新 + 近 60 日趋势）
    dto.setLatestCapitalFlow(marketDataAppService.getMarketOverview(effectiveDate));
    dto.setCapitalFlowTrend(marketDataAppService.getCapitalFlowTrend(effectiveDate, 60));

    // 推荐持仓（复用策略执行逻辑）
    RebalanceAdvice advice = portfolioRecommendationService.recommend(effectiveDate);
    dto.setRecommendedPositions(advice.getPositions());
    dto.setTotalWeight(advice.getTotalWeight());

    // 回测记录保留
    dto.setBacktestRecords(getBacktestRecords());

    return dto;
}
```

---

## 八、后端接口设计

### 8.1 改造 `GET /dashboard/api/data`

返回新 `DashboardDTO`：

```java
public class DashboardDTO {
    private LocalDate tradeDate;
    private String marketStatus;
    private BigDecimal totalWeight;

    // 成交量
    private List<MarketOverviewDTO> volumeTrend;   // 近 60 个交易日

    // 资金流向
    private MarketOverviewDTO latestCapitalFlow;   // 最新有效日
    private List<CapitalFlowDTO> capitalFlowTrend; // 近 60 个交易日

    // 推荐持仓
    private List<PositionDTO> recommendedPositions;

    // 回测（保留）
    private List<BacktestRecordDTO> backtestRecords;
}
```

### 8.2 独立接口（可选，用于懒加载）

| 接口 | 说明 |
|------|------|
| `GET /dashboard/api/market-overview` | 最新市场概况 |
| `GET /dashboard/api/volume-trend?days=60` | 成交量趋势 |
| `GET /dashboard/api/capital-flow-trend?days=60` | 资金流向趋势 |
| `GET /dashboard/api/recommended-positions` | ETF 推荐持仓 |

### 8.3 同步接口

| 接口 | 说明 |
|------|------|
| `POST /api/market-data/sync` | 手动触发大盘数据/资金流向同步 |

---

## 九、定时任务

### 9.1 新增 `MarketDataSyncJob`

```java
@Component
public class MarketDataSyncJob {

    @Scheduled(cron = "0 35 16 ? * MON-FRI")  // 工作日 16:35
    public void dailySync() {
        marketDataAppService.syncLatestMarketData();
    }

    // 启动时若表为空，补录最近 60 个交易日
    @Override
    public void run(String... args) {
        if (marketDataRepository.isEmpty()) {
            LocalDate end = TradeDateUtil.determineEffectiveTradeDate();
            LocalDate start = end.minusDays(90); // 留足非交易日冗余
            marketDataAppService.syncMarketDataHistory(start, end);
        }
    }
}
```

> 15:00 后用户打开看板时，若 16:35 任务尚未完成，仍展示 T-1 数据，避免空白。

---

## 十、前端页面设计

### 10.1 “策略看板” Tab 新布局

```
┌──────────────────────────────────────────────────────────────┐
│ 最新交易日 │ 大盘状态 │ 当前总仓位 │ 立即执行策略                │
├──────────────────────────────────────────────────────────────┤
│  A 股成交量趋势（近 3 个月 / 60 个交易日）                    │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                    ECharts 折线图                        │ │
│  └─────────────────────────────────────────────────────────┘ │
├──────────────────────────────────────────────────────────────┤
│  资金流向看板                                                 │
│  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐     │
│  │ 主力   │ │ 机构   │ │ 大户   │ │ 散户   │ │ 北向   │     │
│  └────────┘ └────────┘ └────────┘ └────────┘ └────────┘     │
│  资金流向近 60 个交易日趋势折线图                             │
├──────────────────────────────────────────────────────────────┤
│  ETF 推荐持仓（基于最新动量排名，与策略执行逻辑一致）         │
│  表格：基金代码/名称/推荐占比/来源策略/入选原因/标签          │
├──────────────────────────────────────────────────────────────┤
│  回测记录 + 曲线（保留，本次不改）                            │
└──────────────────────────────────────────────────────────────┘
```

### 10.2 删除内容

- 策略看板中的“各策略动量分 TOP10”表格。
- 石油价格图表。
- `DashboardDTO` 中的 `momentumRankGroups`。

### 10.3 保留内容

- “全部动量排名” tab（继续调用 `/dashboard/api/momentum-ranks`）。
- “策略逻辑” tab。
- “基金池管理” / “标签管理”。
- 回测记录展示。

---

## 十一、DTO 设计

### 11.1 `MarketOverviewDTO`

```java
public class MarketOverviewDTO {
    private LocalDate tradeDate;
    private Long totalVolume;
    private BigDecimal totalAmount;
    private BigDecimal mainInflow;
    private BigDecimal superLargeInflow;
    private BigDecimal largeInflow;
    private BigDecimal mediumInflow;
    private BigDecimal smallInflow;
    private BigDecimal northBoundInflow;
    private BigDecimal shClose;
    private BigDecimal szClose;
}
```

### 11.2 `CapitalFlowDTO`

与 `MarketOverviewDTO` 资金流向字段一致，用于趋势接口。

### 11.3 复用 `PositionDTO`

推荐持仓直接使用已有 `PositionDTO`：

```java
public class PositionDTO {
    private String fundCode;
    private String fundName;
    private BigDecimal weight;
    private String sourceStrategy;  // ACTIVE / BALANCED
    private String reason;
}
```

> 因为推荐逻辑复用策略执行，字段完全匹配。

---

## 十二、实施计划（建议）

| 阶段 | 任务 | 涉及模块 | 预估工时 |
|------|------|---------|---------|
| 1 | 新增 `market_overview` 表 + Flyway 脚本 | `db/migration/V3__market_overview.sql` | 0.5d |
| 2 | 抽取策略纯计算逻辑 | `StrategyExecutionAppService.calculateWeeklyStrategy()` | 0.5d |
| 3 | 新建 `PortfolioRecommendationService` | `domain/portfolio/service/...` | 0.5d |
| 4 | 新建 `MarketDataFetcher` 接口及 EastMoney 实现 | `domain/fund/service/...`<br>`infrastructure/external/eastmoney/EastMoneyMarketDataFetcher.java` | 1.5d |
| 5 | 新建 Sina 兜底实现 | `infrastructure/external/sina/SinaMarketDataFetcher.java` | 0.5d |
| 6 | 新建 `MarketDataAppService` + Repository/Mapper | `application/service/MarketDataAppService.java`<br>`domain/fund/repository/MarketOverviewRepository.java` 等 | 1d |
| 7 | 改造 `DashboardDTO`、`DashboardAppService`、`DashboardController` | `application/dto/DashboardDTO.java`<br>`application/service/DashboardAppService.java`<br>`adapter/web/controller/DashboardController.java` | 1d |
| 8 | 前端看板重构（删除排名表、新增成交量/资金流向/推荐持仓图表） | `templates/dashboard.html` | 1.5d |
| 9 | 新增 `MarketDataSyncJob` + 启动补录 | `adapter/job/MarketDataSyncJob.java` | 0.5d |
| 10 | 单元/集成测试 + 联调 | `src/test/java/...` | 1d |

**合计：约 8.5 人日。**

---

## 十三、风险点

1. **外部接口稳定性**：东方财富/新浪接口可能限流、字段变更，需要防腐层、失败降级和日志。
2. **北向资金数据缺失**：2024-08-16 起不再实时披露北向净买入，可能只能展示成交额或 T-1 港交所数据。
3. **15:00 后数据未同步**：16:35 任务完成前，15:00-16:35 之间展示的是 T-1 数据，需要前端提示。
4. **推荐与策略结果一致性**：必须确保推荐模块复用策略执行逻辑，否则会出现两套权重。
5. **非交易日处理**：周末/节假日使用 T-1 规则，数据库中需有最近交易日数据。

---

## 十四、待最终确认事项

1. **成交量趋势展示字段**：默认展示“成交额（亿元）”折线，是否同时叠加“成交量（亿股）”双 Y 轴？
2. **资金流向分类命名**：按“主力/机构/大户/散户/北向”命名是否可接受？
3. **推荐持仓数量上限**：当前策略配置中已控制持仓数量，是否需要在看板额外限制展示条数？
4. **Sina 兜底范围**：若新浪无法提供资金流向历史，是否接受仅东方财富单数据源？

---

*文档版本：v1.1（修订版）*
*下一步：确认以上设计后即可进入编码阶段。*
