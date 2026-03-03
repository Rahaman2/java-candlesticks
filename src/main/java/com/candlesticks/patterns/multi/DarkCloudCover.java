package com.candlesticks.patterns.multi;

import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.*;

/**
 * Dark Cloud Cover — a bearish candle that opens above the prior bullish candle's high
 * and closes below the midpoint of the prior bullish body.
 *
 * <p>Criteria:</p>
 * <ul>
 *   <li>Previous candle is bullish</li>
 *   <li>Current candle is bearish</li>
 *   <li>Current opens above the previous candle's high (gap up open)</li>
 *   <li>Current closes below the midpoint of the previous body but above its open</li>
 * </ul>
 *
 * <p>Shows that despite a strong open, sellers pushed prices down through half of the
 * prior day's gains. Bearish reversal signal after an uptrend. If it closes below
 * the prior open it becomes a Bearish Engulfing instead.</p>
 */
public final class DarkCloudCover {

    private DarkCloudCover() {}

    static final PatternMetadata METADATA = new PatternMetadata(
            PatternType.REVERSAL,
            PatternDirection.BEARISH,
            0.72,
            PatternStrength.MODERATE,
            "Bearish close into prior bullish body — sellers erasing prior gains");

    public static boolean matches(CandleProps prev, CandleProps curr) {
        double prevMid = (prev.open() + prev.close()) / 2.0;
        return prev.bullish()
                && curr.bearish()
                && curr.open() > prev.high()
                && curr.close() < prevMid
                && curr.close() > prev.open();
    }

    public static PatternDefinition definition() {
        return new PatternDefinition("darkCloudCover", c -> matches(c[0], c[1]), 2, METADATA);
    }
}
