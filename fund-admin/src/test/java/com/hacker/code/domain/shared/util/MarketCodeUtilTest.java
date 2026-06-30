package com.hacker.code.domain.shared.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MarketCodeUtilTest {

    @Test
    void shouldIdentifyShanghaiStocks() {
        assertTrue(MarketCodeUtil.isShanghai("600000"));
        assertTrue(MarketCodeUtil.isShanghai("688981"));
        assertTrue(MarketCodeUtil.isShanghai("510300"));
        assertTrue(MarketCodeUtil.isShanghai("588000"));
        // 常见上证指数代码
        assertTrue(MarketCodeUtil.isShanghai("000001"));
        assertTrue(MarketCodeUtil.isShanghai("000300"));
        assertTrue(MarketCodeUtil.isShanghai("000852"));
    }

    @Test
    void shouldIdentifyShenzhenStocks() {
        assertTrue(MarketCodeUtil.isShenzhen("000002")); // 深圳主板股票
        assertTrue(MarketCodeUtil.isShenzhen("300750"));
        assertTrue(MarketCodeUtil.isShenzhen("159915"));
        assertTrue(MarketCodeUtil.isShenzhen("123456"));
    }

    @Test
    void shouldResolveEastMoneySecId() {
        assertEquals("1.600000", MarketCodeUtil.eastMoneySecId("600000"));
        assertEquals("1.510300", MarketCodeUtil.eastMoneySecId("510300"));
        assertEquals("1.000001", MarketCodeUtil.eastMoneySecId("000001")); // 上证指数
        assertEquals("0.300750", MarketCodeUtil.eastMoneySecId("300750"));
        assertEquals("0.000002", MarketCodeUtil.eastMoneySecId("000002")); // 深圳主板
    }

    @Test
    void shouldResolveSinaMarket() {
        assertEquals("sh", MarketCodeUtil.sinaMarket("600000"));
        assertEquals("sh", MarketCodeUtil.sinaMarket("510300"));
        assertEquals("sh", MarketCodeUtil.sinaMarket("000001")); // 上证指数
        assertEquals("sz", MarketCodeUtil.sinaMarket("300750"));
        assertEquals("sz", MarketCodeUtil.sinaMarket("000002"));
    }

    @Test
    void shouldResolveTencentMarket() {
        assertEquals("sh", MarketCodeUtil.tencentMarket("600000"));
        assertEquals("sh", MarketCodeUtil.tencentMarket("000001"));
        assertEquals("sz", MarketCodeUtil.tencentMarket("000002"));
        assertEquals("sz", MarketCodeUtil.tencentMarket("300750"));
    }

    @Test
    void shouldValidateStockCodeFormat() {
        assertTrue(MarketCodeUtil.isValidStockLikeCode("600000"));
        assertFalse(MarketCodeUtil.isValidStockLikeCode("ABC123"));
        assertFalse(MarketCodeUtil.isValidStockLikeCode(null));
    }
}
