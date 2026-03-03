package com.candlesticks.patterns.multi;

import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.*;

/**
 * Piercing Line — a bullish candle that opens below the prior bearish candle's low
 * and closes above the midpoint of the prior bearish body.
 *
 * <p>Criteria:</p>
 * <ul>
 *   <li>Previous candle is bearish</li>
 *   <li>Current candle is bullish</li>
 *   <li>Current opens below the previous candle's low (gap down open)</li>
 *   <li>Current closes above the midpoint of the previous body but below its open</li>
 * </ul>
 *
 * <p>Shows that despite a weak open, buyers pushed prices back up through half of the
 * prior day's losses. A bullish reversal signal after a downtrend. If it closes above
 * the prior open it becomes a Bullish Engulfing instead.</p>
 */
public final class PiercingLine {

    private PiercingLine() {}

    static final PatternMetadata METADATA = new PatternMetadata(
            PatternType.REVERSAL,
            PatternDirection.BULLISH,
            0.72,
            PatternStrength.MODERATE,
            "Bullish recovery piercing into prior bearish body — buyers regaining control");

    public static boolean matches(CandleProps prev, CandleProps curr) {
        double prevMid = (prev.open() + prev.close()) / 2.0;
        return prev.bearish()
                && curr.bullish()
                && curr.open() < prev.low()
                && curr.close() > prevMid
                && curr.close() < prev.open();
    }

    public static PatternDefinition definition() {
        return new PatternDefinition("piercingLine", c -> matches(c[0], c[1]), 2, METADATA);
    }
}
