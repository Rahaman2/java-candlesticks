package com.candlesticks.chart;

import com.candlesticks.Candle;
import com.candlesticks.interfaces.ICandle;
import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.PatternResult;
import com.candlesticks.patterns.BuiltInPatterns;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.stage.Stage;

import java.util.List;

/**
 * JavaFX Application — candlestick chart viewer with pattern overlay.
 *
 * <p>Run: {@code mvn javafx:run}</p>
 */
public class ChartApp extends Application {

    private static final double SCENE_W = 1280.0;
    private static final double SCENE_H = 820.0;

    @Override
    public void start(Stage stage) {
        List<ICandle>     series  = buildDemoSeries();
        List<CandleProps> props   = CandleProps.fromList(series);
        List<PatternResult> results = BuiltInPatterns.createScanner().scan(series);

        // Chart pane auto-fits candles to fill scene width
        CandleChartPane chart = new CandleChartPane(props, results, SCENE_W - 18);

        ScrollPane scroll = new ScrollPane(chart);
        scroll.setFitToWidth(true);   // content stretches to viewport width
        scroll.setFitToHeight(false);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setBackground(new Background(
                new BackgroundFill(ChartTheme.BACKGROUND, CornerRadii.EMPTY, Insets.EMPTY)));
        scroll.setStyle("-fx-background: #131722; -fx-background-color: #131722;");

        Scene scene = new Scene(scroll, SCENE_W, SCENE_H);
        scene.setFill(ChartTheme.BACKGROUND);

        stage.setTitle("Candlestick Pattern Viewer  —  Batch 1 + 2");
        stage.setScene(scene);
        stage.show();

        System.out.printf("Chart: %d candles, %d matches%n", props.size(), results.size());
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    // ── Demo series ───────────────────────────────────────────────────────────

    private static List<ICandle> buildDemoSeries() {
        return List.of(
            // [0–2] Three Black Crows
            new Candle(110.0, 111.0, 103.0, 104.0),
            new Candle(104.0, 104.5,  96.0,  97.0),
            new Candle( 97.0,  97.5,  89.0,  90.0),

            // [2–4] Morning Star
            new Candle( 90.0,  91.0,  88.5,  89.5),
            new Candle( 89.5,  99.0,  89.0,  98.0),

            // [5–6] Bullish Engulfing
            new Candle( 98.0,  99.0,  93.0,  94.0),
            new Candle( 92.0, 102.0,  91.5, 101.0),

            // [7–8] Bearish Harami
            new Candle(101.0, 110.0, 100.5, 109.0),
            new Candle(106.0, 107.5, 104.0, 105.0),

            // [9–11] Three White Soldiers
            new Candle(100.0, 105.0,  99.5, 104.0),
            new Candle(102.0, 108.0, 101.5, 107.0),
            new Candle(105.0, 112.0, 104.5, 111.0),

            // [12] Shooting Star; [12–13] Bearish Engulfing
            new Candle(111.0, 121.0, 110.5, 111.5),
            new Candle(113.0, 114.0, 106.0, 107.0),

            // [14–16] Evening Star
            new Candle(107.0, 116.0, 106.5, 115.0),
            new Candle(115.5, 117.0, 114.0, 115.2),
            new Candle(115.0, 115.5, 105.0, 106.0),

            // [17–18] Piercing Line
            new Candle(106.0, 107.0,  98.0,  99.0),
            new Candle( 97.0, 104.0,  96.5, 103.5),

            // [19–20] Tweezer Bottom
            new Candle(103.5, 105.0,  95.0,  96.0),
            new Candle( 96.0, 103.0,  95.1, 102.0),

            // [21–22] Dark Cloud Cover
            new Candle(102.0, 112.0, 101.5, 111.0),
            new Candle(113.0, 114.0, 104.0, 105.0),

            // [23–24] Bullish Kicker
            new Candle(105.0, 106.0,  98.0,  99.0),
            new Candle(106.0, 114.0, 105.5, 113.0)
        );
    }
}
