package com.candlesticks.backtest;

import java.math.BigDecimal;

/**
 * Summary statistics for a completed backtest run.
 *
 * <p>All monetary/percentage values use {@link BigDecimal} (audit checklist §4).</p>
 *
 * @param totalTrades    number of completed round-trip trades
 * @param winningTrades  trades where net P&amp;L &gt; 0
 * @param losingTrades   trades where net P&amp;L &le; 0
 * @param totalPnl       sum of all trade P&amp;Ls
 * @param winRate        winning trades / total trades as a percentage (0–100)
 * @param maxDrawdown    largest peak-to-trough equity decline as a percentage (0–100)
 * @param profitFactor   gross profit / gross loss ({@code 999} when there are no losses)
 * @param finalEquity    initialCapital + totalPnl
 */
public record BacktestStats(
        int        totalTrades,
        int        winningTrades,
        int        losingTrades,
        BigDecimal totalPnl,
        BigDecimal winRate,
        BigDecimal maxDrawdown,
        BigDecimal profitFactor,
        BigDecimal finalEquity
) {}
