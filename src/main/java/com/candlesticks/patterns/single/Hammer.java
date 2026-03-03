package com.candlesticks.patterns.single;

import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.*;

/**
 * Hammer (Pinbar) — long lower shadow, small body in the upper third of the range.
 *
 * <p>Criteria:</p>
 * <ul>
 *   <li>Lower wick ≥ 2× body size</li>
 *   <li>Upper wick ≤ body size</li>
 *   <li>Body top is in the upper two-thirds of the range</li>
 *   <li>Range must be non-zero</li>
 * </ul>
 *
 * <p>Signals potential bullish reversal after a downtrend. The bullish variant (close > open)
 * is a stronger signal than the bearish variant.</p>
 *
 * <p>Variants registered:</p>
 * <ul>
 *   <li>{@code hammer} — any hammer regardless of body colour</li>
 *   <li>{@code bullishHammer} — hammer with bullish (green) body</li>
 *   <li>{@code bearishHammer} — hammer with bearish (red) body</li>
 * </ul>
 */
public final class Hammer {

    private Hammer() {}

    // ── Metadata ─────────────────────────────────────────────────────────────

    static final PatternMetadata METADATA = new PatternMetadata(
            PatternType.REVERSAL,
            PatternDirection.BULLISH,
            0.70,
            PatternStrength.MODERATE,
            "Potential bullish reversal — long lower shadow shows rejection of lower prices");

    static final PatternMetadata BULLISH_META = new PatternMetadata(
            PatternType.REVERSAL,
            PatternDirection.BULLISH,
            0.75,
            PatternStrength.MODERATE,
            "Hammer with bullish body — stronger reversal signal");

    static final PatternMetadata BEARISH_META = new PatternMetadata(
            PatternType.REVERSAL,
            PatternDirection.BULLISH,
            0.65,
            PatternStrength.MODERATE,
            "Hammer with bearish body — still a bullish reversal signal, but weaker");

    // ── Detection ────────────────────────────────────────────────────────────

    public static boolean matches(CandleProps c) {
        return c.range() > 0
                && c.lowerWick() >= 2 * c.bodySize()
                && c.upperWick() <= c.bodySize()
                && c.bodyTop() > c.low() + (c.range() * 2.0 / 3.0);
    }

    public static boolean matchesBullish(CandleProps c) {
        return c.isBullish() && matches(c);
    }

    public static boolean matchesBearish(CandleProps c) {
        return c.isBearish() && matches(c);
    }

    // ── Definitions ──────────────────────────────────────────────────────────

    public static PatternDefinition definition()        { return new PatternDefinition("hammer",       c -> matches(c[0]),        1, METADATA);     }
    public static PatternDefinition bullishDefinition() { return new PatternDefinition("bullishHammer", c -> matchesBullish(c[0]), 1, BULLISH_META); }
    public static PatternDefinition bearishDefinition() { return new PatternDefinition("bearishHammer", c -> matchesBearish(c[0]), 1, BEARISH_META); }
}
