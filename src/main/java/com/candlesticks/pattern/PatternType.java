package com.candlesticks.pattern;

/** Broad category of what a pattern signals. */
public enum PatternType {
    /** Pattern suggests the current trend may reverse. */
    REVERSAL,
    /** Pattern suggests the current trend will continue. */
    CONTINUATION,
    /** Pattern signals indecision — direction unclear without context. */
    NEUTRAL
}
