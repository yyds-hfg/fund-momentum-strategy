package com.hacker.code.infrastructure.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    @Primary
    public RestTemplate restTemplate(RestTemplateBuilder builder, CrawlerProperties crawlerProperties) {
        return builder
                .setConnectTimeout(Duration.ofMillis(crawlerProperties.getRequestTimeout()))
                .setReadTimeout(Duration.ofMillis(crawlerProperties.getRequestTimeout()))
                .defaultHeader("User-Agent", crawlerProperties.getUserAgent())
                .defaultHeader("Referer", "https://quote.eastmoney.com/")
                .build();
    }

    /**
     * 用于新浪行情接口（GB2312 编码）。
     */
    @Bean
    public RestTemplate sinaRestTemplate(CrawlerProperties crawlerProperties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(crawlerProperties.getRequestTimeout());
        factory.setReadTimeout(crawlerProperties.getRequestTimeout());

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getMessageConverters().stream()
                .filter(converter -> converter instanceof StringHttpMessageConverter)
                .findFirst()
                .ifPresent(converter -> ((StringHttpMessageConverter) converter).setDefaultCharset(Charset.forName("GBK")));
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("User-Agent", crawlerProperties.getUserAgent());
            request.getHeaders().set("Referer", "https://finance.sina.com.cn");
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}
