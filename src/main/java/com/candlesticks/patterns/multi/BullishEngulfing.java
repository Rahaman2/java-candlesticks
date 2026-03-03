package com.candlesticks.patterns.multi;

import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.*;

/**
 * Bullish Engulfing — a bullish candle whose body completely swallows the prior bearish body.
 *
 * <p>Criteria:</p>
 * <ul>
 *   <li>Previous candle is bearish</li>
 *   <li>Current candle is bullish</li>
 *   <li>Current open is below previous close (body bottom of bearish candle)</li>
 *   <li>Current close is above previous open (body top of bearish candle)</li>
 *   <li>Current body is larger than previous body</li>
 * </ul>
 *
 * <p>One of the strongest two-candle reversal signals. After a downtrend, shows
 * that buyers overwhelmed sellers completely in a single session.</p>
 */
public final class BullishEngulfing {

    private BullishEngulfing() {}

    static final PatternMetadata METADATA = new PatternMetadata(
            PatternType.REVERSAL,
            PatternDirection.BULLISH,
            0.78,
            PatternStrength.STRONG,
            "Bullish candle fully engulfs prior bearish body — strong bullish reversal signal");

    public static boolean matches(CandleProps prev, CandleProps curr) {
        return prev.bearish()
                && curr.bullish()
                && curr.open() < prev.close()
                && curr.close() > prev.open()
                && curr.bodySize() > prev.bodySize();
    }

    public static PatternDefinition definition() {
        return new PatternDefinition("bullishEngulfing", c -> matches(c[0], c[1]), 2, METADATA);
    }
}
