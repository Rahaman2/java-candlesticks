package com.candlesticks.backtest;

import java.math.BigDecimal;

/**
 * Immutable configuration for a {@link BacktestRunner} run.
 *
 * <p>All monetary values are {@link BigDecimal} (audit checklist §4: never use
 * {@code double} for financial calculations).</p>
 *
 * @param initialCapital total starting equity
 * @param positionSize   fixed currency amount allocated per trade (e.g. $1 000)
 * @param commissionRate fraction charged on each leg, e.g. {@code 0.001} = 0.1%
 * @param slippage       price fraction added/subtracted at fill, e.g. {@code 0.0005}
 */
public record BacktestConfig(
        BigDecimal initialCapital,
        BigDecimal positionSize,
        double     commissionRate,
        double     slippage
) {
    /** Sensible defaults: $10 000 capital, $1 000/trade, 0.1% commission, 0.05% slippage. */
    public static BacktestConfig defaults() {
        return new BacktestConfig(
                new BigDecimal("10000"),
                new BigDecimal("1000"),
                0.001,
                0.0005
        );
    }
}
