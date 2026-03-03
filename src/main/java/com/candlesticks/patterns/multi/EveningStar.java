package com.candlesticks.patterns.multi;

import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.*;

/**
 * Evening Star — a three-candle bearish reversal pattern at the top of an uptrend.
 *
 * <p>Criteria:</p>
 * <ol>
 *   <li>First candle: large bullish body (body ≥ 50% of its range)</li>
 *   <li>Middle candle: small body — doji or spinning top (body ≤ 30% of its range)</li>
 *   <li>Third candle: bearish, closing below the midpoint of the first candle's body</li>
 * </ol>
 *
 * <p>Mirror image of the Morning Star. The third candle closing into the first candle's
 * bullish body confirms that sellers have taken control and the uptrend is reversing.</p>
 */
public final class EveningStar {

    private EveningStar() {}

    static final PatternMetadata METADATA = new PatternMetadata(
            PatternType.REVERSAL,
            PatternDirection.BEARISH,
            0.78,
            PatternStrength.STRONG,
            "Three-candle top reversal: large bull → small indecision → large bear");

    public static boolean matches(CandleProps first, CandleProps middle, CandleProps last) {
        double firstMid = (first.open() + first.close()) / 2.0;
        boolean middleSmall = middle.range() == 0
                || middle.bodySize() / middle.range() <= 0.3;
        return first.bullish()
                && first.bodySize() >= first.range() * 0.5
                && middleSmall
                && last.bearish()
                && last.close() < firstMid;
    }

    public static PatternDefinition definition() {
        return new PatternDefinition("eveningStar", c -> matches(c[0], c[1], c[2]), 3, METADATA);
    }
}
