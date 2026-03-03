package com.candlesticks.patterns.multi;

import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.*;
import com.candlesticks.utils.CandleUtils;

/**
 * Bullish Harami — a small bullish candle whose body is contained within a prior large bearish body.
 *
 * <p>Criteria:</p>
 * <ul>
 *   <li>Previous candle is bearish with a substantial body (≥ 50% of its range)</li>
 *   <li>Current candle is bullish</li>
 *   <li>Current body is fully inside previous body (inside bar)</li>
 * </ul>
 *
 * <p>Signals that the prior bearish momentum is stalling. The small inside candle
 * shows indecision after strong selling. Weaker than engulfing — confirmation needed.</p>
 */
public final class BullishHarami {

    private BullishHarami() {}

    static final PatternMetadata METADATA = new PatternMetadata(
            PatternType.REVERSAL,
            PatternDirection.BULLISH,
            0.65,
            PatternStrength.MODERATE,
            "Small bullish body inside prior bearish body — selling momentum stalling");

    public static boolean matches(CandleProps prev, CandleProps curr) {
        return prev.bearish()
                && curr.bullish()
                && prev.bodySize() >= prev.range() * 0.5
                && CandleUtils.isEngulfed(curr, prev);
    }

    public static PatternDefinition definition() {
        return new PatternDefinition("bullishHarami", c -> matches(c[0], c[1]), 2, METADATA);
    }
}
