package com.hacker.code.domain.strategy.service;

import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.domain.strategy.valueobject.UpQuality;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class UpQualityCalculator {

    public UpQuality calculate(List<Nav> navHistory, int windowDays) {
        if (navHistory == null || navHistory.size() < windowDays + 1) {
            return new UpQuality(windowDays, BigDecimal.ZERO);
        }
        List<Nav> window = navHistory.subList(navHistory.size() - windowDays - 1, navHistory.size());

        int upDays = 0;
        for (int i = 1; i < window.size(); i++) {
            if (window.get(i).getCloseNav().compareTo(window.get(i - 1).getCloseNav()) > 0) {
                upDays++;
            }
        }
        BigDecimal ratio = BigDecimal.valueOf(upDays)
                .divide(BigDecimal.valueOf(windowDays), 6, RoundingMode.HALF_UP);
        return new UpQuality(windowDays, ratio);
    }
}
