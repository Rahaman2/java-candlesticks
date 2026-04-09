package com.candlesticks.push;

import com.candlesticks.Candle;
import com.candlesticks.binance.SubscriptionKey;
import com.candlesticks.pattern.PatternResult;
import org.springframework.context.ApplicationEvent;

import java.util.List;

/**
 * Spring {@link ApplicationEvent} published by {@link com.candlesticks.service.CandleService}
 * whenever a kline closes. Contains a snapshot of the full rolling window + fresh pattern results.
 *
 * <p>The JavaFX {@link com.candlesticks.spring.SpringChartApp} listens for this event
 * and redraws the chart via {@code Platform.runLater()}.</p>
 */
public class CandleUpdateEvent extends ApplicationEvent {

    private final SubscriptionKey    key;
    private final List<Candle>       candles;
    private final List<PatternResult> patterns;

    public CandleUpdateEvent(Object source,
                             SubscriptionKey key,
                             List<Candle> candles,
                             List<PatternResult> patterns) {
        super(source);
        this.key      = key;
        this.candles  = candles;
        this.patterns = patterns;
    }

    public SubscriptionKey     key()      { return key; }
    public List<Candle>        candles()  { return candles; }
    public List<PatternResult> patterns() { return patterns; }
}
