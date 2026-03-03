package com.candlesticks.patterns.multi;

import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.*;

/**
 * Three White Soldiers — three consecutive bullish candles, each closing higher and
 * opening within the prior candle's body, with small upper wicks.
 *
 * <p>Criteria (applied across all three candles):</p>
 * <ul>
 *   <li>All three candles are bullish</li>
 *   <li>Each candle closes higher than the previous</li>
 *   <li>Each candle opens within the prior candle's body (open ≥ prev open, open ≤ prev close)</li>
 *   <li>Each candle's upper wick is ≤ 30% of its body (minimal upper shadow)</li>
 * </ul>
 *
 * <p>Signals strong and sustained buying pressure over three sessions. One of the most
 * reliable bullish continuation patterns. Most powerful after a consolidation or at the
 * end of a downtrend.</p>
 */
public final class ThreeWhiteSoldiers {

    private ThreeWhiteSoldiers() {}

    static final PatternMetadata METADATA = new PatternMetadata(
            PatternType.CONTINUATION,
            PatternDirection.BULLISH,
            0.75,
            PatternStrength.STRONG,
            "Three rising bullish candles — sustained buying pressure across three sessions");

    public static boolean matches(CandleProps c0, CandleProps c1, CandleProps c2) {
        return c0.bullish() && c1.bullish() && c2.bullish()
                && c1.close() > c0.close()
                && c2.close() > c1.close()
                && c1.open() >= c0.open() && c1.open() <= c0.close()
                && c2.open() >= c1.open() && c2.open() <= c1.close()
                && c0.upperWick() <= c0.bodySize() * 0.3
                && c1.upperWick() <= c1.bodySize() * 0.3
                && c2.upperWick() <= c2.bodySize() * 0.3;
    }

    public static PatternDefinition definition() {
        return new PatternDefinition("threeWhiteSoldiers", c -> matches(c[0], c[1], c[2]), 3, METADATA);
    }
}
