package com.candlesticks.pattern;

import com.candlesticks.model.CandleProps;

import java.util.List;

/**
 * A pattern match found by the scanner.
 *
 * @param index       position in the original series where the pattern starts
 * @param patternName name of the matched pattern (e.g. "hammer")
 * @param candles     the candles that formed the pattern (1–N candles depending on paramCount)
 * @param metadata    descriptive metadata copied from the pattern definition
 */
public record PatternResult(
        int              index,
        String           patternName,
        List<CandleProps> candles,
        PatternMetadata  metadata
) {
    @Override
    public String toString() {
        return String.format("[idx=%d] %-24s | %-8s %-8s | conf=%.0f%% | %s",
                index,
                patternName,
                metadata.direction(),
                metadata.strength(),
                metadata.confidence() * 100,
                metadata.description());
    }
}
