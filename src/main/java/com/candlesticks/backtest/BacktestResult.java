package com.candlesticks.backtest;

import java.util.List;

/**
 * The full output of a {@link BacktestRunner} run: every trade plus aggregate stats.
 *
 * @param trades ordered list of completed trades (chronological)
 * @param stats  aggregate performance metrics
 */
public record BacktestResult(
        List<Trade>    trades,
        BacktestStats  stats
) {
    /** Print a formatted summary to stdout. */
    public void print() {
        BacktestStats s = stats;
        System.out.println("── Backtest Result ──────────────────────────────────────────────────");
        System.out.printf("  Trades      : %d  (W: %d  L: %d)%n",
                s.totalTrades(), s.winningTrades(), s.losingTrades());
        System.out.printf("  Win Rate    : %s%%%n",   s.winRate());
        System.out.printf("  Total P&L   : %s%n",     s.totalPnl());
        System.out.printf("  Max Drawdown: %s%%%n",   s.maxDrawdown());
        System.out.printf("  Profit Factor: %s%n",    s.profitFactor());
        System.out.printf("  Final Equity: %s%n",     s.finalEquity());
        System.out.println("─────────────────────────────────────────────────────────────────────");

        if (trades.isEmpty()) return;

        System.out.printf("  %-6s %-6s %-12s %-12s %-10s%n",
                "Entry", "Exit", "EntryPrice", "ExitPrice", "P&L");
        trades.forEach(t -> System.out.printf("  [%3d]  [%3d]  %10.4f  %10.4f  %s%n",
                t.entryIndex(), t.exitIndex(),
                t.entryPrice(), t.exitPrice(),
                t.pnl()));
    }
}
