package com.candlesticks.api.dto;

import com.candlesticks.Candle;

/**
 * REST/WebSocket representation of a single OHLCV candle.
 * The {@code partial} flag is {@code true} when the kline interval has not yet closed.
 */
public record CandleDto(long ts, double o, double h, double l, double c, double v, boolean partial) {

    public static CandleDto from(Candle candle, boolean partial) {
        return new CandleDto(
                candle.timestamp(),
                candle.open(),
                candle.high(),
                candle.low(),
                candle.close(),
                candle.volume(),
                partial);
    }
}
