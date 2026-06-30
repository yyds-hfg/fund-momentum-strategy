package com.hacker.code.domain.shared.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A 股交易日工具类。
 * <p>
 * 内置 2024–2026 年中国 A 股主要节假日静态表（每年需按国务院放假通知更新）。
 * 判断逻辑：周末或节假日为非交易日，其余为交易日。
 */
public final class TradeDateUtil {

    private static final LocalTime MARKET_CLOSE_TIME = LocalTime.of(15, 0);

    /**
     * 2024–2026 年 A 股非交易日（含周末调休放假及长假）。
     * 注意：未包含所有周末，周末由 {@link #isWeekend(LocalDate)} 单独判断。
     */
    private static final Set<LocalDate> HOLIDAYS;

    static {
        Set<LocalDate> holidays = new HashSet<>();
        // 2024
        holidays.add(LocalDate.of(2024, 1, 1));
        holidays.add(LocalDate.of(2024, 2, 9));
        holidays.add(LocalDate.of(2024, 2, 12));
        holidays.add(LocalDate.of(2024, 2, 13));
        holidays.add(LocalDate.of(2024, 2, 14));
        holidays.add(LocalDate.of(2024, 2, 15));
        holidays.add(LocalDate.of(2024, 2, 16));
        holidays.add(LocalDate.of(2024, 4, 4));
        holidays.add(LocalDate.of(2024, 5, 1));
        holidays.add(LocalDate.of(2024, 5, 2));
        holidays.add(LocalDate.of(2024, 5, 3));
        holidays.add(LocalDate.of(2024, 6, 10));
        holidays.add(LocalDate.of(2024, 9, 16));
        holidays.add(LocalDate.of(2024, 9, 17));
        holidays.add(LocalDate.of(2024, 10, 1));
        holidays.add(LocalDate.of(2024, 10, 2));
        holidays.add(LocalDate.of(2024, 10, 3));
        holidays.add(LocalDate.of(2024, 10, 4));
        holidays.add(LocalDate.of(2024, 10, 7));

        // 2025
        holidays.add(LocalDate.of(2025, 1, 1));
        holidays.add(LocalDate.of(2025, 1, 28));
        holidays.add(LocalDate.of(2025, 1, 29));
        holidays.add(LocalDate.of(2025, 1, 30));
        holidays.add(LocalDate.of(2025, 1, 31));
        holidays.add(LocalDate.of(2025, 2, 3));
        holidays.add(LocalDate.of(2025, 2, 4));
        holidays.add(LocalDate.of(2025, 4, 4));
        holidays.add(LocalDate.of(2025, 5, 1));
        holidays.add(LocalDate.of(2025, 5, 2));
        holidays.add(LocalDate.of(2025, 5, 5));
        holidays.add(LocalDate.of(2025, 6, 2));
        holidays.add(LocalDate.of(2025, 10, 1));
        holidays.add(LocalDate.of(2025, 10, 2));
        holidays.add(LocalDate.of(2025, 10, 3));
        holidays.add(LocalDate.of(2025, 10, 6));
        holidays.add(LocalDate.of(2025, 10, 7));
        holidays.add(LocalDate.of(2025, 10, 8));

        // 2026（国务院安排未发布，按常规假期预估，每年需更新）
        holidays.add(LocalDate.of(2026, 1, 1));
        holidays.add(LocalDate.of(2026, 2, 17));
        holidays.add(LocalDate.of(2026, 2, 18));
        holidays.add(LocalDate.of(2026, 2, 19));
        holidays.add(LocalDate.of(2026, 2, 20));
        holidays.add(LocalDate.of(2026, 2, 23));
        holidays.add(LocalDate.of(2026, 2, 24));
        holidays.add(LocalDate.of(2026, 2, 25));
        holidays.add(LocalDate.of(2026, 4, 6));
        holidays.add(LocalDate.of(2026, 5, 1));
        holidays.add(LocalDate.of(2026, 5, 4));
        holidays.add(LocalDate.of(2026, 5, 5));
        holidays.add(LocalDate.of(2026, 6, 22));
        holidays.add(LocalDate.of(2026, 9, 25));
        holidays.add(LocalDate.of(2026, 10, 1));
        holidays.add(LocalDate.of(2026, 10, 2));
        holidays.add(LocalDate.of(2026, 10, 5));
        holidays.add(LocalDate.of(2026, 10, 6));
        holidays.add(LocalDate.of(2026, 10, 7));
        holidays.add(LocalDate.of(2026, 10, 8));

        HOLIDAYS = Collections.unmodifiableSet(holidays);
    }

    private TradeDateUtil() {
    }

    /**
     * 根据当前时间确定有效的动量计算日期。
     * <p>
     * 规则：收盘（15:00）之前最新可用数据为上一交易日；收盘及之后为最近一个交易日（当天若为非交易日则顺延）。
     */
    public static LocalDate determineEffectiveTradeDate() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        if (now.isBefore(MARKET_CLOSE_TIME)) {
            return lastTradeDate(today.minusDays(1));
        }
        return lastTradeDate(today);
    }

    /**
     * 判断是否为 A 股交易日。
     */
    public static boolean isTradeDate(LocalDate date) {
        return !isWeekend(date) && !HOLIDAYS.contains(date);
    }

    /**
     * 获取指定日期及之前的最近一个交易日。
     */
    public static LocalDate lastTradeDate(LocalDate date) {
        LocalDate candidate = date;
        while (!isTradeDate(candidate)) {
            candidate = candidate.minusDays(1);
        }
        return candidate;
    }

    /**
     * 获取指定日期及之后的最近一个交易日。
     */
    public static LocalDate nextTradeDate(LocalDate date) {
        LocalDate candidate = date;
        while (!isTradeDate(candidate)) {
            candidate = candidate.plusDays(1);
        }
        return candidate;
    }

    /**
     * 获取一段时间范围内的所有交易日（升序）。
     */
    public static java.util.List<LocalDate> tradeDatesBetween(LocalDate startDate, LocalDate endDate) {
        java.util.List<LocalDate> dates = new java.util.ArrayList<>();
        LocalDate candidate = startDate;
        while (!candidate.isAfter(endDate)) {
            if (isTradeDate(candidate)) {
                dates.add(candidate);
            }
            candidate = candidate.plusDays(1);
        }
        return dates;
    }

    private static boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
}
