package com.candlesticks.patterns.multi;

import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.*;

/**
 * Tweezer Bottom — two consecutive candles with matching (or very nearly matching) lows,
 * with the second candle being bullish.
 *
 * <p>Criteria:</p>
 * <ul>
 *   <li>Both candles have nearly identical lows (within 2% of the larger candle's range)</li>
 *   <li>Second candle is bullish (confirms reversal bias)</li>
 * </ul>
 *
 * <p>Signals that price found a support level twice and was rejected both times.
 * The bullish second candle adds confidence in the bounce. Most reliable after a downtrend.</p>
 */
public final class TweezerBottom {

    private TweezerBottom() {}

    static final PatternMetadata METADATA = new PatternMetadata(
            PatternType.REVERSAL,
            PatternDirection.BULLISH,
            0.62,
            PatternStrength.MODERATE,
            "Double low with bullish close — price found support and bounced twice");

    public static boolean matches(CandleProps prev, CandleProps curr) {
        double maxRange = Math.max(Math.max(prev.range(), curr.range()), 0.0001);
        return curr.bullish()
                && Math.abs(prev.low() - curr.low()) / maxRange < 0.02;
    }

    public static PatternDefinition definition() {
        return new PatternDefinition("tweezerBottom", c -> matches(c[0], c[1]), 2, METADATA);
    }
}
