package com.candlesticks.backtest;

import java.math.BigDecimal;

/**
 * An executed round-trip trade recorded by {@link BacktestRunner}.
 *
 * <p>P&amp;L is stored as {@link BigDecimal} to avoid floating-point rounding errors
 * that accumulate over many trades (see audit checklist §4).</p>
 *
 * @param entryIndex series index of the entry candle
 * @param exitIndex  series index of the exit candle
 * @param entryPrice fill price on entry (after slippage)
 * @param exitPrice  fill price on exit  (after slippage)
 * @param pnl        net profit/loss in currency units (after commission)
 * @param isWin      {@code true} if {@code pnl > 0}
 */
public record Trade(
        int        entryIndex,
        int        exitIndex,
        double     entryPrice,
        double     exitPrice,
        BigDecimal pnl,
        boolean    isWin
) {}
