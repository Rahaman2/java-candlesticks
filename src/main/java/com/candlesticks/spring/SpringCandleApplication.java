package com.candlesticks.spring;

import com.candlesticks.binance.BinanceWsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point — runs as a pure web application.
 *
 * <p>Starts the HTTP server, REST API, STOMP WebSocket server, and Binance kline client.
 * Open {@code http://localhost:8080} in a browser to view the live chart.</p>
 *
 * <p>Run with: {@code mvn spring-boot:run}</p>
 */
@SpringBootApplication(scanBasePackages = "com.candlesticks")
public class SpringCandleApplication implements CommandLineRunner {

    private final BinanceWsClient binanceWsClient;

    @Value("${candlesticks.default-symbols:BTCUSDT}")
    private String defaultSymbols;

    @Value("${candlesticks.default-interval:1m}")
    private String defaultInterval;

    public SpringCandleApplication(BinanceWsClient binanceWsClient) {
        this.binanceWsClient = binanceWsClient;
    }

    public static void main(String[] args) {
        SpringApplication.run(SpringCandleApplication.class, args);
    }

    @Override
    public void run(String... args) {
        for (String symbol : defaultSymbols.split(",")) {
            String s = symbol.trim();
            if (!s.isBlank()) {
                binanceWsClient.subscribe(s, defaultInterval);
            }
        }
    }
}
