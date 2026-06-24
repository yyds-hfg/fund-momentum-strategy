package com.hacker.code.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "crawler")
public class CrawlerProperties {

    private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    private int requestTimeout = 30000;

    private int retryCount = 3;

    private int retryDelayMillis = 1000;

    private String eastMoneyKlineUrl = "https://push2his.eastmoney.com/api/qt/stock/kline/get";

    private String eastMoneyUt = "fa5fd1943c7b386f172d6893dbfba10b";

    private String sinaRealtimeUrl = "https://hq.sinajs.cn/list={market}{code}";
}
