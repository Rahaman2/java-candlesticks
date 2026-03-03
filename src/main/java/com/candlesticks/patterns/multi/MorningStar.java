package com.candlesticks.patterns.multi;

import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.*;

/**
 * Morning Star — a three-candle bullish reversal pattern at the bottom of a downtrend.
 *
 * <p>Criteria:</p>
 * <ol>
 *   <li>First candle: large bearish body (body ≥ 50% of its range)</li>
 *   <li>Middle candle: small body — doji or spinning top (body ≤ 30% of its range)</li>
 *   <li>Third candle: bullish, closing above the midpoint of the first candle's body</li>
 * </ol>
 *
 * <p>The pattern tells a three-act story: strong selling (candle 1), indecision and
 * momentum loss (candle 2), buyers taking over (candle 3). The third candle closing
 * into the first candle's body confirms the reversal.</p>
 */
public final class MorningStar {

    private MorningStar() {}

    static final PatternMetadata METADATA = new PatternMetadata(
            PatternType.REVERSAL,
            PatternDirection.BULLISH,
            0.78,
            PatternStrength.STRONG,
            "Three-candle bottom reversal: large bear → small indecision → large bull");

    public static boolean matches(CandleProps first, CandleProps middle, CandleProps last) {
        double firstMid = (first.open() + first.close()) / 2.0;
        boolean middleSmall = middle.range() == 0
                || middle.bodySize() / middle.range() <= 0.3;
        return first.bearish()
                && first.bodySize() >= first.range() * 0.5
                && middleSmall
                && last.bullish()
                && last.close() > firstMid;
    }

    public static PatternDefinition definition() {
        return new PatternDefinition("morningStar", c -> matches(c[0], c[1], c[2]), 3, METADATA);
    }
}
