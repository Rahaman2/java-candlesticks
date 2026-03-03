package com.candlesticks.patterns.multi;

import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.*;

/**
 * Shooting Star — an inverted-hammer-shaped candle (long upper wick, small body near the bottom
 * of range) with a bearish body, signalling potential reversal at the top of an uptrend.
 *
 * <p>Shape criteria (mirrors Inverted Hammer):</p>
 * <ul>
 *   <li>Upper wick ≥ 2× body size</li>
 *   <li>Lower wick ≤ body size</li>
 *   <li>Body bottom is in the lower two-thirds of the range</li>
 *   <li>Range must be non-zero</li>
 *   <li>Body is bearish (close &lt; open) — strongest warning signal</li>
 * </ul>
 *
 * <p><strong>Note:</strong> This detector is shape-only and has no trend-context awareness.
 * The same shape after a downtrend is called an Inverted Hammer (bullish signal).
 * The bearish body requirement encodes the stronger bearish-reversal variant.</p>
 */
public final class ShootingStar {

    private ShootingStar() {}

    static final PatternMetadata METADATA = new PatternMetadata(
            PatternType.REVERSAL,
            PatternDirection.BEARISH,
            0.70,
            PatternStrength.MODERATE,
            "Long upper wick with bearish body — buyers failed to hold gains, sellers took control");

    public static boolean matches(CandleProps c) {
        return c.range() > 0
                && c.upperWick() >= 2 * c.bodySize()
                && c.lowerWick() <= c.bodySize()
                && c.bodyBottom() < c.high() - (c.range() * 2.0 / 3.0)
                && c.bearish();
    }

    public static PatternDefinition definition() {
        return new PatternDefinition("shootingStar", c -> matches(c[0]), 1, METADATA);
    }
}
