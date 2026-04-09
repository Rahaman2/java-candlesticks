package com.candlesticks.chart;

import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.PatternDirection;
import com.candlesticks.pattern.PatternResult;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.candlesticks.chart.ChartTheme.*;

/**
 * TradingView-style candlestick chart with pattern signal overlay.
 *
 * <p>Layout (VBox):</p>
 * <ol>
 *   <li>Chart canvas — candles fill full pane width; clean ▲/▼ markers, no text</li>
 *   <li>Legend canvas — color-coded table of every detected pattern</li>
 * </ol>
 *
 * <p>Candle slot width is computed automatically to fill {@code paneWidth}.</p>
 */
public class CandleChartPane extends VBox {

    private static final double CHART_H      = 490.0;
    private static final double ROW_H        = 22.0;
    private static final double LEGEND_HDR_H = 44.0;
    private static final double LEGEND_PAD   = 14.0;

    // computed in constructor; mutable to support live update()
    private double slotW;
    private double bodyW;
    private double canvasW;
    private final double paneWidth;

    // canvas instances kept for live redraws
    private final Canvas chart;
    private final Canvas legend;

    public CandleChartPane(List<CandleProps> candles, List<PatternResult> results, double paneWidth) {
        this.paneWidth = paneWidth;
        double chartArea = paneWidth - MARGIN_LEFT - MARGIN_RIGHT;
        slotW   = Math.max(chartArea / candles.size(), 6.0);
        bodyW   = Math.max(slotW * 0.68, 2.0);
        canvasW = Math.max(MARGIN_LEFT + candles.size() * slotW + MARGIN_RIGHT, paneWidth);

        setBackground(new Background(new BackgroundFill(BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));
        setPrefWidth(canvasW);

        chart  = new Canvas(canvasW, CHART_H);
        drawChart(chart.getGraphicsContext2D(), candles, results);

        double legendH = LEGEND_HDR_H + results.size() * ROW_H + LEGEND_PAD;
        legend  = new Canvas(canvasW, legendH);
        drawLegend(legend.getGraphicsContext2D(), results, legendH);

        getChildren().addAll(chart, legend);
    }

    /**
     * Refresh the chart with a new candle series and pattern results.
     * Safe to call from the JavaFX Application Thread via {@code Platform.runLater()}.
     */
    public void update(List<CandleProps> candles, List<PatternResult> results) {
        double chartArea = paneWidth - MARGIN_LEFT - MARGIN_RIGHT;
        slotW   = Math.max(chartArea / candles.size(), 6.0);
        bodyW   = Math.max(slotW * 0.68, 2.0);
        canvasW = Math.max(MARGIN_LEFT + candles.size() * slotW + MARGIN_RIGHT, paneWidth);

        setPrefWidth(canvasW);
        chart.setWidth(canvasW);
        drawChart(chart.getGraphicsContext2D(), candles, results);

        double legendH = LEGEND_HDR_H + results.size() * ROW_H + LEGEND_PAD;
        legend.setWidth(canvasW);
        legend.setHeight(legendH);
        drawLegend(legend.getGraphicsContext2D(), results, legendH);
    }

    // ── Chart ─────────────────────────────────────────────────────────────────

    private void drawChart(GraphicsContext gc, List<CandleProps> candles, List<PatternResult> results) {
        double W         = canvasW;
        double H         = CHART_H;
        double chartTop  = MARGIN_TOP;
        double chartBot  = H - MARGIN_BOTTOM;
        double chartH    = chartBot - chartTop;

        // Price range + 8% padding on each side
        double lo  = candles.stream().mapToDouble(CandleProps::low).min().orElse(0);
        double hi  = candles.stream().mapToDouble(CandleProps::high).max().orElse(1);
        double pad = (hi - lo) * 0.08;
        double minP = lo - pad;
        double maxP = hi + pad;

        // ── Background ────────────────────────────────────────────────────────
        gc.setFill(BACKGROUND);
        gc.fillRect(0, 0, W, H);

        // ── Title bar ─────────────────────────────────────────────────────────
        gc.setFill(PANEL_BG);
        gc.fillRect(0, 0, W, MARGIN_TOP - 2);
        gc.setFill(TEXT);
        gc.setFont(Font.font("System", FontWeight.BOLD, TITLE_FONT_SIZE));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText(
            String.format("DEMO  ·  %d candles  ·  %d patterns detected",
                candles.size(), results.size()),
            MARGIN_LEFT, MARGIN_TOP - 12);

        // ── Price grid + right axis ───────────────────────────────────────────
        gc.setFont(Font.font("Monospaced", AXIS_FONT_SIZE));
        gc.setTextAlign(TextAlignment.LEFT);
        double priceStep = (maxP - minP) / GRID_LINES;
        for (int i = 0; i <= GRID_LINES; i++) {
            double price = minP + i * priceStep;
            double y     = py(price, minP, maxP, chartTop, chartH);
            gc.setStroke(GRID);
            gc.setLineWidth(0.5);
            gc.strokeLine(MARGIN_LEFT, y, W - MARGIN_RIGHT, y);
            gc.setFill(AXIS_TEXT);
            gc.fillText(String.format("%.2f", price), W - MARGIN_RIGHT + 6, y + 4);
        }

        // ── Candle bars ───────────────────────────────────────────────────────
        for (int i = 0; i < candles.size(); i++) {
            CandleProps c     = candles.get(i);
            double      cx    = cx(i);
            double      left  = cx - bodyW / 2.0;
            double      highY = py(c.high(),       minP, maxP, chartTop, chartH);
            double      lowY  = py(c.low(),        minP, maxP, chartTop, chartH);
            double      topY  = py(c.bodyTop(),    minP, maxP, chartTop, chartH);
            double      botY  = py(c.bodyBottom(), minP, maxP, chartTop, chartH);
            double      bH    = Math.max(botY - topY, 1.5);
            Color       col   = c.bullish() ? BULL_BODY : BEAR_BODY;

            gc.setStroke(col);
            gc.setLineWidth(1.0);
            gc.strokeLine(cx, highY, cx, lowY);     // wick

            gc.setFill(col);
            gc.fillRect(left, topY, bodyW, bH);     // body
        }

        // ── Pattern markers (grouped per candle index, no text) ───────────────
        Map<Integer, List<PatternResult>> byIdx = results.stream()
            .collect(Collectors.groupingBy(PatternResult::index));

        int ms = Math.max(Math.min((int)(slotW * 0.22), 8), 4);  // marker half-size

        for (Map.Entry<Integer, List<PatternResult>> e : byIdx.entrySet()) {
            int idx = e.getKey();
            if (idx >= candles.size()) continue;
            CandleProps c  = candles.get(idx);
            double      cx = cx(idx);

            List<PatternResult> bulls = e.getValue().stream()
                .filter(r -> r.metadata().direction() == PatternDirection.BULLISH)
                .sorted(Comparator.comparingDouble(r -> -r.metadata().confidence()))
                .toList();
            List<PatternResult> bears = e.getValue().stream()
                .filter(r -> r.metadata().direction() == PatternDirection.BEARISH)
                .sorted(Comparator.comparingDouble(r -> -r.metadata().confidence()))
                .toList();
            List<PatternResult> neutr = e.getValue().stream()
                .filter(r -> r.metadata().direction() == PatternDirection.NEUTRAL)
                .toList();

            if (!bulls.isEmpty()) {
                double lowY = py(c.low(), minP, maxP, chartTop, chartH);
                double tip  = lowY + MARKER_OFFSET;
                double base = tip + ms;
                gc.setFill(BULL_MARKER);
                gc.fillPolygon(
                    new double[]{ cx,      cx - ms, cx + ms },
                    new double[]{ tip,     base,    base    }, 3);
                if (bulls.size() > 1) {
                    gc.setFill(BULL_MARKER);
                    gc.setFont(Font.font("System", FontWeight.BOLD, 8));
                    gc.setTextAlign(TextAlignment.LEFT);
                    gc.fillText("×" + bulls.size(), cx + ms + 1, base);
                }
            }
            if (!bears.isEmpty()) {
                double highY = py(c.high(), minP, maxP, chartTop, chartH);
                double tip   = highY - MARKER_OFFSET;
                double base  = tip - ms;
                gc.setFill(BEAR_MARKER);
                gc.fillPolygon(
                    new double[]{ cx,      cx - ms, cx + ms },
                    new double[]{ tip,     base,    base    }, 3);
                if (bears.size() > 1) {
                    gc.setFill(BEAR_MARKER);
                    gc.setFont(Font.font("System", FontWeight.BOLD, 8));
                    gc.setTextAlign(TextAlignment.LEFT);
                    gc.fillText("×" + bears.size(), cx + ms + 1, base + 8);
                }
            }
            if (!neutr.isEmpty()) {
                double midY = py((c.bodyTop() + c.bodyBottom()) / 2, minP, maxP, chartTop, chartH);
                int ds = ms - 1;
                gc.setFill(NEUTRAL_MARKER);
                gc.fillPolygon(
                    new double[]{ cx,     cx - ds, cx,     cx + ds },
                    new double[]{ midY-ds, midY,   midY+ds, midY   }, 4);
            }
        }

        // ── Bottom index axis ─────────────────────────────────────────────────
        gc.setFill(AXIS_TEXT);
        gc.setFont(Font.font("Monospaced", AXIS_FONT_SIZE));
        gc.setTextAlign(TextAlignment.CENTER);
        int step = Math.max(1, candles.size() / 20);
        for (int i = 0; i < candles.size(); i += step) {
            gc.fillText(String.valueOf(i), cx(i), H - 6);
        }
    }

    // ── Legend ────────────────────────────────────────────────────────────────

    private void drawLegend(GraphicsContext gc, List<PatternResult> results, double h) {
        double W = canvasW;

        gc.setFill(PANEL_BG);
        gc.fillRect(0, 0, W, h);

        // Top border
        gc.setStroke(GRID);
        gc.setLineWidth(1.0);
        gc.strokeLine(0, 0, W, 0);

        // Section title
        gc.setFill(TEXT);
        gc.setFont(Font.font("System", FontWeight.BOLD, 11.5));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("PATTERN SIGNALS", MARGIN_LEFT, 18);

        // Column headers
        double hdrY = LEGEND_HDR_H - 8;
        gc.setFill(AXIS_TEXT);
        gc.setFont(Font.font("System", FontWeight.BOLD, AXIS_FONT_SIZE));
        gc.fillText("IDX",       MARGIN_LEFT,       hdrY);
        gc.fillText("PATTERN",   MARGIN_LEFT + 46,  hdrY);
        gc.fillText("TYPE",      MARGIN_LEFT + 210, hdrY);
        gc.fillText("DIRECTION", MARGIN_LEFT + 290, hdrY);
        gc.fillText("STRENGTH",  MARGIN_LEFT + 378, hdrY);
        gc.fillText("CONF",      MARGIN_LEFT + 462, hdrY);

        gc.setStroke(GRID);
        gc.setLineWidth(0.5);
        gc.strokeLine(MARGIN_LEFT, hdrY + 5, W - MARGIN_RIGHT, hdrY + 5);

        // Data rows
        for (int i = 0; i < results.size(); i++) {
            PatternResult r    = results.get(i);
            double        rowY = LEGEND_HDR_H + (i + 1) * ROW_H;

            // Alternating row tint
            if (i % 2 == 0) {
                gc.setFill(Color.web("#1a1f2e"));
                gc.fillRect(MARGIN_LEFT - 6, rowY - ROW_H + 5,
                            W - MARGIN_LEFT - MARGIN_RIGHT + 12, ROW_H);
            }

            Color col = switch (r.metadata().direction()) {
                case BULLISH -> BULL_MARKER;
                case BEARISH -> BEAR_MARKER;
                default      -> NEUTRAL_MARKER;
            };
            String icon = switch (r.metadata().direction()) {
                case BULLISH -> "▲";
                case BEARISH -> "▼";
                default      -> "◆";
            };

            // Direction icon
            gc.setFill(col);
            gc.setFont(Font.font("System", 9.5));
            gc.setTextAlign(TextAlignment.RIGHT);
            gc.fillText(icon, MARGIN_LEFT - 4, rowY);

            // Index
            gc.setFill(AXIS_TEXT);
            gc.setFont(Font.font("Monospaced", AXIS_FONT_SIZE + 0.5));
            gc.setTextAlign(TextAlignment.LEFT);
            gc.fillText(String.format("[%2d]", r.index()), MARGIN_LEFT, rowY);

            // Pattern name (colored)
            gc.setFill(col);
            gc.fillText(r.patternName(),                       MARGIN_LEFT + 46,  rowY);

            // Type / Direction / Strength (muted)
            gc.setFill(AXIS_TEXT);
            gc.fillText(r.metadata().type().toString(),        MARGIN_LEFT + 210, rowY);
            gc.fillText(r.metadata().direction().toString(),   MARGIN_LEFT + 290, rowY);
            gc.fillText(r.metadata().strength().toString(),    MARGIN_LEFT + 378, rowY);

            // Confidence (colored)
            gc.setFill(col);
            gc.fillText(String.format("%.0f%%", r.metadata().confidence() * 100),
                        MARGIN_LEFT + 462, rowY);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** X center of candle at given index. */
    private double cx(int index) {
        return MARGIN_LEFT + index * slotW + slotW / 2.0;
    }

    /** Map a price to a Y pixel coordinate within the chart area. */
    private double py(double price, double minP, double maxP, double chartTop, double chartH) {
        return chartTop + chartH * (1.0 - (price - minP) / (maxP - minP));
    }
}
