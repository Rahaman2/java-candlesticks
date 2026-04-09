package com.candlesticks.api.dto;

import com.candlesticks.pattern.PatternResult;

/** REST representation of a detected pattern. */
public record PatternResultDto(
        int    index,
        String name,
        String type,
        String direction,
        String strength,
        double confidence
) {
    public static PatternResultDto from(PatternResult r) {
        return new PatternResultDto(
                r.index(),
                r.patternName(),
                r.metadata().type().name(),
                r.metadata().direction().name(),
                r.metadata().strength().name(),
                r.metadata().confidence());
    }
}
