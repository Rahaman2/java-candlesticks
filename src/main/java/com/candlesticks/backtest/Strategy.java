package com.candlesticks.backtest;

import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.PatternResult;

import java.util.List;

/**
 * A trading strategy evaluated candle-by-candle during a backtest.
 *
 * <p>The {@link BacktestRunner} calls {@code evaluate} once per candle, passing:</p>
 * <ul>
 *   <li>{@code patternsHere} — all patterns whose <em>last</em> candle is the current one</li>
 *   <li>{@code candle}       — enriched properties of the current candle</li>
 * </ul>
 *
 * <p>The strategy returns a {@link Signal}; position tracking is handled by the runner.
 * Strategies should be stateless so that {@link BacktestRunner#runAll} can safely
 * execute multiple strategies concurrently on virtual threads.</p>
 *
 * <h3>Example — pattern-based strategy</h3>
 * <pre>{@code
 * Strategy patternStrategy = (patterns, candle) -> {
 *     boolean hasBullish = patterns.stream()
 *         .anyMatch(p -> p.metadata().direction() == PatternDirection.BULLISH
 *                     && p.metadata().confidence() >= 0.70);
 *     boolean hasBearish = patterns.stream()
 *         .anyMatch(p -> p.metadata().direction() == PatternDirection.BEARISH
 *                     && p.metadata().confidence() >= 0.70);
 *     if (hasBullish) return Signal.BUY;
 *     if (hasBearish) return Signal.SELL;
 *     return Signal.HOLD;
 * };
 * }</pre>
 */
@FunctionalInterface
public interface Strategy {
    Signal evaluate(List<PatternResult> patternsHere, CandleProps candle);
}
