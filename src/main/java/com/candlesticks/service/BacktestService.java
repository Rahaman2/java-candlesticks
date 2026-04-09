package com.candlesticks.service;

import com.candlesticks.Candle;
import com.candlesticks.backtest.BacktestConfig;
import com.candlesticks.backtest.BacktestResult;
import com.candlesticks.backtest.BacktestRunner;
import com.candlesticks.backtest.Signal;
import com.candlesticks.backtest.Strategy;
import com.candlesticks.pattern.PatternDirection;
import com.candlesticks.patterns.BuiltInPatterns;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Spring wrapper around {@link BacktestRunner}.
 * Provides named built-in strategies consumable via REST.
 */
@Service
public class BacktestService {

    private final CandleService candleService;

    /** Named strategies available via {@code POST /api/v1/backtest}. */
    private static final Map<String, Strategy> STRATEGIES = Map.of(
            "BULLISH_CONF_60", (patterns, candle) ->
                    patterns.stream().anyMatch(r ->
                            r.metadata().direction() == PatternDirection.BULLISH
                            && r.metadata().confidence() >= 0.60)
                    ? Signal.BUY : Signal.HOLD,

            "BULLISH_CONF_70", (patterns, candle) ->
                    patterns.stream().anyMatch(r ->
                            r.metadata().direction() == PatternDirection.BULLISH
                            && r.metadata().confidence() >= 0.70)
                    ? Signal.BUY : Signal.HOLD,

            "BULLISH_CONF_75", (patterns, candle) ->
                    patterns.stream().anyMatch(r ->
                            r.metadata().direction() == PatternDirection.BULLISH
                            && r.metadata().confidence() >= 0.75)
                    ? Signal.BUY : Signal.HOLD,

            "BEARISH_CONF_70", (patterns, candle) ->
                    patterns.stream().anyMatch(r ->
                            r.metadata().direction() == PatternDirection.BEARISH
                            && r.metadata().confidence() >= 0.70)
                    ? Signal.SELL : Signal.HOLD
    );

    public BacktestService(CandleService candleService) {
        this.candleService = candleService;
    }

    /**
     * Run a named strategy on the current rolling window for the given symbol+interval.
     *
     * @param symbol       e.g. "BTCUSDT"
     * @param interval     e.g. "1m"
     * @param strategyName one of the keys in {@link #STRATEGIES}
     * @param config       backtest configuration (or {@code null} for defaults)
     * @return backtest result
     * @throws IllegalArgumentException if the strategy name is unknown or no data available
     */
    public BacktestResult run(String symbol, String interval,
                              String strategyName, BacktestConfig config) {
        Strategy strategy = STRATEGIES.get(strategyName);
        if (strategy == null) {
            throw new IllegalArgumentException("Unknown strategy: " + strategyName
                    + ". Available: " + STRATEGIES.keySet());
        }

        List<Candle> candles = candleService.getCandles(symbol, interval, 0);
        if (candles.isEmpty()) {
            throw new IllegalStateException("No candle data for " + symbol + "/" + interval);
        }

        BacktestRunner runner = new BacktestRunner(
                BuiltInPatterns.createScanner(),
                config != null ? config : BacktestConfig.defaults());
        return runner.run(candles, strategy);
    }

    public List<String> availableStrategies() {
        return STRATEGIES.keySet().stream().sorted().toList();
    }
}
