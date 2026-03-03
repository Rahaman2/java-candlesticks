package com.candlesticks.chart;

import javafx.scene.paint.Color;

/**
 * TradingView-style dark theme color palette and layout constants for the candlestick chart.
 */
public final class ChartTheme {

    private ChartTheme() {}

    // ── Colors ────────────────────────────────────────────────────────────────

    public static final Color BACKGROUND   = Color.web("#131722");
    public static final Color PANEL_BG     = Color.web("#1e222d");
    public static final Color GRID         = Color.web("#2a2e39");
    public static final Color TEXT         = Color.web("#d1d4dc");
    public static final Color AXIS_TEXT    = Color.web("#787b86");

    public static final Color BULL_BODY    = Color.web("#26a69a");   // teal green
    public static final Color BULL_WICK    = Color.web("#26a69a");
    public static final Color BEAR_BODY    = Color.web("#ef5350");   // red
    public static final Color BEAR_WICK    = Color.web("#ef5350");

    public static final Color BULL_MARKER  = Color.web("#26a69a");   // green arrow ▲
    public static final Color BEAR_MARKER  = Color.web("#ef5350");   // red arrow ▼
    public static final Color NEUTRAL_MARKER = Color.web("#f0b429"); // amber

    // ── Layout ────────────────────────────────────────────────────────────────

    /** Width of each candle body in pixels. */
    public static final int CANDLE_WIDTH   = 12;

    /** Gap between candle body and the edge of its slot. */
    public static final int CANDLE_GAP     = 3;

    /** Total horizontal slot width per candle: body + two gaps. */
    public static final int SLOT_WIDTH     = CANDLE_WIDTH + CANDLE_GAP * 2;

    public static final int MARGIN_LEFT    = 18;
    public static final int MARGIN_RIGHT   = 80;
    public static final int MARGIN_TOP     = 50;
    public static final int MARGIN_BOTTOM  = 30;

    /** Number of horizontal grid / price-axis lines. */
    public static final int GRID_LINES     = 6;

    /** Pixels below the candle low where a bullish marker triangle is drawn. */
    public static final int MARKER_OFFSET  = 8;

    /** Half-width of the marker triangle. */
    public static final int MARKER_SIZE    = 6;

    /** Font size for axis labels. */
    public static final double AXIS_FONT_SIZE    = 10.0;

    /** Font size for pattern labels on markers. */
    public static final double LABEL_FONT_SIZE   = 9.0;

    /** Font size for the chart title. */
    public static final double TITLE_FONT_SIZE   = 13.0;
}
