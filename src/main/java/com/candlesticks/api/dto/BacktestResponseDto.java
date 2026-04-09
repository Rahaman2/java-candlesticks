package com.candlesticks.api.dto;

import com.candlesticks.backtest.BacktestResult;
import com.candlesticks.backtest.BacktestStats;
import com.candlesticks.backtest.Trade;

import java.math.BigDecimal;
import java.util.List;

/** Response body for {@code POST /api/v1/backtest}. */
public record BacktestResponseDto(StatsDto stats, List<TradeDto> trades) {

    public static BacktestResponseDto from(BacktestResult result) {
        return new BacktestResponseDto(
                StatsDto.from(result.stats()),
                result.trades().stream().map(TradeDto::from).toList());
    }

    public record StatsDto(
            int        totalTrades,
            int        winningTrades,
            int        losingTrades,
            BigDecimal totalPnl,
            BigDecimal winRate,
            BigDecimal maxDrawdown,
            BigDecimal profitFactor,
            BigDecimal finalEquity
    ) {
        public static StatsDto from(BacktestStats s) {
            return new StatsDto(s.totalTrades(), s.winningTrades(), s.losingTrades(),
                    s.totalPnl(), s.winRate(), s.maxDrawdown(),
                    s.profitFactor(), s.finalEquity());
        }
    }

    public record TradeDto(
            int    entryIndex,
            int    exitIndex,
            double entryPrice,
            double exitPrice,
            BigDecimal pnl,
            boolean    win
    ) {
        public static TradeDto from(Trade t) {
            return new TradeDto(t.entryIndex(), t.exitIndex(),
                    t.entryPrice(), t.exitPrice(), t.pnl(), t.isWin());
        }
    }
}
