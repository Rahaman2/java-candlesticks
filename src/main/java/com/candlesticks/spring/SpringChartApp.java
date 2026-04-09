package com.candlesticks.spring;

import com.candlesticks.chart.CandleChartPane;
import com.candlesticks.model.CandleProps;
import com.candlesticks.pattern.PatternResult;
import com.candlesticks.push.CandleUpdateEvent;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;

/**
 * JavaFX entry point for the Spring-managed candlestick chart.
 *
 * <p>Bootstrapped by {@link SpringCandleApplication#main} after the Spring context
 * is fully initialised. Uses {@link SpringContext} to retrieve the {@link CandleService}
 * bean without requiring JavaFX to be a Spring bean itself.</p>
 *
 * <p>Live chart updates arrive via {@link CandleUpdateEvent} (fired by
 * {@link CandleService} on each kline close). The event listener calls
 * {@code Platform.runLater()} to safely update the JavaFX canvas.</p>
 */
public class SpringChartApp extends Application {

    private static final double PANE_WIDTH = 1240.0;

    private CandleChartPane chartPane;
    private ScrollPane scrollPane;

    @Override
    public void start(Stage stage) {
        // Build initial chart with whatever data is already available
        List<CandleProps> initialProps   = List.of();
        List<PatternResult> initialPats  = List.of();

        chartPane  = new CandleChartPane(
                initialProps.isEmpty() ? placeholderProps() : initialProps,
                initialPats, PANE_WIDTH);

        scrollPane = new ScrollPane(chartPane);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setFitToHeight(false);
        scrollPane.setBackground(new Background(
                new BackgroundFill(Color.web("#131722"), CornerRadii.EMPTY, Insets.EMPTY)));

        VBox root = new VBox(scrollPane);
        root.setBackground(new Background(
                new BackgroundFill(Color.web("#131722"), CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(root, 1280, 820);
        stage.setTitle("Candlesticks — Live");
        stage.setScene(scene);
        stage.show();

        // Register live update listener with Spring context
        ((ConfigurableApplicationContext) SpringContext.get()).addApplicationListener(
                (ApplicationListener<CandleUpdateEvent>) event -> {
                    List<CandleProps> props = CandleProps.fromList(event.candles());
                    List<PatternResult> pats = event.patterns();
                    Platform.runLater(() -> {
                        chartPane.update(props, pats);
                        scrollPane.setHvalue(1.0);  // auto-scroll to latest candle
                    });
                });
    }

    /** Placeholder so the chart renders something before any live data arrives. */
    private List<CandleProps> placeholderProps() {
        return List.of(CandleProps.from(
                new com.candlesticks.Candle(0L, 100, 105, 95, 102, 0)));
    }
}
