package com.candlesticks;

import com.candlesticks.backtest.*;
import com.candlesticks.interfaces.ICandle;
import com.candlesticks.loader.CsvLoader;
import com.candlesticks.loader.JsonLoader;
import com.candlesticks.pattern.*;
import com.candlesticks.patterns.BuiltInPatterns;
import com.candlesticks.registry.PatternRegistry;
import com.candlesticks.scanner.CandleScanner;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Console demo — Batch 1 + 2 (pattern scanner) + Batch 4 (data loader, backtesting).
 *
 * <p>Run:   {@code mvn compile exec:java -Dexec.mainClass=com.candlesticks.Main}</p>
 * <p>Chart: {@code mvn javafx:run}</p>
 */
public class Main {

    public static void main(String[] args) throws Exception {

        // ── Sample series crafted to exercise multi-candle patterns ──────────
        //
        //  [0–2]  Three Black Crows  (also Morning Star spanning [2,3,4])
        //  [2–4]  Morning Star
        //  [5–6]  Bullish Engulfing
        //  [7–8]  Bearish Harami
        //  [9–11] Three White Soldiers
        //  [12]   Shooting Star
        //  [12–13] Bearish Engulfing
        //  [14–16] Evening Star
        //  [17–18] Piercing Line
        //  [19–20] Tweezer Bottom
        //  [21–22] Dark Cloud Cover
        //  [23–24] Bullish Kicker

        List<ICandle> series = List.of(
            // [0–2] Three Black Crows
            new Candle(110.0, 111.0, 103.0, 104.0),   // [0] bearish
            new Candle(104.0, 104.5,  96.0,  97.0),   // [1] bearish, closes lower
            new Candle( 97.0,  97.5,  89.0,  90.0),   // [2] bearish, closes lower again

            // [2–4] Morning Star (c[2] above is the large bear)
            new Candle( 90.0,  91.0,  88.5,  89.5),   // [3] tiny doji-like body
            new Candle( 89.5,  99.0,  89.0,  98.0),   // [4] large bull, closes above midpoint of [2]

            // [5–6] Bullish Engulfing
            new Candle( 98.0,  99.0,  93.0,  94.0),   // [5] bearish
            new Candle( 92.0, 102.0,  91.5, 101.0),   // [6] bullish engulfs [5]

            // [7–8] Bearish Harami
            new Candle(101.0, 110.0, 100.5, 109.0),   // [7] large bullish
            new Candle(106.0, 107.5, 104.0, 105.0),   // [8] small bearish inside [7]

            // [9–11] Three White Soldiers
            new Candle(100.0, 105.0,  99.5, 104.0),   // [9]  bullish
            new Candle(102.0, 108.0, 101.5, 107.0),   // [10] bullish, opens in prev body
            new Candle(105.0, 112.0, 104.5, 111.0),   // [11] bullish, opens in prev body

            // [12] Shooting Star; [12–13] Bearish Engulfing
            new Candle(111.0, 121.0, 110.5, 111.5),   // [12] shooting star (long upper wick, bearish)
            new Candle(113.0, 114.0, 106.0, 107.0),   // [13] bearish engulfs [12]

            // [14–16] Evening Star
            new Candle(107.0, 116.0, 106.5, 115.0),   // [14] large bullish
            new Candle(115.5, 117.0, 114.0, 115.2),   // [15] tiny doji
            new Candle(115.0, 115.5, 105.0, 106.0),   // [16] large bearish, closes below [14] midpoint

            // [17–18] Piercing Line
            new Candle(106.0, 107.0,  98.0,  99.0),   // [17] bearish
            new Candle( 97.0, 104.0,  96.5, 103.5),   // [18] opens below [17].low, closes above midpoint

            // [19–20] Tweezer Bottom
            new Candle(103.5, 105.0,  95.0,  96.0),   // [19] bearish, low=95.0
            new Candle( 96.0, 103.0,  95.1, 102.0),   // [20] bullish, low≈95.1 (matches [19])

            // [21–22] Dark Cloud Cover
            new Candle(102.0, 112.0, 101.5, 111.0),   // [21] large bullish
            new Candle(113.0, 114.0, 104.0, 105.0),   // [22] opens above [21].high, closes below midpoint

            // [23–24] Bullish Kicker
            new Candle(105.0, 106.0,  98.0,  99.0),   // [23] bearish
            new Candle(106.0, 114.0, 105.5, 113.0)    // [24] opens >= prev.open → kicker
        );

        System.out.println("════════════════════════════════════════════════════════════════════");
        System.out.println("  Candlestick Pattern Scanner — Batch 1 + Batch 2");
        System.out.printf( "  Series: %d candles  |  Registered patterns: %d%n",
                series.size(), BuiltInPatterns.createRegistry().size());
        System.out.println("════════════════════════════════════════════════════════════════════");

        // ── Full scan ────────────────────────────────────────────────────────
        CandleScanner scanner = BuiltInPatterns.createScanner();
        List<PatternResult> results = scanner.scan(series);

        System.out.printf("%n%-5s %-26s %-13s %-10s %-10s %-7s  %s%n",
                "Idx", "Pattern", "Type", "Direction", "Strength", "Conf%", "Description");
        System.out.println("─".repeat(110));

        results.forEach(r -> System.out.printf(
                "[%2d] %-26s %-13s %-10s %-10s %5.0f%%  %s%n",
                r.index(),
                r.patternName(),
                r.metadata().type(),
                r.metadata().direction(),
                r.metadata().strength(),
                r.metadata().confidence() * 100,
                r.metadata().description()));

        // ── High-confidence only (≥ 75%) ─────────────────────────────────────
        System.out.println();
        System.out.println("── High-confidence (≥ 75%) ──────────────────────────────────────────");
        List<PatternResult> highConf = scanner.scan(series, 0.75);
        if (highConf.isEmpty()) {
            System.out.println("  None.");
        } else {
            highConf.forEach(r -> System.out.printf(
                    "  [%2d] %-26s  %-8s  %.0f%%%n",
                    r.index(), r.patternName(), r.metadata().direction(),
                    r.metadata().confidence() * 100));
        }

        // ── Custom pattern demo ───────────────────────────────────────────────
        System.out.println();
        System.out.println("── Custom pattern demo ───────────────────────────────────────────────");
        PatternRegistry registry = BuiltInPatterns.createRegistry();
        registry.register(new PatternDefinition(
                "bigBullishBody",
                c -> c[0].range() > 0 && c[0].bodySize() / c[0].range() > 0.75 && c[0].bullish(),
                1,
                new PatternMetadata(
                        PatternType.CONTINUATION,
                        PatternDirection.BULLISH,
                        0.70,
                        PatternStrength.MODERATE,
                        "Large bullish body — custom continuation signal")));

        new CandleScanner(registry).scan(series).stream()
                .filter(r -> r.patternName().equals("bigBullishBody"))
                .forEach(r -> System.out.printf("  [%2d] %s%n", r.index(), r.patternName()));

        System.out.println();
        System.out.printf("Registered patterns: %d  |  Matches found: %d%n",
                registry.size(), results.size());

        // ══════════════════════════════════════════════════════════════════════
        //  Batch 4 — Data Loader + Backtesting
        // ══════════════════════════════════════════════════════════════════════

        System.out.println();
        System.out.println("════════════════════════════════════════════════════════════════════");
        System.out.println("  Batch 4 — Data Loader & Backtesting");
        System.out.println("════════════════════════════════════════════════════════════════════");

        // ── 4a. Load from CSV ─────────────────────────────────────────────────
        Path csvPath     = resourcePath("sample.csv");
        List<Candle> csvCandles  = CsvLoader.load(csvPath);
        System.out.printf("%nCSV  loader : %d candles from %s%n", csvCandles.size(), csvPath.getFileName());

        // ── 4b. Load from JSON (CCXT format) ──────────────────────────────────
        Path jsonPath    = resourcePath("sample.json");
        List<Candle> jsonCandles = JsonLoader.load(jsonPath);
        System.out.printf("JSON loader : %d candles from %s%n", jsonCandles.size(), jsonPath.getFileName());

        // ── 4c. Simple pattern-based strategy ────────────────────────────────
        //
        //   BUY  — any BULLISH pattern, conf ≥ 0.70
        //   SELL — any BEARISH pattern, conf ≥ 0.70
        //   HOLD — otherwise
        //
        Strategy patternStrategy = (patterns, candle) -> {
            boolean bullish = patterns.stream().anyMatch(p ->
                    p.metadata().direction() == PatternDirection.BULLISH
                    && p.metadata().confidence() >= 0.70);
            boolean bearish = patterns.stream().anyMatch(p ->
                    p.metadata().direction() == PatternDirection.BEARISH
                    && p.metadata().confidence() >= 0.70);
            if (bullish) return Signal.BUY;
            if (bearish) return Signal.SELL;
            return Signal.HOLD;
        };

        BacktestRunner runner = new BacktestRunner(
                BuiltInPatterns.createScanner(),
                BacktestConfig.defaults());

        System.out.println();
        System.out.println("── Strategy: pattern-based (conf ≥ 70%)  —  CSV data ───────────────");
        runner.run(csvCandles, patternStrategy).print();

        // ── 4d. Compare two strategies concurrently via Virtual Threads ───────
        //
        //   Strategy A: conf ≥ 0.60   (more trades, lower bar)
        //   Strategy B: conf ≥ 0.75   (fewer trades, higher conviction)
        //
        Strategy stratA = (patterns, candle) -> {
            if (patterns.stream().anyMatch(p ->
                    p.metadata().direction() == PatternDirection.BULLISH
                    && p.metadata().confidence() >= 0.60)) return Signal.BUY;
            if (patterns.stream().anyMatch(p ->
                    p.metadata().direction() == PatternDirection.BEARISH
                    && p.metadata().confidence() >= 0.60)) return Signal.SELL;
            return Signal.HOLD;
        };

        Strategy stratB = (patterns, candle) -> {
            if (patterns.stream().anyMatch(p ->
                    p.metadata().direction() == PatternDirection.BULLISH
                    && p.metadata().confidence() >= 0.75)) return Signal.BUY;
            if (patterns.stream().anyMatch(p ->
                    p.metadata().direction() == PatternDirection.BEARISH
                    && p.metadata().confidence() >= 0.75)) return Signal.SELL;
            return Signal.HOLD;
        };

        System.out.println();
        System.out.println("── Multi-strategy comparison (Virtual Threads)  —  JSON data ────────");
        List<BacktestResult> multiResults = runner.runAll(jsonCandles, List.of(stratA, stratB));

        String[] names = {"Strategy A (conf ≥ 60%)", "Strategy B (conf ≥ 75%)"};
        for (int i = 0; i < multiResults.size(); i++) {
            System.out.printf("%n  %s%n", names[i]);
            multiResults.get(i).print();
        }

        System.out.println();
        System.out.println("Run 'mvn javafx:run' to open the chart viewer.");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Resolve a resource file packaged in src/main/resources. */
    private static Path resourcePath(String name) throws Exception {
        URI uri = Main.class.getClassLoader().getResource(name).toURI();
        return Paths.get(uri);
    }
}
