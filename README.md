# java-candlesticks

A Java 21 candlestick pattern detection, backtesting, and live trading library with a Spring Boot API, JavaFX desktop chart, and browser-based web UI.

## Features

- **40+ built-in candlestick patterns** — Doji, Hammer, Engulfing, Harami, Kicker, Morning/Evening Star, Three White Soldiers, and more
- **Pattern scanner** — scans any OHLCV series and returns typed results with confidence scores
- **Backtesting engine** — BigDecimal P&L precision, slippage/commission modelling, Virtual Threads for parallel strategy runs
- **Live Binance feed** — real-time kline WebSocket client with auto-reconnect
- **REST API** — query candles, patterns, and run backtests over HTTP
- **Desktop UI** — JavaFX TradingView-style candlestick chart with pattern markers
- **Web UI** — browser-based live chart (TradingView Lightweight Charts + STOMP/SockJS)

## Project Structure

```
com.candlesticks/
├── Candle.java / interfaces/ICandle.java   — OHLCV model (CCXT convention)
├── model/CandleProps.java                  — derived properties (body, wicks, direction)
├── pattern/                                — PatternResult, PatternMetadata, enums
├── patterns/                               — 40+ pattern implementations
├── registry/PatternRegistry.java           — register/unregister patterns
├── scanner/CandleScanner.java              — sliding-window pattern detection
├── patterns/BuiltInPatterns.java           — factory: createScanner() / createRegistry()
├── loader/CsvLoader.java                   — CSV → List<Candle>
├── loader/JsonLoader.java                  — CCXT JSON [[ts,o,h,l,c,v],...] → List<Candle>
├── backtest/                               — BacktestRunner, Strategy, Trade, BacktestStats
├── chart/                                  — JavaFX CandleChartPane + ChartTheme
├── spring/                                 — Spring Boot entry point + JavaFX bridge
├── binance/                                — Binance WebSocket client
├── service/                                — CandleService (rolling window), BacktestService
├── push/                                   — STOMP publisher + CandleUpdateEvent
└── api/                                    — REST controllers + DTOs
```

## Run Modes

### Spring Boot web app (live data + API + browser chart)
```bash
mvn spring-boot:run
```
- Connects to Binance kline stream on startup (`BTCUSDT 1m` by default)
- Open **http://localhost:8080** in your browser for the live chart
- REST API: http://localhost:8080/api/v1/...

### Standalone JavaFX chart (demo data, no Spring)
```bash
mvn javafx:run
```

### Console demo (pattern scan + backtest)
```bash
mvn compile exec:java -Dexec.mainClass=com.candlesticks.Main
```

## REST API

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/candles/{symbol}/{interval}?limit=N` | Rolling candle window |
| `GET` | `/api/v1/patterns/{symbol}/{interval}?minConfidence=0.7` | Detected patterns |
| `POST` | `/api/v1/backtest` | Run a named strategy on current data |
| `GET` | `/api/v1/backtest/strategies` | List available strategy names |
| `GET` | `/api/v1/symbols` | Active subscriptions |
| `POST` | `/api/v1/subscribe` | Subscribe to a symbol+interval |
| `DELETE` | `/api/v1/subscribe/{symbol}/{interval}` | Unsubscribe |

### Example requests
```bash
# Get last 100 BTCUSDT 1m candles
curl http://localhost:8080/api/v1/candles/BTCUSDT/1m?limit=100

# Get patterns with confidence ≥ 70%
curl http://localhost:8080/api/v1/patterns/BTCUSDT/1m?minConfidence=0.7

# Subscribe to ETHUSDT 5m
curl -X POST http://localhost:8080/api/v1/subscribe \
  -H 'Content-Type: application/json' \
  -d '{"symbol":"ETHUSDT","interval":"5m"}'

# Run a backtest
curl -X POST http://localhost:8080/api/v1/backtest \
  -H 'Content-Type: application/json' \
  -d '{"symbol":"BTCUSDT","interval":"1m","strategyName":"BULLISH_CONF_70"}'
```

### Built-in backtest strategies
| Name | Description |
|------|-------------|
| `BULLISH_CONF_60` | Buy on any bullish pattern ≥ 60% confidence |
| `BULLISH_CONF_70` | Buy on any bullish pattern ≥ 70% confidence |
| `BULLISH_CONF_75` | Buy on any bullish pattern ≥ 75% confidence |
| `BEARISH_CONF_70` | Sell on any bearish pattern ≥ 70% confidence |

## Live WebSocket (STOMP)

Browser clients connect via SockJS to `/ws` and subscribe to:

| Topic | Payload | When |
|-------|---------|------|
| `/topic/candles/{symbol}_{interval}` | Single candle (`partial` flag) | Every Binance tick |
| `/topic/patterns/{symbol}_{interval}` | Full pattern list | On candle close |
| `/topic/subscriptions` | Active key list | On subscribe/unsubscribe |

## Configuration

`src/main/resources/application.properties`:
```properties
server.port=8080
candlesticks.binance.ws-url=wss://stream.binance.com:9443/ws
candlesticks.default-symbols=BTCUSDT
candlesticks.default-interval=1m
candlesticks.rolling-window-size=500
```

## Using the Library

```java
// Scan a series for patterns
CandleScanner scanner = BuiltInPatterns.createScanner();
List<PatternResult> results = scanner.scan(candles);

// Filter by confidence
List<PatternResult> highConf = scanner.scan(candles, 0.75);

// Run a backtest
BacktestRunner runner = new BacktestRunner(BuiltInPatterns.createScanner());
Strategy strategy = (patterns, candle) ->
    patterns.stream().anyMatch(r ->
        r.metadata().direction() == PatternDirection.BULLISH
        && r.metadata().confidence() >= 0.70)
    ? Signal.BUY : Signal.HOLD;

BacktestResult result = runner.run(candles, strategy);
result.print();

// Load data
List<Candle> fromCsv  = CsvLoader.load(Path.of("data.csv"));
List<Candle> fromJson = JsonLoader.load(Path.of("data.json"));  // CCXT format
```

## Requirements

- Java 21+
- Maven 3.6+
