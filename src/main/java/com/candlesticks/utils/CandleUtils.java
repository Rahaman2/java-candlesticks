package com.candlesticks.utils;

import com.candlesticks.model.CandleProps;

/**
 * Static helpers for multi-candle relationships.
 * Single-candle arithmetic lives directly on {@link CandleProps}.
 */
public final class CandleUtils {

    private CandleUtils() {}

    /**
     * Returns {@code true} if the previous candle's body top is strictly below
     * the current candle's body bottom (gap up).
     */
    public static boolean hasGapUp(CandleProps prev, CandleProps curr) {
        return prev.bodyTop() < curr.bodyBottom();
    }

    /**
     * Returns {@code true} if the previous candle's body bottom is strictly above
     * the current candle's body top (gap down).
     */
    public static boolean hasGapDown(CandleProps prev, CandleProps curr) {
        return prev.bodyBottom() > curr.bodyTop();
    }

    /**
     * Returns {@code true} if {@code inner}'s body is fully contained within
     * {@code outer}'s body (used for harami, inside-bar patterns).
     */
    public static boolean isEngulfed(CandleProps inner, CandleProps outer) {
        return inner.bodyTop()    <= outer.bodyTop() &&
               inner.bodyBottom() >= outer.bodyBottom();
    }
}
