package com.hacker.code.domain.shared.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TradeDateUtilTest {

    @Test
    void shouldDetectWeekendAsNonTradeDate() {
        assertFalse(TradeDateUtil.isTradeDate(LocalDate.of(2024, 6, 15))); // Saturday
        assertFalse(TradeDateUtil.isTradeDate(LocalDate.of(2024, 6, 16))); // Sunday
    }

    @Test
    void shouldDetectHolidayAsNonTradeDate() {
        assertFalse(TradeDateUtil.isTradeDate(LocalDate.of(2024, 2, 12))); // Spring Festival
        assertFalse(TradeDateUtil.isTradeDate(LocalDate.of(2024, 10, 1))); // National Day
    }

    @Test
    void shouldDetectWorkdayAsTradeDate() {
        assertTrue(TradeDateUtil.isTradeDate(LocalDate.of(2024, 6, 14))); // Friday
        assertTrue(TradeDateUtil.isTradeDate(LocalDate.of(2024, 6, 17))); // Monday
    }

    @Test
    void shouldFindLastTradeDate() {
        // 2024-02-08 周四，02-09 除夕休市，02-10~02-17 春节长假，02-18 周日
        assertEquals(LocalDate.of(2024, 2, 8), TradeDateUtil.lastTradeDate(LocalDate.of(2024, 2, 9)));
        assertEquals(LocalDate.of(2024, 2, 19), TradeDateUtil.lastTradeDate(LocalDate.of(2024, 2, 19)));
    }

    @Test
    void shouldFindNextTradeDate() {
        assertEquals(LocalDate.of(2024, 2, 8), TradeDateUtil.nextTradeDate(LocalDate.of(2024, 2, 8)));
        assertEquals(LocalDate.of(2024, 2, 19), TradeDateUtil.nextTradeDate(LocalDate.of(2024, 2, 9)));
    }

    @Test
    void shouldReturnTradeDatesBetween() {
        List<LocalDate> dates = TradeDateUtil.tradeDatesBetween(LocalDate.of(2024, 6, 14), LocalDate.of(2024, 6, 17));
        assertEquals(2, dates.size());
        assertEquals(LocalDate.of(2024, 6, 14), dates.get(0));
        assertEquals(LocalDate.of(2024, 6, 17), dates.get(1));
    }
}
