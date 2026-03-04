package com.candlesticks.backtest;

import com.candlesticks.interfaces.ICandle;
import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.PatternResult;
import com.candlesticks.scanner.CandleScanner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Runs a {@link Strategy} over a historical candle series and produces a {@link BacktestResult}.
 *
 * <h3>Execution model</h3>
 * <ol>
 *   <li>All patterns are detected upfront via {@link CandleScanner}.</li>
 *   <li>Patterns are grouped by their <em>completion index</em>
 *       ({@code startIndex + candleCount - 1}).</li>
 *   <li>For each candle the strategy receives patterns that completed there
 *       and returns a {@link Signal}.</li>
 *   <li>Entry is filled at the close of the signal candle (+ slippage).
 *       Exit is filled at the close of the exit candle (- slippage).</li>
 * </ol>
 *
 * <h3>P&amp;L precision</h3>
 * All monetary arithmetic uses {@link BigDecimal} with {@link RoundingMode#HALF_UP}
 * (audit checklist §4 — never {@code double} for financial calculations).
 *
 * <h3>Concurrency</h3>
 * {@link #runAll} executes multiple strategies concurrently using
 * <strong>Virtual Threads</strong> ({@code Executors.newVirtualThreadPerTaskExecutor()})
 * per the 2026 audit checklist §1.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * BacktestRunner runner = new BacktestRunner(BuiltInPatterns.createScanner());
 * BacktestResult result = runner.run(candles, myStrategy);
 * result.print();
 * }</pre>
 */
public class BacktestRunner {

    private static final int SCALE = 8;

    private final CandleScanner scanner;
    private final BacktestConfig config;

    public BacktestRunner(CandleScanner scanner) {
        this(scanner, BacktestConfig.defaults());
    }

    public BacktestRunner(CandleScanner scanner, BacktestConfig config) {
        this.scanner = scanner;
        this.config  = config;
    }

    // ── Single strategy ───────────────────────────────────────────────────────

    /**
     * Run one strategy over the series.
     *
     * @param candles  historical OHLCV data (any {@link ICandle} implementation)
     * @param strategy stateless signal function
     * @return completed backtest result
     */
    public BacktestResult run(List<? extends ICandle> candles, Strategy strategy) {
        List<CandleProps> props       = CandleProps.fromList(candles);
        List<PatternResult> allPat    = scanner.scan(candles);
        Map<Integer, List<PatternResult>> byIndex = groupByCompletion(allPat);

        List<Trade>    trades   = new ArrayList<>();
        BigDecimal     equity   = config.initialCapital();
        BigDecimal     maxEq    = equity;
        BigDecimal     maxDD    = BigDecimal.ZERO;

        boolean    inLong    = false;
        int        entryIdx  = -1;
        BigDecimal entryFill = BigDecimal.ZERO;

        for (int i = 0; i < props.size(); i++) {
            CandleProps c = props.get(i);
            List<PatternResult> here = byIndex.getOrDefault(i, List.of());
            Signal signal = strategy.evaluate(here, c);

            if (signal == Signal.BUY && !inLong) {
                inLong    = true;
                entryIdx  = i;
                entryFill = fillPrice(c.close(), +config.slippage());

            } else if (signal == Signal.SELL && inLong) {
                BigDecimal exitFill = fillPrice(c.close(), -config.slippage());
                BigDecimal pnl      = calcPnl(entryFill, exitFill);

                trades.add(new Trade(entryIdx, i, entryFill.doubleValue(),
                        exitFill.doubleValue(), pnl, pnl.signum() > 0));

                equity = equity.add(pnl);
                maxEq  = equity.max(maxEq);
                BigDecimal dd = drawdownPct(maxEq, equity);
                if (dd.compareTo(maxDD) > 0) maxDD = dd;

                inLong = false;
            }
        }

        // Force-close any open position at the last candle's close
        if (inLong) {
            CandleProps last    = props.get(props.size() - 1);
            BigDecimal exitFill = fillPrice(last.close(), -config.slippage());
            BigDecimal pnl      = calcPnl(entryFill, exitFill);
            trades.add(new Trade(entryIdx, props.size() - 1,
                    entryFill.doubleValue(), exitFill.doubleValue(),
                    pnl, pnl.signum() > 0));
            equity = equity.add(pnl);
        }

        return new BacktestResult(List.copyOf(trades), computeStats(trades, equity, maxDD));
    }

    // ── Multiple strategies (Virtual Threads) ─────────────────────────────────

    /**
     * Run multiple strategies concurrently using <strong>Virtual Threads</strong>
     * (Java 21+, audit checklist §1).
     *
     * <p>Each strategy runs independently; results are returned in the same order
     * as the input list.</p>
     *
     * @param candles    historical series
     * @param strategies list of stateless strategies to evaluate
     * @return one {@link BacktestResult} per strategy, in input order
     */
    public List<BacktestResult> runAll(List<? extends ICandle> candles,
                                       List<Strategy> strategies) {
        // Virtual threads — one per strategy; scales to thousands without pool exhaustion
        try (ExecutorService vt = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<BacktestResult>> futures = strategies.stream()
                    .map(s -> vt.submit(() -> run(candles, s)))
                    .toList();

            return futures.stream().map(f -> {
                try {
                    return f.get();
                } catch (Exception e) {
                    throw new RuntimeException("Strategy execution failed", e);
                }
            }).toList();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Group pattern results by the index of their last candle. */
    private static Map<Integer, List<PatternResult>> groupByCompletion(
            List<PatternResult> patterns) {
        Map<Integer, List<PatternResult>> map = new HashMap<>();
        for (PatternResult r : patterns) {
            int completionIndex = r.index() + r.candles().size() - 1;
            map.computeIfAbsent(completionIndex, k -> new ArrayList<>()).add(r);
        }
        return map;
    }

    /**
     * Apply slippage to a raw price.
     *
     * @param rawPrice closing price
     * @param fraction positive to add (buy), negative to subtract (sell)
     */
    private static BigDecimal fillPrice(double rawPrice, double fraction) {
        BigDecimal price = BigDecimal.valueOf(rawPrice);
        BigDecimal slip  = price.multiply(BigDecimal.valueOf(Math.abs(fraction)))
                                .setScale(SCALE, RoundingMode.HALF_UP);
        return fraction >= 0 ? price.add(slip) : price.subtract(slip);
    }

    /**
     * Net P&amp;L for a long trade after commission on both legs.
     *
     * <p>Uses BigDecimal throughout (audit checklist §4).</p>
     */
    private BigDecimal calcPnl(BigDecimal entry, BigDecimal exit) {
        BigDecimal comm  = BigDecimal.valueOf(config.commissionRate());
        BigDecimal units = config.positionSize().divide(entry, SCALE, RoundingMode.HALF_UP);

        BigDecimal gross = exit.subtract(entry).multiply(units);
        BigDecimal entryComm = config.positionSize().multiply(comm);
        BigDecimal exitComm  = exit.multiply(units).multiply(comm);

        return gross.subtract(entryComm).subtract(exitComm)
                    .setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal drawdownPct(BigDecimal peak, BigDecimal current) {
        if (peak.signum() == 0) return BigDecimal.ZERO;
        return peak.subtract(current)
                   .divide(peak, 4, RoundingMode.HALF_UP)
                   .multiply(BigDecimal.valueOf(100))
                   .setScale(2, RoundingMode.HALF_UP);
    }

    private static BacktestStats computeStats(List<Trade> trades,
                                               BigDecimal finalEquity,
                                               BigDecimal maxDD) {
        int wins = (int) trades.stream().filter(Trade::isWin).count();
        int total = trades.size();

        BigDecimal totalPnl = trades.stream()
                .map(Trade::pnl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal winRate = total == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(wins * 100.0 / total)
                            .setScale(1, RoundingMode.HALF_UP);

        BigDecimal grossProfit = trades.stream()
                .filter(Trade::isWin).map(Trade::pnl)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal grossLoss = trades.stream()
                .filter(t -> !t.isWin()).map(Trade::pnl)
                .reduce(BigDecimal.ZERO, BigDecimal::add).abs();

        BigDecimal profitFactor = grossLoss.signum() == 0
                ? new BigDecimal("999")
                : grossProfit.divide(grossLoss, 2, RoundingMode.HALF_UP);

        return new BacktestStats(total, wins, total - wins,
                totalPnl, winRate, maxDD, profitFactor, finalEquity);
    }
}
