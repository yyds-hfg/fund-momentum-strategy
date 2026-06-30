package com.hacker.code.domain.risk.service;

import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.domain.portfolio.valueobject.Position;
import com.hacker.code.domain.risk.valueobject.Drawdown;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
public class RiskControlService {

    private static final BigDecimal STOP_LOSS_THRESHOLD = new BigDecimal("0.08");

    /**
     * 判断给定净值序列是否触发止损（持仓以来最高收盘净值回撤 > 8%）。
     */
    public boolean isStopLossTriggered(List<Nav> navHistory, Nav currentNav) {
        if (navHistory == null || navHistory.isEmpty() || currentNav == null) {
            return false;
        }
        BigDecimal peakNav = navHistory.stream()
                .map(Nav::getCloseNav)
                .max(Comparator.naturalOrder())
                .orElse(currentNav.getCloseNav());

        Drawdown drawdown = calculateDrawdown(peakNav, currentNav.getCloseNav());
        return drawdown.exceeds(STOP_LOSS_THRESHOLD);
    }

    public Drawdown calculateDrawdown(BigDecimal peakNav, BigDecimal currentNav) {
        if (peakNav == null || currentNav == null || peakNav.compareTo(BigDecimal.ZERO) == 0) {
            return new Drawdown(peakNav, currentNav, BigDecimal.ZERO);
        }
        BigDecimal ratio = peakNav.subtract(currentNav)
                .divide(peakNav, 6, RoundingMode.HALF_UP);
        return new Drawdown(peakNav, currentNav, ratio);
    }

    public boolean exceedsSingleWeightCap(Position position, BigDecimal cap) {
        return position.getWeight().compareTo(cap) > 0;
    }

    public boolean belowMinWeight(Position position, BigDecimal minWeight) {
        return position.getWeight().compareTo(minWeight) < 0;
    }
}
