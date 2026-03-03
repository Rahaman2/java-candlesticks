package com.candlesticks.pattern;

import com.candlesticks.model.CandleProps;

/**
 * Functional interface for a pattern detection function.
 *
 * <p>The varargs parameter receives exactly {@code paramCount} candles (as defined in
 * {@link PatternDefinition}). Single-candle patterns use {@code candles[0]};
 * two-candle patterns use {@code candles[0]} (previous) and {@code candles[1]} (current), etc.</p>
 *
 * <p>Example lambda for a single-candle pattern:</p>
 * <pre>{@code
 * PatternDetector d = candles -> Hammer.matches(candles[0]);
 * }</pre>
 */
@FunctionalInterface
public interface PatternDetector {
    boolean matches(CandleProps... candles);
}
