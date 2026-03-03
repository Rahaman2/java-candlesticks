package com.candlesticks.patterns.single;

import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.*;

/**
 * Spinning Top — small body with both shadows longer than the body.
 *
 * <p>Criteria:</p>
 * <ul>
 *   <li>Body is between 10% and 30% of the total range (small but not a doji)</li>
 *   <li>Both upper and lower wicks are longer than the body</li>
 *   <li>Range must be non-zero</li>
 * </ul>
 *
 * <p>Like a Doji, signals indecision. The larger body (vs. a Doji) shows some directional
 * bias, but the long wicks indicate neither side could take full control.</p>
 *
 * <p>Variants registered:</p>
 * <ul>
 *   <li>{@code spinningTop} — any spinning top</li>
 *   <li>{@code bullishSpinningTop} — spinning top with bullish body</li>
 *   <li>{@code bearishSpinningTop} — spinning top with bearish body</li>
 * </ul>
 */
public final class SpinningTop {

    private SpinningTop() {}

    // ── Metadata ─────────────────────────────────────────────────────────────

    static final PatternMetadata METADATA = new PatternMetadata(
            PatternType.NEUTRAL,
            PatternDirection.NEUTRAL,
            0.60,
            PatternStrength.WEAK,
            "Indecision — neither buyers nor sellers took clear control");

    static final PatternMetadata BULLISH_META = new PatternMetadata(
            PatternType.NEUTRAL,
            PatternDirection.BULLISH,
            0.58,
            PatternStrength.WEAK,
            "Spinning top with slight bullish bias");

    static final PatternMetadata BEARISH_META = new PatternMetadata(
            PatternType.NEUTRAL,
            PatternDirection.BEARISH,
            0.58,
            PatternStrength.WEAK,
            "Spinning top with slight bearish bias");

    // ── Detection ────────────────────────────────────────────────────────────

    public static boolean matches(CandleProps c) {
        if (c.range() <= 0) return false;
        double bodyRatio = c.bodySize() / c.range();
        return bodyRatio > 0.10 && bodyRatio < 0.30
                && c.upperWick() > c.bodySize()
                && c.lowerWick() > c.bodySize();
    }

    public static boolean matchesBullish(CandleProps c) {
        return c.isBullish() && matches(c);
    }

    public static boolean matchesBearish(CandleProps c) {
        return c.isBearish() && matches(c);
    }

    // ── Definitions ──────────────────────────────────────────────────────────

    public static PatternDefinition definition()        { return new PatternDefinition("spinningTop",       c -> matches(c[0]),        1, METADATA);     }
    public static PatternDefinition bullishDefinition() { return new PatternDefinition("bullishSpinningTop", c -> matchesBullish(c[0]), 1, BULLISH_META); }
    public static PatternDefinition bearishDefinition() { return new PatternDefinition("bearishSpinningTop", c -> matchesBearish(c[0]), 1, BEARISH_META); }
}
