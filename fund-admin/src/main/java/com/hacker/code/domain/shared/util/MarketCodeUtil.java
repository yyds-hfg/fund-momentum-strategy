package com.hacker.code.domain.shared.util;

import java.util.Set;

/**
 * 证券代码市场判断工具类。
 * <p>
 * 统一处理 A 股 ETF/LOF/股票/可转债/北交所/指数等代码前缀规则，
 * 供东方财富、新浪、腾讯等不同接口复用。
 */
public final class MarketCodeUtil {

    /**
     * 常见上海证券交易所指数代码（000 开头但属于上证）。
     */
    private static final Set<String> SH_INDEX_CODES = Set.of(
            "000001", "000009", "000010", "000016", "000300", "000688", "000852", "000903", "000905", "000906"
    );

    private MarketCodeUtil() {
    }

    /**
     * 判断代码是否属于上海证券交易所。
     */
    public static boolean isShanghai(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        String c = code.trim();
        if (SH_INDEX_CODES.contains(c)) {
            return true;
        }
        return c.startsWith("60") || c.startsWith("68") || c.startsWith("58") || c.startsWith("56")
                || c.startsWith("50") || c.startsWith("51") || c.startsWith("52")
                || c.startsWith("88") || c.startsWith("89");
    }

    /**
     * 判断代码是否属于深圳证券交易所。
     */
    public static boolean isShenzhen(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        String c = code.trim();
        if (SH_INDEX_CODES.contains(c)) {
            return false;
        }
        return c.startsWith("00") || c.startsWith("30") || c.startsWith("12") || c.startsWith("13")
                || c.startsWith("15") || c.startsWith("16");
    }

    /**
     * 判断代码是否属于北京证券交易所。
     */
    public static boolean isBeijing(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return code.trim().startsWith("83") || code.trim().startsWith("87") || code.trim().startsWith("43");
    }

    /**
     * 获取东方财富接口使用的 secid 格式：市场标识.代码。
     */
    public static String eastMoneySecId(String code) {
        if (isShanghai(code)) {
            return "1." + code.trim();
        }
        return "0." + code.trim();
    }

    /**
     * 获取新浪实时行情接口使用的市场前缀。
     */
    public static String sinaMarket(String code) {
        if (isShanghai(code)) {
            return "sh";
        }
        if (isShenzhen(code) || isBeijing(code)) {
            return "sz";
        }
        return "sh";
    }

    /**
     * 获取腾讯 K 线接口使用的市场前缀。
     */
    public static String tencentMarket(String code) {
        if (isShanghai(code)) {
            return "sh";
        }
        return "sz";
    }

    /**
     * 校验代码是否看起来像 A 股证券代码（6 位数字或可转债/基金的 6 位代码）。
     */
    public static boolean isValidStockLikeCode(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        return code.trim().matches("\\d{6}");
    }
}
