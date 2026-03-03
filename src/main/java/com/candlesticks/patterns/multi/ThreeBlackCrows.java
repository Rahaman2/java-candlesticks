package com.candlesticks.patterns.multi;

import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.*;

/**
 * Three Black Crows — three consecutive bearish candles, each closing lower and
 * opening within the prior candle's body, with small lower wicks.
 *
 * <p>Criteria (applied across all three candles):</p>
 * <ul>
 *   <li>All three candles are bearish</li>
 *   <li>Each candle closes lower than the previous</li>
 *   <li>Each candle opens within the prior candle's body (open ≤ prev open, open ≥ prev close)</li>
 *   <li>Each candle's lower wick is ≤ 30% of its body (minimal lower shadow)</li>
 * </ul>
 *
 * <p>Mirror image of Three White Soldiers. Signals relentless selling pressure across
 * three sessions. Most powerful after an uptrend or at a key resistance level.</p>
 */
public final class ThreeBlackCrows {

    private ThreeBlackCrows() {}

    static final PatternMetadata METADATA = new PatternMetadata(
            PatternType.CONTINUATION,
            PatternDirection.BEARISH,
            0.75,
            PatternStrength.STRONG,
            "Three falling bearish candles — sustained selling pressure across three sessions");

    public static boolean matches(CandleProps c0, CandleProps c1, CandleProps c2) {
        return c0.bearish() && c1.bearish() && c2.bearish()
                && c1.close() < c0.close()
                && c2.close() < c1.close()
                && c1.open() <= c0.open() && c1.open() >= c0.close()
                && c2.open() <= c1.open() && c2.open() >= c1.close()
                && c0.lowerWick() <= c0.bodySize() * 0.3
                && c1.lowerWick() <= c1.bodySize() * 0.3
                && c2.lowerWick() <= c2.bodySize() * 0.3;
    }

    public static PatternDefinition definition() {
        return new PatternDefinition("threeBlackCrows", c -> matches(c[0], c[1], c[2]), 3, METADATA);
    }
}
