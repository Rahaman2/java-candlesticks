package com.candlesticks.binance;

/**
 * Identifies a live data subscription by symbol and kline interval.
 * Used as a map key throughout the application.
 *
 * @param symbol   e.g. "BTCUSDT"
 * @param interval e.g. "1m", "5m", "1h"
 */
public record SubscriptionKey(String symbol, String interval) {

    /** Returns e.g. {@code "BTCUSDT_1m"} — used as STOMP topic suffix and display label. */
    public String toTopicKey() {
        return symbol + "_" + interval;
    }
}
