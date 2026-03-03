package com.candlesticks.pattern;

/**
 * Descriptive metadata attached to a pattern definition.
 *
 * @param type        broad category (reversal / continuation / neutral)
 * @param direction   directional bias (bullish / bearish / neutral)
 * @param confidence  historical reliability, 0.0 – 1.0
 * @param strength    qualitative reliability label
 * @param description human-readable explanation of what the pattern signals
 */
public record PatternMetadata(
        PatternType      type,
        PatternDirection direction,
        double           confidence,
        PatternStrength  strength,
        String           description
) {}
