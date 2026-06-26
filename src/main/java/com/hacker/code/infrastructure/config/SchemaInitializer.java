package com.hacker.code.infrastructure.config;

import com.hacker.code.domain.fund.repository.MarketOverviewRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SchemaInitializer {

    private final MarketOverviewRepository marketOverviewRepository;

    @PostConstruct
    public void init() {
        try {
            marketOverviewRepository.createTableIfNotExists();
            log.info("市场概况表初始化检查完成");
        } catch (Exception e) {
            log.error("市场概况表初始化失败: {}", e.getMessage(), e);
        }
    }
}
