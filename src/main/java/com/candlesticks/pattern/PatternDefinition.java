package com.candlesticks.pattern;

/**
 * Complete description of a registered pattern.
 *
 * @param name       unique identifier used in results (e.g. "hammer", "bullishEngulfing")
 * @param detector   the detection function
 * @param paramCount number of consecutive candles the detector inspects (1 = single-candle, 2 = two-candle, etc.)
 * @param metadata   descriptive metadata (type, direction, confidence, strength, description)
 */
public record PatternDefinition(
        String           name,
        PatternDetector  detector,
        int              paramCount,
        PatternMetadata  metadata
) {
    public PatternDefinition {
        if (name == null || name.isBlank())  throw new IllegalArgumentException("Pattern name must not be blank");
        if (detector == null)                throw new IllegalArgumentException("PatternDetector must not be null");
        if (paramCount < 1 || paramCount > 10) throw new IllegalArgumentException("paramCount must be 1–10");
        if (metadata == null)                throw new IllegalArgumentException("PatternMetadata must not be null");
    }
}
