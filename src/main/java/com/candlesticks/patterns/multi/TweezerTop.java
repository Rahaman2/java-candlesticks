package com.candlesticks.patterns.multi;

import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.*;

/**
 * Tweezer Top — two consecutive candles with matching (or very nearly matching) highs,
 * with the second candle being bearish.
 *
 * <p>Criteria:</p>
 * <ul>
 *   <li>Both candles have nearly identical highs (within 2% of the larger candle's range)</li>
 *   <li>Second candle is bearish (confirms reversal bias)</li>
 * </ul>
 *
 * <p>Signals that price hit a resistance level twice and was rejected both times.
 * The bearish second candle confirms that sellers stepped in at that level. Most reliable
 * after an uptrend.</p>
 */
public final class TweezerTop {

    private TweezerTop() {}

    static final PatternMetadata METADATA = new PatternMetadata(
            PatternType.REVERSAL,
            PatternDirection.BEARISH,
            0.62,
            PatternStrength.MODERATE,
            "Double high with bearish close — price hit resistance and reversed twice");

    public static boolean matches(CandleProps prev, CandleProps curr) {
        double maxRange = Math.max(Math.max(prev.range(), curr.range()), 0.0001);
        return curr.bearish()
                && Math.abs(prev.high() - curr.high()) / maxRange < 0.02;
    }

    public static PatternDefinition definition() {
        return new PatternDefinition("tweezerTop", c -> matches(c[0], c[1]), 2, METADATA);
    }
}
