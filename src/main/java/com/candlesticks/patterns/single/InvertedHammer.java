package com.candlesticks.patterns.single;

import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.*;

/**
 * Inverted Hammer — long upper shadow, small body in the lower third of the range.
 *
 * <p>Criteria:</p>
 * <ul>
 *   <li>Upper wick ≥ 2× body size</li>
 *   <li>Lower wick ≤ body size</li>
 *   <li>Body bottom is in the lower two-thirds of the range</li>
 *   <li>Range must be non-zero</li>
 * </ul>
 *
 * <p>After a downtrend, signals that bulls attempted to push prices up.
 * Requires confirmation on the next candle. The bullish variant is a stronger signal.</p>
 *
 * <p>Note: The same shape at the top of an uptrend is called a Shooting Star
 * (context-dependent). The Shooting Star will be a separate pattern in Batch 2.</p>
 *
 * <p>Variants registered:</p>
 * <ul>
 *   <li>{@code invertedHammer} — any inverted hammer</li>
 *   <li>{@code bullishInvertedHammer} — inverted hammer with bullish body</li>
 *   <li>{@code bearishInvertedHammer} — inverted hammer with bearish body</li>
 * </ul>
 */
public final class InvertedHammer {

    private InvertedHammer() {}

    // ── Metadata ─────────────────────────────────────────────────────────────

    static final PatternMetadata METADATA = new PatternMetadata(
            PatternType.REVERSAL,
            PatternDirection.BULLISH,
            0.65,
            PatternStrength.MODERATE,
            "Potential bullish reversal — long upper shadow shows buyers tested higher prices");

    static final PatternMetadata BULLISH_META = new PatternMetadata(
            PatternType.REVERSAL,
            PatternDirection.BULLISH,
            0.70,
            PatternStrength.MODERATE,
            "Inverted hammer with bullish body — stronger reversal signal");

    static final PatternMetadata BEARISH_META = new PatternMetadata(
            PatternType.REVERSAL,
            PatternDirection.BULLISH,
            0.60,
            PatternStrength.WEAK,
            "Inverted hammer with bearish body — weaker reversal signal");

    // ── Detection ────────────────────────────────────────────────────────────

    public static boolean matches(CandleProps c) {
        return c.range() > 0
                && c.upperWick() >= 2 * c.bodySize()
                && c.lowerWick() <= c.bodySize()
                && c.bodyBottom() < c.high() - (c.range() * 2.0 / 3.0);
    }

    public static boolean matchesBullish(CandleProps c) {
        return c.isBullish() && matches(c);
    }

    public static boolean matchesBearish(CandleProps c) {
        return c.isBearish() && matches(c);
    }

    // ── Definitions ──────────────────────────────────────────────────────────

    public static PatternDefinition definition()        { return new PatternDefinition("invertedHammer",       c -> matches(c[0]),        1, METADATA);     }
    public static PatternDefinition bullishDefinition() { return new PatternDefinition("bullishInvertedHammer", c -> matchesBullish(c[0]), 1, BULLISH_META); }
    public static PatternDefinition bearishDefinition() { return new PatternDefinition("bearishInvertedHammer", c -> matchesBearish(c[0]), 1, BEARISH_META); }
}
