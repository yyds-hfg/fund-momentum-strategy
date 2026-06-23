package com.hacker.code.infrastructure.external.eastmoney;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.infrastructure.config.CrawlerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EastMoneyKlineNavFetcherTest {

    @Mock
    private CrawlerProperties crawlerProperties;

    @Mock
    private RestTemplate restTemplate;

    private EastMoneyKlineNavFetcher fetcher;

    @BeforeEach
    void setUp() {
        fetcher = new EastMoneyKlineNavFetcher(crawlerProperties, restTemplate, new ObjectMapper());
    }

    @Test
    void shouldParseKlineResponse() {
        when(crawlerProperties.getEastMoneyKlineUrl()).thenReturn("https://push2his.eastmoney.com/api/qt/stock/kline/get");

        String response = "{\"rc\":0,\"data\":{\"klines\":[\"2024-01-02,3.502,3.453,3.502,3.451,9429306\",\"2024-01-03,3.446,3.443,3.460,3.428,10617503\"]}}";
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(response);

        List<Nav> navs = fetcher.fetchHistory("510300", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 3));

        assertNotNull(navs);
        assertEquals(2, navs.size());
        assertEquals(LocalDate.of(2024, 1, 2), navs.get(0).getDate());
        assertEquals(0, navs.get(0).getCloseNav().compareTo(java.math.BigDecimal.valueOf(3.453)));
        assertEquals(0, navs.get(1).getCloseNav().compareTo(java.math.BigDecimal.valueOf(3.443)));
    }

    @Test
    void shouldReturnEmptyOnEmptyResponse() {
        when(crawlerProperties.getEastMoneyKlineUrl()).thenReturn("https://push2his.eastmoney.com/api/qt/stock/kline/get");
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("{\"rc\":0,\"data\":{}}");

        List<Nav> navs = fetcher.fetchHistory("510300", LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 3));

        assertTrue(navs.isEmpty());
    }
}
