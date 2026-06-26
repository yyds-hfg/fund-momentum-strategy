package com.hacker.code.domain.strategy.service;

import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.domain.strategy.entity.StrategyConfig;
import com.hacker.code.domain.strategy.valueobject.MomentumTrend;
import com.hacker.code.domain.strategy.valueobject.MomentumTrendResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MomentumTrendCalculator {

    private final MomentumCalculator momentumCalculator;
    private final UpQualityCalculator upQualityCalculator;

    private static final int EMA_WINDOW = 5;
    private static final int SIGMA_WINDOW = 30;
    private static final BigDecimal SCORE_WEIGHT_LONG = new BigDecimal("0.6");
    private static final BigDecimal SCORE_WEIGHT_SHORT = new BigDecimal("0.3");
    private static final BigDecimal SCORE_WEIGHT_UP_QUALITY = new BigDecimal("0.1");

    public MomentumTrendResult calculate(List<Nav> history, StrategyConfig config) {
        int longWindow = config.getLongMomentumWindow();
        // 至少需要：长窗口 + 20 日斜率 + EMA 预热 + 若干安全边际
        if (history == null || history.size() < longWindow + 25) {
            return null;
        }

        List<BigDecimal> scores = new ArrayList<>();
        for (int i = longWindow; i < history.size(); i++) {
            List<Nav> sub = history.subList(0, i + 1);
            BigDecimal shortMomentum = momentumCalculator.calculate(sub, config.getShortMomentumWindow()).getValue();
            BigDecimal longMomentum = momentumCalculator.calculate(sub, config.getLongMomentumWindow()).getValue();
            BigDecimal upDaysRatio = upQualityCalculator.calculate(sub, config.getLongMomentumWindow()).getUpDaysRatio();
            scores.add(calculateCompositeScore(shortMomentum, longMomentum, upDaysRatio));
        }

        if (scores.size() < 25) {
            return null;
        }

        List<Double> emaScores = ema(scores, EMA_WINDOW);

        int n = emaScores.size();
        BigDecimal slope7 = linearRegressionSlope(emaScores, Math.max(0, n - 7), n);
        BigDecimal slope14 = linearRegressionSlope(emaScores, Math.max(0, n - 14), n);
        BigDecimal slope20 = linearRegressionSlope(emaScores, Math.max(0, n - 20), n);

        double sigma = computeSigma(scores, SIGMA_WINDOW);
        BigDecimal sigmaBd = BigDecimal.valueOf(sigma).setScale(4, RoundingMode.HALF_UP);

        double weightedSlope = 0.5 * slope7.doubleValue()
                + 0.3 * slope14.doubleValue()
                + 0.2 * slope20.doubleValue();

        MomentumTrend trend = classifyTrend(weightedSlope, sigma);
        String description = buildDescription(slope7, slope14, slope20, sigmaBd, trend);

        return new MomentumTrendResult(slope7, slope14, slope20, sigmaBd, trend, description);
    }

    private BigDecimal calculateCompositeScore(BigDecimal shortMomentum,
                                               BigDecimal longMomentum,
                                               BigDecimal upDaysRatio) {
        return longMomentum.multiply(SCORE_WEIGHT_LONG)
                .add(shortMomentum.multiply(SCORE_WEIGHT_SHORT))
                .add(upDaysRatio.multiply(BigDecimal.valueOf(100)).multiply(SCORE_WEIGHT_UP_QUALITY))
                .setScale(4, RoundingMode.HALF_UP);
    }

    private List<Double> ema(List<BigDecimal> values, int window) {
        double alpha = 2.0 / (window + 1);
        List<Double> result = new ArrayList<>();
        result.add(values.get(0).doubleValue());
        for (int i = 1; i < values.size(); i++) {
            result.add(alpha * values.get(i).doubleValue() + (1 - alpha) * result.get(i - 1));
        }
        return result;
    }

    private BigDecimal linearRegressionSlope(List<Double> values, int start, int end) {
        int n = end - start;
        if (n < 2) {
            return BigDecimal.ZERO;
        }
        double sumX = 0;
        double sumY = 0;
        double sumXY = 0;
        double sumX2 = 0;
        for (int i = 0; i < n; i++) {
            double x = i;
            double y = values.get(start + i);
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }
        double slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);
        return BigDecimal.valueOf(slope).setScale(4, RoundingMode.HALF_UP);
    }

    private double computeSigma(List<BigDecimal> scores, int window) {
        int n = Math.min(window, scores.size() - 1);
        if (n < 5) {
            return 0;
        }
        double sum = 0;
        List<Double> diffs = new ArrayList<>();
        for (int i = scores.size() - n; i < scores.size(); i++) {
            double diff = scores.get(i).subtract(scores.get(i - 1)).doubleValue();
            diffs.add(diff);
            sum += diff;
        }
        double mean = sum / n;
        double sumSq = 0;
        for (double diff : diffs) {
            sumSq += (diff - mean) * (diff - mean);
        }
        return Math.sqrt(sumSq / n);
    }

    private MomentumTrend classifyTrend(double weightedSlope, double sigma) {
        double effectiveSigma = sigma < 0.05 ? 1.0 : sigma;
        double sharpUp = 1.5 * effectiveSigma;
        double up = 0.5 * effectiveSigma;
        double flatUp = 0.1 * effectiveSigma;
        double flat = 0.1 * effectiveSigma;
        double flatDown = -0.1 * effectiveSigma;
        double down = -0.5 * effectiveSigma;
        double sharpDown = -1.5 * effectiveSigma;

        if (weightedSlope > sharpUp) {
            return MomentumTrend.SHARP_UP;
        }
        if (weightedSlope > up) {
            return MomentumTrend.UP;
        }
        if (weightedSlope > flatUp) {
            return MomentumTrend.FLAT_UP;
        }
        if (weightedSlope >= flatDown && weightedSlope <= flat) {
            return MomentumTrend.FLAT;
        }
        if (weightedSlope > sharpDown) {
            return MomentumTrend.FLAT_DOWN;
        }
        if (weightedSlope > down) {
            return MomentumTrend.DOWN;
        }
        return MomentumTrend.SHARP_DOWN;
    }

    private String buildDescription(BigDecimal slope7,
                                    BigDecimal slope14,
                                    BigDecimal slope20,
                                    BigDecimal sigma,
                                    MomentumTrend trend) {
        double s7 = slope7.doubleValue();
        double s14 = slope14.doubleValue();
        double s20 = slope20.doubleValue();
        double sig = sigma.doubleValue();

        double z7 = sig > 0 ? s7 / sig : 0;

        StringBuilder sb = new StringBuilder();
        sb.append("趋势").append(trend.getLabel()).append("。");
        sb.append("7日斜率").append(formatSigned(slope7)).append("（").append(formatSigned(z7)).append("σ），");
        sb.append("14日").append(formatSigned(slope14)).append("，");
        sb.append("20日").append(formatSigned(slope20)).append("。");

        if (s7 > s14 && s14 > s20) {
            sb.append("短期动能强于中期，仍在加速。");
        } else if (s7 > s14 && s14 <= s20) {
            sb.append("短期反弹，但中期趋势仍弱。");
        } else if (s7 < s14 && s14 < s20) {
            sb.append("短期跌幅收窄，但中期仍向下。");
        } else if (s7 < s14 && s14 >= s20) {
            sb.append("短期回落，中期向上趋势未变。");
        } else {
            sb.append("各周期方向一致。");
        }

        return sb.toString();
    }

    private String formatSigned(BigDecimal value) {
        return (value.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + value.setScale(2, RoundingMode.HALF_UP);
    }

    private String formatSigned(double value) {
        return (value >= 0 ? "+" : "") + String.format("%.2f", value);
    }
}
