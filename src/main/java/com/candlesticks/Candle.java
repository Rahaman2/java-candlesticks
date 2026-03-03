package com.candlesticks;

import com.candlesticks.interfaces.ICandle;

/**
 * Immutable OHLCV candlestick.
 *
 * <p>Constructor order follows the CCXT convention: timestamp, open, high, low, close, volume.
 * Convenience overloads are provided for manual/test usage.</p>
 *
 * <p>CCXT fetch_ohlcv returns: [timestamp, open, high, low, close, volume]</p>
 */
public final class Candle implements ICandle {

    private final long   timestamp;
    private final double open;
    private final double high;
    private final double low;
    private final double close;
    private final double volume;

    /** Full constructor — matches CCXT [timestamp, open, high, low, close, volume]. */
    public Candle(long timestamp, double open, double high, double low, double close, double volume) {
        this.timestamp = timestamp;
        this.open      = open;
        this.high      = high;
        this.low       = low;
        this.close     = close;
        this.volume    = volume;
    }

    /** Convenience: OHLC only, no timestamp or volume. */
    public Candle(double open, double high, double low, double close) {
        this(0L, open, high, low, close, 0.0);
    }

    /** Convenience: OHLCV, no timestamp. */
    public Candle(double open, double high, double low, double close, double volume) {
        this(0L, open, high, low, close, volume);
    }

    @Override public double open()      { return open; }
    @Override public double high()      { return high; }
    @Override public double low()       { return low; }
    @Override public double close()     { return close; }
    @Override public double volume()    { return volume; }
    @Override public long   timestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("Candle{ts=%d, O=%.4f, H=%.4f, L=%.4f, C=%.4f, V=%.2f}",
                timestamp, open, high, low, close, volume);
    }
}
