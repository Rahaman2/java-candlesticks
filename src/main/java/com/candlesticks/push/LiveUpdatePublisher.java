package com.candlesticks.push;

import com.candlesticks.Candle;
import com.candlesticks.binance.SubscriptionKey;
import com.candlesticks.pattern.PatternResult;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Pushes live data to browser WebSocket clients via STOMP.
 *
 * <p>Topics:</p>
 * <ul>
 *   <li>{@code /topic/candles/{symbol}_{interval}} — every Binance kline tick</li>
 *   <li>{@code /topic/patterns/{symbol}_{interval}} — on candle close only</li>
 *   <li>{@code /topic/subscriptions} — when a symbol is added or removed</li>
 * </ul>
 */
@Component
public class LiveUpdatePublisher {

    private final SimpMessagingTemplate messaging;

    public LiveUpdatePublisher(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    /** Push a single candle tick to all browser subscribers. */
    public void pushCandle(SubscriptionKey key, Candle candle, boolean partial) {
        messaging.convertAndSend(
                "/topic/candles/" + key.toTopicKey(),
                Map.of(
                    "ts",      candle.timestamp(),
                    "o",       candle.open(),
                    "h",       candle.high(),
                    "l",       candle.low(),
                    "c",       candle.close(),
                    "v",       candle.volume(),
                    "partial", partial
                ));
    }

    /** Push the full updated pattern list after a kline closes. */
    public void pushPatterns(SubscriptionKey key, List<PatternResult> results) {
        List<Map<String, Object>> payload = results.stream()
                .map(r -> Map.<String, Object>of(
                        "index",      r.index(),
                        "name",       r.patternName(),
                        "type",       r.metadata().type().name(),
                        "direction",  r.metadata().direction().name(),
                        "strength",   r.metadata().strength().name(),
                        "confidence", r.metadata().confidence()
                ))
                .toList();
        messaging.convertAndSend("/topic/patterns/" + key.toTopicKey(), payload);
    }

    /** Notify all clients that the subscription list changed. */
    public void pushSubscriptions(List<String> keys) {
        messaging.convertAndSend("/topic/subscriptions", keys);
    }
}
