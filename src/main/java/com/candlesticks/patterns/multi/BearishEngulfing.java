package com.candlesticks.patterns.multi;

import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.*;

/**
 * Bearish Engulfing — a bearish candle whose body completely swallows the prior bullish body.
 *
 * <p>Criteria:</p>
 * <ul>
 *   <li>Previous candle is bullish</li>
 *   <li>Current candle is bearish</li>
 *   <li>Current open is above previous close (body top of bullish candle)</li>
 *   <li>Current close is below previous open (body bottom of bullish candle)</li>
 *   <li>Current body is larger than previous body</li>
 * </ul>
 *
 * <p>After an uptrend, shows that sellers overwhelmed buyers completely in a single session.
 * One of the most reliable bearish reversal signals.</p>
 */
public final class BearishEngulfing {

    private BearishEngulfing() {}

    static final PatternMetadata METADATA = new PatternMetadata(
            PatternType.REVERSAL,
            PatternDirection.BEARISH,
            0.78,
            PatternStrength.STRONG,
            "Bearish candle fully engulfs prior bullish body — strong bearish reversal signal");

    public static boolean matches(CandleProps prev, CandleProps curr) {
        return prev.bullish()
                && curr.bearish()
                && curr.open() > prev.close()
                && curr.close() < prev.open()
                && curr.bodySize() > prev.bodySize();
    }

    public static PatternDefinition definition() {
        return new PatternDefinition("bearishEngulfing", c -> matches(c[0], c[1]), 2, METADATA);
    }
}
