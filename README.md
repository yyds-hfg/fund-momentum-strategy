# ETF 动量轮动策略系统

基于 Spring Boot + Vue 3 + Naive UI 的 ETF 动量轮动策略系统。

## 技术栈

- **后端**：Spring Boot 3.2 + Java 17 + MyBatis Plus + MySQL
- **前端**：Vue 3 + Vite + TypeScript + Naive UI + Pinia + ECharts
- **数据**：东方财富、新浪财经、腾讯

## 快速开始

详细启动与部署说明请见：**[doc/启动与部署文档.md](doc/启动与部署文档.md)**

### 后端

```bash
# 1. 创建数据库并初始化表结构
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS momentum_strategy DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p momentum_strategy < src/main/resources/db/migration/V1__init_schema.sql
mysql -u root -p momentum_strategy < src/main/resources/db/migration/V2__init_data.sql

# 2. 配置数据库连接（src/main/resources/application.yml）

# 3. 启动服务
mvn spring-boot:run
```

后端默认地址：http://localhost:8080

### 前端

```bash
cd frontend
npm install
npm run dev
```

前端开发地址：http://localhost:5173

生产构建：

```bash
npm run build
```

构建产物会自动输出到 `src/main/resources/static/`，由 Spring Boot 统一托管。

## 主要页面

- **策略看板**：指标卡、A股成交量、资金流向、推荐持仓、回测记录
- **动量排名**：全部基金动量排名 + 多维度筛选
- **策略执行**：手动执行策略、查看最新结果
- **基金池**：基金 CRUD、净值同步
- **回测记录**：运行回测、查看净值/回撤曲线
- **周报**：在线预览与下载
- **任务调度**：一键跑批、单任务触发

## 访问方式

启动后端后，直接访问 http://localhost:8080 即可进入新版前端。

## 文档

- [启动与部署文档](doc/启动与部署文档.md)
- [DDD 系统架构设计](doc/DDD系统架构设计.md)
- [ETF 动量轮动策略手册](doc/ETF动量轮动策略%20.md)
- [核心逻辑开发文档](doc/核心逻辑开发文档.md)
- [项目核心逻辑与使用文档](doc/项目核心逻辑与使用文档.md)
- [项目需求迭代文档](doc/项目需求迭代文档.md)
