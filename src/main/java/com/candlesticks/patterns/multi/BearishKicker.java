package com.candlesticks.patterns.multi;

import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.*;

/**
 * Bearish Kicker — a sharp reversal where a bullish candle is immediately followed by a
 * bearish candle that opens at or below the prior candle's open.
 *
 * <p>Criteria:</p>
 * <ul>
 *   <li>Previous candle is bullish</li>
 *   <li>Current candle is bearish</li>
 *   <li>Current open is at or below the previous open (reversal gap)</li>
 * </ul>
 *
 * <p>One of the most powerful bearish reversal patterns. The downside gap signals a sudden
 * collapse in buying pressure and the start of a potential downtrend.</p>
 */
public final class BearishKicker {

    private BearishKicker() {}

    static final PatternMetadata METADATA = new PatternMetadata(
            PatternType.REVERSAL,
            PatternDirection.BEARISH,
            0.83,
            PatternStrength.STRONG,
            "Gap reversal from bullish to bearish — sudden decisive change in sentiment");

    public static boolean matches(CandleProps prev, CandleProps curr) {
        return prev.bullish()
                && curr.bearish()
                && curr.open() <= prev.open();
    }

    public static PatternDefinition definition() {
        return new PatternDefinition("bearishKicker", c -> matches(c[0], c[1]), 2, METADATA);
    }
}
