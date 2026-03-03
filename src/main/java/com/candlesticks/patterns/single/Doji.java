package com.candlesticks.patterns.single;

import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.*;

/**
 * Doji — body is less than 10% of the total range.
 *
 * <p>Signals indecision: buyers and sellers finished at nearly the same price.
 * Stronger signal when it appears after a prolonged trend.</p>
 *
 * <p>Variants registered:</p>
 * <ul>
 *   <li>{@code doji} — any doji</li>
 *   <li>{@code bullishDoji} — doji with a slightly bullish close (close ≥ open)</li>
 *   <li>{@code bearishDoji} — doji with a slightly bearish close (close ≤ open)</li>
 * </ul>
 */
public final class Doji {

    private Doji() {}

    // ── Metadata ─────────────────────────────────────────────────────────────

    static final PatternMetadata METADATA = new PatternMetadata(
            PatternType.NEUTRAL,
            PatternDirection.NEUTRAL,
            0.55,
            PatternStrength.WEAK,
            "Indecision — buyers and sellers finished near the same price");

    static final PatternMetadata BULLISH_META = new PatternMetadata(
            PatternType.NEUTRAL,
            PatternDirection.BULLISH,
            0.58,
            PatternStrength.WEAK,
            "Doji with a slight bullish bias");

    static final PatternMetadata BEARISH_META = new PatternMetadata(
            PatternType.NEUTRAL,
            PatternDirection.BEARISH,
            0.58,
            PatternStrength.WEAK,
            "Doji with a slight bearish bias");

    // ── Detection ────────────────────────────────────────────────────────────

    /** Body is less than 10% of the candle's range. */
    public static boolean matches(CandleProps c) {
        return c.range() > 0 && c.bodySize() / c.range() < 0.1;
    }

    public static boolean matchesBullish(CandleProps c) {
        return !c.isBearish() && matches(c);
    }

    public static boolean matchesBearish(CandleProps c) {
        return !c.isBullish() && matches(c);
    }

    // ── Definitions ──────────────────────────────────────────────────────────

    public static PatternDefinition definition()        { return new PatternDefinition("doji",        c -> matches(c[0]),        1, METADATA);     }
    public static PatternDefinition bullishDefinition() { return new PatternDefinition("bullishDoji", c -> matchesBullish(c[0]), 1, BULLISH_META); }
    public static PatternDefinition bearishDefinition() { return new PatternDefinition("bearishDoji", c -> matchesBearish(c[0]), 1, BEARISH_META); }
}
