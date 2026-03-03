package com.candlesticks.model;

import com.candlesticks.interfaces.ICandle;

import java.util.List;

/**
 * Immutable candle with all derived properties precomputed.
 *
 * <p>Pattern detectors always receive {@code CandleProps} so they never repeat the same arithmetic.
 * Use {@link #from(ICandle)} or {@link #fromList(List)} to convert raw candles.</p>
 *
 * <p>Field naming mirrors {@link ICandle} so this record can itself be used wherever
 * {@code ICandle} is expected (the auto-generated accessors satisfy the interface).</p>
 */
public record CandleProps(
        double open,
        double high,
        double low,
        double close,
        double volume,
        long   timestamp,

        // --- precomputed ---
        double bodySize,    // |close - open|
        double upperWick,   // high - max(open, close)
        double lowerWick,   // min(open, close) - low
        double range,       // high - low

        boolean bullish,    // close > open
        boolean bearish     // close < open

) implements ICandle {

    // ── ICandle satisfaction ─────────────────────────────────────────────────

    // The record auto-generates: open(), high(), low(), close(), volume(), timestamp(),
    // bodySize(), upperWick(), lowerWick(), range() — all matching ICandle.
    // Only isBullish/isBearish need explicit bridge methods (component names differ).

    public boolean isBullish() { return bullish; }
    public boolean isBearish() { return bearish; }

    // ── Convenience ──────────────────────────────────────────────────────────

    /** Upper end of the body (max of open/close). */
    public double bodyTop()    { return Math.max(open, close); }

    /** Lower end of the body (min of open/close). */
    public double bodyBottom() { return Math.min(open, close); }

    // ── Factory ──────────────────────────────────────────────────────────────

    /** Compute all properties from a raw {@link ICandle}. */
    public static CandleProps from(ICandle c) {
        double o = c.open(), h = c.high(), l = c.low(), cl = c.close();
        double body  = Math.abs(cl - o);
        double upper = h - Math.max(o, cl);
        double lower = Math.min(o, cl) - l;
        double rng   = h - l;
        return new CandleProps(
                o, h, l, cl, c.volume(), c.timestamp(),
                body, upper, lower, rng,
                cl > o, cl < o
        );
    }

    /** Convert a list of raw candles to a list of enriched {@code CandleProps}. */
    public static List<CandleProps> fromList(List<? extends ICandle> candles) {
        return candles.stream().map(CandleProps::from).toList();
    }
}
