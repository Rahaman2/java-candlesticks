package com.candlesticks.service;

import com.candlesticks.Candle;
import com.candlesticks.binance.SubscriptionKey;
import com.candlesticks.interfaces.ICandle;
import com.candlesticks.pattern.PatternResult;
import com.candlesticks.patterns.BuiltInPatterns;
import com.candlesticks.push.CandleUpdateEvent;
import com.candlesticks.push.LiveUpdatePublisher;
import com.candlesticks.scanner.CandleScanner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central hub for live candlestick data.
 *
 * <p>Maintains a rolling window of {@link Candle} objects per {@link SubscriptionKey}.
 * On each Binance kline tick:
 * <ul>
 *   <li>The partial candle is updated in-place and pushed to browser WebSocket clients.</li>
 *   <li>When the kline <em>closes</em>, a full pattern scan is run and results are
 *       broadcast to the browser and to JavaFX via {@link CandleUpdateEvent}.</li>
 * </ul>
 */
@Service
public class CandleService {

    private final CandleScanner            scanner;
    private final LiveUpdatePublisher      publisher;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${candlesticks.rolling-window-size:500}")
    private int maxWindowSize;

    /** Rolling window per subscription. */
    private final ConcurrentHashMap<SubscriptionKey, List<Candle>>        windows  = new ConcurrentHashMap<>();
    /** Latest pattern scan results per subscription. */
    private final ConcurrentHashMap<SubscriptionKey, List<PatternResult>> patterns = new ConcurrentHashMap<>();
    /** Active subscription keys (used by REST /symbols endpoint). */
    private final Set<SubscriptionKey> activeKeys = ConcurrentHashMap.newKeySet();

    public CandleService(LiveUpdatePublisher publisher, ApplicationEventPublisher eventPublisher) {
        this.scanner        = BuiltInPatterns.createScanner();
        this.publisher      = publisher;
        this.eventPublisher = eventPublisher;
    }

    // ── Subscription management ───────────────────────────────────────────────

    public void addSubscription(SubscriptionKey key) {
        activeKeys.add(key);
        windows.computeIfAbsent(key, k -> Collections.synchronizedList(new ArrayList<>()));
        patterns.put(key, List.of());
    }

    public void removeSubscription(SubscriptionKey key) {
        activeKeys.remove(key);
        windows.remove(key);
        patterns.remove(key);
    }

    public Set<SubscriptionKey> getActiveKeys() {
        return Collections.unmodifiableSet(activeKeys);
    }

    // ── History seeding ───────────────────────────────────────────────────────

    /**
     * Pre-populate the rolling window with historical candles (e.g. from REST API).
     * Does NOT push individual STOMP ticks — only one pattern scan at the end.
     * Called before the live WebSocket connects.
     */
    public void seedHistory(SubscriptionKey key, List<Candle> history) {
        if (history.isEmpty()) return;
        List<Candle> window = windows.computeIfAbsent(
                key, k -> Collections.synchronizedList(new ArrayList<>()));
        synchronized (window) {
            window.clear();
            window.addAll(history);
            while (window.size() > maxWindowSize) window.remove(0);
        }
        // Run initial pattern scan so REST and browser get results immediately
        List<ICandle> snapshot = new ArrayList<>(history);
        List<PatternResult> results = scanner.scan(snapshot);
        patterns.put(key, results);
    }

    // ── Kline ingestion ───────────────────────────────────────────────────────

    /**
     * Called by {@link com.candlesticks.binance.BinanceWsClient} for every kline message.
     *
     * @param symbol   trading pair, e.g. "BTCUSDT"
     * @param interval kline interval, e.g. "1m"
     * @param candle   OHLCV data (may be partial)
     * @param closed   {@code true} when the kline interval has completed
     */
    public void onKline(String symbol, String interval, Candle candle, boolean closed) {
        SubscriptionKey key    = new SubscriptionKey(symbol, interval);
        List<Candle>    window = windows.computeIfAbsent(
                key, k -> Collections.synchronizedList(new ArrayList<>()));

        synchronized (window) {
            if (!window.isEmpty() && window.get(window.size() - 1).timestamp() == candle.timestamp()) {
                window.set(window.size() - 1, candle);   // update partial candle in-place
            } else {
                window.add(candle);                       // new interval started
                while (window.size() > maxWindowSize) {
                    window.remove(0);
                }
            }
        }

        // Push every tick to browser (partial flag = !closed)
        publisher.pushCandle(key, candle, !closed);

        if (closed) {
            List<ICandle> snapshot;
            synchronized (window) {
                snapshot = new ArrayList<>(window);
            }
            List<PatternResult> results = scanner.scan(snapshot);
            patterns.put(key, results);
            publisher.pushPatterns(key, results);

            // Notify JavaFX (dispatched on Spring event thread; JavaFX reads via Platform.runLater)
            eventPublisher.publishEvent(
                    new CandleUpdateEvent(this, key, new ArrayList<>(snapshot.stream()
                            .map(c -> (Candle) c).toList()), results));
        }
    }

    // ── Query methods (used by REST controllers) ──────────────────────────────

    public List<Candle> getCandles(String symbol, String interval, int limit) {
        SubscriptionKey key    = new SubscriptionKey(symbol, interval);
        List<Candle>    window = windows.getOrDefault(key, List.of());
        synchronized (window) {
            List<Candle> copy = new ArrayList<>(window);
            return limit > 0 && limit < copy.size()
                    ? copy.subList(copy.size() - limit, copy.size())
                    : copy;
        }
    }

    public List<PatternResult> getPatterns(String symbol, String interval, double minConfidence) {
        SubscriptionKey key     = new SubscriptionKey(symbol, interval);
        List<PatternResult> all = patterns.getOrDefault(key, List.of());
        return all.stream()
                  .filter(r -> r.metadata().confidence() >= minConfidence)
                  .toList();
    }
}
