package com.candlesticks.patterns.multi;

import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.*;

/**
 * Hanging Man — a hammer-shaped candle (long lower wick, small body near the top of range)
 * with a bearish body, signalling potential reversal at the top of an uptrend.
 *
 * <p>Shape criteria (identical to Hammer):</p>
 * <ul>
 *   <li>Lower wick ≥ 2× body size</li>
 *   <li>Upper wick ≤ body size</li>
 *   <li>Body top is in the upper two-thirds of the range</li>
 *   <li>Range must be non-zero</li>
 *   <li>Body is bearish (close &lt; open) — strongest warning signal</li>
 * </ul>
 *
 * <p><strong>Note:</strong> This detector is shape-only and has no trend-context awareness.
 * A Hanging Man shape appearing in a downtrend is called a Hammer instead.
 * The bearish body requirement encodes the stronger signal variant.</p>
 */
public final class HangingMan {

    private HangingMan() {}

    static final PatternMetadata METADATA = new PatternMetadata(
            PatternType.REVERSAL,
            PatternDirection.BEARISH,
            0.67,
            PatternStrength.MODERATE,
            "Hammer shape with bearish body at top of range — potential trend exhaustion");

    public static boolean matches(CandleProps c) {
        return c.range() > 0
                && c.lowerWick() >= 2 * c.bodySize()
                && c.upperWick() <= c.bodySize()
                && c.bodyTop() > c.low() + (c.range() * 2.0 / 3.0)
                && c.bearish();
    }

    public static PatternDefinition definition() {
        return new PatternDefinition("hangingMan", c -> matches(c[0]), 1, METADATA);
    }
}
