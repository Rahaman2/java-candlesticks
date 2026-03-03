package com.candlesticks.patterns;

import com.candlesticks.patterns.multi.*;
import com.candlesticks.patterns.single.*;
import com.candlesticks.registry.PatternRegistry;
import com.candlesticks.scanner.CandleScanner;

/**
 * Wires all built-in patterns into a ready-to-use scanner.
 *
 * <p>Registration order:</p>
 * <ol>
 *   <li><strong>Batch 1 — Single-candle:</strong> Doji, Hammer, InvertedHammer, Marubozu, SpinningTop</li>
 *   <li><strong>Batch 2 — Context single-candle:</strong> HangingMan, ShootingStar</li>
 *   <li><strong>Batch 2 — Two-candle:</strong> BullishEngulfing, BearishEngulfing,
 *       BullishHarami, BearishHarami, BullishKicker, BearishKicker, PiercingLine,
 *       DarkCloudCover, TweezerBottom, TweezerTop</li>
 *   <li><strong>Batch 2 — Three-candle:</strong> MorningStar, EveningStar,
 *       ThreeWhiteSoldiers, ThreeBlackCrows</li>
 * </ol>
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * // Option A — use the scanner directly
 * CandleScanner scanner = BuiltInPatterns.createScanner();
 * List<PatternResult> results = scanner.scan(myCandles);
 *
 * // Option B — extend with custom patterns
 * PatternRegistry registry = BuiltInPatterns.createRegistry();
 * registry.register(myCustomDefinition);
 * CandleScanner scanner = new CandleScanner(registry);
 * }</pre>
 */
public final class BuiltInPatterns {

    private BuiltInPatterns() {}

    /**
     * Build a {@link PatternRegistry} pre-loaded with all built-in patterns.
     * You can add your own patterns to this registry before passing it to a scanner.
     */
    public static PatternRegistry createRegistry() {
        PatternRegistry registry = new PatternRegistry();

        // ── Batch 1: Single-candle ────────────────────────────────────────────

        registry.register(Doji.definition());
        registry.register(Doji.bullishDefinition());
        registry.register(Doji.bearishDefinition());

        registry.register(Hammer.definition());
        registry.register(Hammer.bullishDefinition());
        registry.register(Hammer.bearishDefinition());

        registry.register(InvertedHammer.definition());
        registry.register(InvertedHammer.bullishDefinition());
        registry.register(InvertedHammer.bearishDefinition());

        registry.register(Marubozu.definition());
        registry.register(Marubozu.bullishDefinition());
        registry.register(Marubozu.bearishDefinition());

        registry.register(SpinningTop.definition());
        registry.register(SpinningTop.bullishDefinition());
        registry.register(SpinningTop.bearishDefinition());

        // ── Batch 2: Context single-candle (bearish-body shape detector) ─────

        registry.register(HangingMan.definition());
        registry.register(ShootingStar.definition());

        // ── Batch 2: Two-candle ───────────────────────────────────────────────

        registry.register(BullishEngulfing.definition());
        registry.register(BearishEngulfing.definition());

        registry.register(BullishHarami.definition());
        registry.register(BearishHarami.definition());

        registry.register(BullishKicker.definition());
        registry.register(BearishKicker.definition());

        registry.register(PiercingLine.definition());
        registry.register(DarkCloudCover.definition());

        registry.register(TweezerBottom.definition());
        registry.register(TweezerTop.definition());

        // ── Batch 2: Three-candle ─────────────────────────────────────────────

        registry.register(MorningStar.definition());
        registry.register(EveningStar.definition());

        registry.register(ThreeWhiteSoldiers.definition());
        registry.register(ThreeBlackCrows.definition());

        return registry;
    }

    /**
     * Create a {@link CandleScanner} pre-loaded with all built-in patterns.
     * This is the quickest way to get started.
     */
    public static CandleScanner createScanner() {
        return new CandleScanner(createRegistry());
    }
}
