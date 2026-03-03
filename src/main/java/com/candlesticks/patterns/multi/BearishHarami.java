package com.candlesticks.patterns.multi;

import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.*;
import com.candlesticks.utils.CandleUtils;

/**
 * Bearish Harami — a small bearish candle whose body is contained within a prior large bullish body.
 *
 * <p>Criteria:</p>
 * <ul>
 *   <li>Previous candle is bullish with a substantial body (≥ 50% of its range)</li>
 *   <li>Current candle is bearish</li>
 *   <li>Current body is fully inside previous body (inside bar)</li>
 * </ul>
 *
 * <p>Signals that the prior bullish momentum is stalling. Requires confirmation on
 * the next candle to act on this signal.</p>
 */
public final class BearishHarami {

    private BearishHarami() {}

    static final PatternMetadata METADATA = new PatternMetadata(
            PatternType.REVERSAL,
            PatternDirection.BEARISH,
            0.65,
            PatternStrength.MODERATE,
            "Small bearish body inside prior bullish body — buying momentum stalling");

    public static boolean matches(CandleProps prev, CandleProps curr) {
        return prev.bullish()
                && curr.bearish()
                && prev.bodySize() >= prev.range() * 0.5
                && CandleUtils.isEngulfed(curr, prev);
    }

    public static PatternDefinition definition() {
        return new PatternDefinition("bearishHarami", c -> matches(c[0], c[1]), 2, METADATA);
    }
}
