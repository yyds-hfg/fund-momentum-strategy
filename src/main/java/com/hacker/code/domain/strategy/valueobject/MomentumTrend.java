package com.hacker.code.domain.strategy.valueobject;


import lombok.Getter;


@Getter
public enum MomentumTrend {

    SHARP_UP("急剧上升"),
    UP("上升"),
    FLAT_UP("平缓上升"),
    FLAT("平缓"),
    FLAT_DOWN("平缓下降"),
    DOWN("下降"),
    SHARP_DOWN("急剧下降");

    private final String label;

    MomentumTrend(String label) {
        this.label = label;
    }

}
