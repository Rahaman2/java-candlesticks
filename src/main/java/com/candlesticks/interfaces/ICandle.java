package com.candlesticks.interfaces;

/**
 * Minimal OHLCV + timestamp contract for a single candlestick.
 * All computed properties (bodySize, wicks, etc.) live in CandleProps.
 */
public interface ICandle {

    /** Opening price */
    double open();

    /** Highest price reached */
    double high();

    /** Lowest price reached */
    double low();

    /** Closing price */
    double close();

    /**
     * Trade volume. Returns 0 if unknown.
     * Populated when data comes from a live feed (e.g. CCXT).
     */
    double volume();

    /**
     * Candle open time as epoch milliseconds. Returns 0 if unknown.
     * Populated when data comes from a live feed (e.g. CCXT).
     */
    long timestamp();
}
