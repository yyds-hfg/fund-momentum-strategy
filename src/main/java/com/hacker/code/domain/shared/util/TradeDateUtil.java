package com.hacker.code.domain.shared.util;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 交易日工具类。
 * <p>
 * A 股收盘时间为 15:00，收盘前最新可用数据为 T-1 日，收盘后最新可用数据为 T 日。
 */
public final class TradeDateUtil {

    private static final LocalTime MARKET_CLOSE_TIME = LocalTime.of(15, 0);

    private TradeDateUtil() {
    }

    /**
     * 根据当前时间确定有效的动量计算日期。
     *
     * @return 15:00 之前返回 T-1 日，15:00 及之后返回 T 日
     */
    public static LocalDate determineEffectiveTradeDate() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        if (now.isBefore(MARKET_CLOSE_TIME)) {
            return today.minusDays(1);
        }
        return today;
    }
}
