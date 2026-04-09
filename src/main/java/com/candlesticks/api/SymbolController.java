package com.candlesticks.api;

import com.candlesticks.binance.BinanceWsClient;
import com.candlesticks.binance.SubscriptionKey;
import com.candlesticks.push.LiveUpdatePublisher;
import com.candlesticks.service.CandleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SymbolController {

    private final CandleService       candleService;
    private final BinanceWsClient     binanceWsClient;
    private final LiveUpdatePublisher publisher;

    public SymbolController(CandleService candleService,
                            BinanceWsClient binanceWsClient,
                            LiveUpdatePublisher publisher) {
        this.candleService   = candleService;
        this.binanceWsClient = binanceWsClient;
        this.publisher       = publisher;
    }

    /** GET /api/v1/symbols — list active subscriptions */
    @GetMapping("/symbols")
    public List<String> symbols() {
        return candleService.getActiveKeys().stream()
                .map(SubscriptionKey::toTopicKey)
                .sorted()
                .toList();
    }

    /** GET /api/v1/intervals — supported kline intervals */
    @GetMapping("/intervals")
    public List<String> intervals() {
        return List.of("1m", "3m", "5m", "15m", "30m", "1h", "2h", "4h", "6h", "12h", "1d");
    }

    /**
     * POST /api/v1/subscribe
     * Body: {@code {"symbol":"BTCUSDT","interval":"1m"}}
     */
    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, String>> subscribe(@RequestBody Map<String, String> body) {
        String symbol   = body.getOrDefault("symbol",   "").toUpperCase();
        String interval = body.getOrDefault("interval", "1m");
        if (symbol.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "symbol is required"));
        }
        binanceWsClient.subscribe(symbol, interval);
        String key = new SubscriptionKey(symbol, interval).toTopicKey();
        publisher.pushSubscriptions(candleService.getActiveKeys().stream()
                .map(SubscriptionKey::toTopicKey).sorted().toList());
        return ResponseEntity.ok(Map.of("key", key));
    }

    /** DELETE /api/v1/subscribe/{symbol}/{interval} */
    @DeleteMapping("/subscribe/{symbol}/{interval}")
    public ResponseEntity<Void> unsubscribe(@PathVariable String symbol,
                                            @PathVariable String interval) {
        binanceWsClient.unsubscribe(symbol.toUpperCase(), interval);
        publisher.pushSubscriptions(candleService.getActiveKeys().stream()
                .map(SubscriptionKey::toTopicKey).sorted().toList());
        return ResponseEntity.noContent().build();
    }
}
