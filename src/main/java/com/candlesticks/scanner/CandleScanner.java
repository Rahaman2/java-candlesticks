package com.candlesticks.scanner;

import com.candlesticks.interfaces.ICandle;
import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.PatternDefinition;
import com.candlesticks.pattern.PatternResult;
import com.candlesticks.registry.PatternRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Slides a window over a candlestick series and collects all pattern matches.
 *
 * <p>Converts raw candles to {@link CandleProps} once, then passes windows of the
 * appropriate size to each registered {@link com.candlesticks.pattern.PatternDetector}.</p>
 *
 * <p>Results are sorted chronologically by {@link PatternResult#index()}.</p>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * CandleScanner scanner = BuiltInPatterns.createScanner();
 * List<PatternResult> results = scanner.scan(myCandles);
 * }</pre>
 */
public class CandleScanner {

    private final PatternRegistry registry;

    public CandleScanner(PatternRegistry registry) {
        this.registry = registry;
    }

    /**
     * Scan a series of candles for all registered patterns.
     *
     * @param candles raw OHLCV series (any {@link ICandle} implementation)
     * @return chronologically sorted list of pattern matches
     */
    public List<PatternResult> scan(List<? extends ICandle> candles) {
        List<CandleProps> props = CandleProps.fromList(candles);
        List<PatternResult> results = new ArrayList<>();

        for (PatternDefinition def : registry.getAll()) {
            int paramCount = def.paramCount(); // how many candles the definition needs for defining.
            int upperBound = props.size() - paramCount;

            for (int i = 0; i <= upperBound; i++) {
                CandleProps[] window = props.subList(i, i + paramCount)
                                           .toArray(CandleProps[]::new);
                if (def.detector().matches(window)) {
                    results.add(new PatternResult(
                            i,
                            def.name(),
                            List.copyOf(props.subList(i, i + paramCount)),
                            def.metadata()));
                }
            }
        }

        results.sort(Comparator.comparingInt(PatternResult::index));
        return results;
    }

    /**
     * Scan and filter results to only include patterns with confidence ≥ {@code minConfidence}.
     *
     * @param candles       raw series
     * @param minConfidence threshold 0.0 – 1.0
     */
    public List<PatternResult> scan(List<? extends ICandle> candles, double minConfidence) {
        return scan(candles).stream()
                .filter(r -> r.metadata().confidence() >= minConfidence)
                .toList();
    }
}
