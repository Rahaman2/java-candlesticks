package com.candlesticks.api.dto;

/** Request body for {@code POST /api/v1/backtest}. */
public record BacktestRequestDto(
        String symbol,
        String interval,
        String strategyName,
        Double initialCapital,
        Double positionSize,
        Double commissionRate,
        Double slippage
) {}
