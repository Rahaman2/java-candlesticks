package com.candlesticks.patterns.multi;

import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.*;

/**
 * Bullish Kicker — a sharp reversal where a bearish candle is immediately followed by a
 * bullish candle that opens at or above the prior candle's open.
 *
 * <p>Criteria:</p>
 * <ul>
 *   <li>Previous candle is bearish</li>
 *   <li>Current candle is bullish</li>
 *   <li>Current open is at or above the previous open (reversal gap — price "kicks" the opposite way)</li>
 * </ul>
 *
 * <p>One of the most powerful reversal patterns. The gap in the opposite direction of the
 * prior candle signals a sudden, decisive change in market sentiment.</p>
 */
public final class BullishKicker {

    private BullishKicker() {}

    static final PatternMetadata METADATA = new PatternMetadata(
            PatternType.REVERSAL,
            PatternDirection.BULLISH,
            0.83,
            PatternStrength.STRONG,
            "Gap reversal from bearish to bullish — sudden decisive change in sentiment");

    public static boolean matches(CandleProps prev, CandleProps curr) {
        return prev.bearish()
                && curr.bullish()
                && curr.open() >= prev.open();
    }

    public static PatternDefinition definition() {
        return new PatternDefinition("bullishKicker", c -> matches(c[0], c[1]), 2, METADATA);
    }
}
