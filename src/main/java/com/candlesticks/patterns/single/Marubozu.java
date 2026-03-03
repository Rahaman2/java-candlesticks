package com.candlesticks.patterns.single;

import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.*;

/**
 * Marubozu — a strong directional candle with virtually no shadows.
 *
 * <p>Criteria:</p>
 * <ul>
 *   <li>Body is at least 95% of the total range</li>
 *   <li>Each shadow is at most 1% of the range (near-zero wicks)</li>
 *   <li>Range must be non-zero</li>
 * </ul>
 *
 * <p>Signals strong conviction in one direction. A bullish Marubozu suggests
 * sustained buying pressure; a bearish Marubozu suggests sustained selling pressure.</p>
 *
 * <p>Variants registered:</p>
 * <ul>
 *   <li>{@code marubozu} — any marubozu regardless of colour</li>
 *   <li>{@code bullishMarubozu} — bullish (green) marubozu</li>
 *   <li>{@code bearishMarubozu} — bearish (red) marubozu</li>
 * </ul>
 */
public final class Marubozu {

    private Marubozu() {}

    // ── Metadata ─────────────────────────────────────────────────────────────

    static final PatternMetadata METADATA = new PatternMetadata(
            PatternType.CONTINUATION,
            PatternDirection.NEUTRAL,
            0.80,
            PatternStrength.STRONG,
            "Strong directional move with minimal shadows");

    static final PatternMetadata BULLISH_META = new PatternMetadata(
            PatternType.CONTINUATION,
            PatternDirection.BULLISH,
            0.85,
            PatternStrength.STRONG,
            "Very strong bullish candle — buyers in full control from open to close");

    static final PatternMetadata BEARISH_META = new PatternMetadata(
            PatternType.CONTINUATION,
            PatternDirection.BEARISH,
            0.85,
            PatternStrength.STRONG,
            "Very strong bearish candle — sellers in full control from open to close");

    // ── Detection ────────────────────────────────────────────────────────────

    public static boolean matches(CandleProps c) {
        return c.range() > 0
                && c.bodySize() / c.range() >= 0.95
                && c.upperWick() / c.range() <= 0.01
                && c.lowerWick() / c.range() <= 0.01;
    }

    public static boolean matchesBullish(CandleProps c) {
        return c.isBullish() && matches(c);
    }

    public static boolean matchesBearish(CandleProps c) {
        return c.isBearish() && matches(c);
    }

    // ── Definitions ──────────────────────────────────────────────────────────

    public static PatternDefinition definition()        { return new PatternDefinition("marubozu",       c -> matches(c[0]),        1, METADATA);     }
    public static PatternDefinition bullishDefinition() { return new PatternDefinition("bullishMarubozu", c -> matchesBullish(c[0]), 1, BULLISH_META); }
    public static PatternDefinition bearishDefinition() { return new PatternDefinition("bearishMarubozu", c -> matchesBearish(c[0]), 1, BEARISH_META); }
}
