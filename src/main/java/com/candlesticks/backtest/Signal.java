package com.candlesticks.backtest;

/**
 * Trading signal emitted by a {@link Strategy} for a single candle.
 */
public enum Signal {
    /** Open a long position (or close a short). */
    BUY,
    /** Close a long position (or open a short). */
    SELL,
    /** Do nothing. */
    HOLD
}
