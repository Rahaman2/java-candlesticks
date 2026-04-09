package com.candlesticks.binance;

import com.candlesticks.Candle;
import com.candlesticks.service.CandleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Connects to the Binance kline WebSocket stream for each subscribed symbol+interval.
 *
 * <p>Stream URL: {@code wss://stream.binance.com:9443/ws/{symbol}@kline_{interval}}</p>
 *
 * <p>One WebSocket connection is maintained per {@link SubscriptionKey}.
 * On disconnect, the client automatically reconnects after 5 seconds.</p>
 */
@Component
public class BinanceWsClient {

    private static final Logger log = Logger.getLogger(BinanceWsClient.class.getName());

    private final CandleService      candleService;
    private final BinanceRestClient  restClient;
    private final ObjectMapper       objectMapper;

    @Value("${candlesticks.binance.ws-url:wss://stream.binance.com:9443/ws}")
    private String wsBaseUrl;

    @Value("${candlesticks.history-limit:42}")
    private int historyLimit;

    private final Map<SubscriptionKey, WebSocketClient> connections = new ConcurrentHashMap<>();

    public BinanceWsClient(CandleService candleService, BinanceRestClient restClient, ObjectMapper objectMapper) {
        this.candleService = candleService;
        this.restClient    = restClient;
        this.objectMapper  = objectMapper;
    }

    /**
     * Seed historical candles from REST, then open a live WebSocket stream.
     * Silently ignored if already subscribed.
     */
    public void subscribe(String symbol, String interval) {
        SubscriptionKey key = new SubscriptionKey(symbol.toUpperCase(), interval);
        if (connections.containsKey(key)) return;
        candleService.addSubscription(key);

        // Fetch and seed history before connecting live stream
        var history = restClient.fetchHistory(symbol, interval, historyLimit);
        candleService.seedHistory(key, history);
        log.info("Seeded " + history.size() + " historical candles for " + key.toTopicKey());

        connect(key);
    }

    /** Close the WebSocket connection for this symbol+interval. */
    public void unsubscribe(String symbol, String interval) {
        SubscriptionKey key = new SubscriptionKey(symbol.toUpperCase(), interval);
        WebSocketClient ws  = connections.remove(key);
        if (ws != null) ws.close();
        candleService.removeSubscription(key);
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void connect(SubscriptionKey key) {
        String url = wsBaseUrl + "/" + key.symbol().toLowerCase() + "@kline_" + key.interval();
        WebSocketClient client = new WebSocketClient(URI.create(url)) {

            @Override
            public void onOpen(ServerHandshake handshake) {
                log.info("Binance WS connected: " + key.toTopicKey());
            }

            @Override
            public void onMessage(String message) {
                try {
                    BinanceKlineMessage msg = objectMapper.readValue(message, BinanceKlineMessage.class);
                    if (!"kline".equals(msg.getType())) return;

                    BinanceKlineMessage.Kline k = msg.getKline();
                    Candle candle = new Candle(
                            k.getOpenTime(),
                            Double.parseDouble(k.getOpen()),
                            Double.parseDouble(k.getHigh()),
                            Double.parseDouble(k.getLow()),
                            Double.parseDouble(k.getClose()),
                            Double.parseDouble(k.getVolume()));

                    candleService.onKline(key.symbol(), key.interval(), candle, k.isClosed());

                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to parse kline message: " + e.getMessage());
                }
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                log.warning("Binance WS closed: " + key.toTopicKey() + " — reconnecting in 5s");
                connections.remove(key);
                scheduleReconnect(key);
            }

            @Override
            public void onError(Exception e) {
                log.log(Level.WARNING, "Binance WS error [" + key.toTopicKey() + "]: " + e.getMessage());
            }
        };

        connections.put(key, client);
        client.connect();
    }

    private void scheduleReconnect(SubscriptionKey key) {
        Executors.newSingleThreadScheduledExecutor()
                 .schedule(() -> {
                     if (!connections.containsKey(key)) {
                         connect(key);
                     }
                 }, 5, TimeUnit.SECONDS);
    }
}
