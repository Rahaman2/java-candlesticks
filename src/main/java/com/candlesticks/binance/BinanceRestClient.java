package com.candlesticks.binance;

import com.candlesticks.Candle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fetches historical OHLCV candles from the Binance REST API.
 *
 * <p>Endpoint: {@code GET https://api.binance.com/api/v3/klines?symbol=BTCUSDT&interval=4h&limit=42}</p>
 *
 * <p>Response format (array of arrays — first 6 fields used):
 * {@code [openTime, open, high, low, close, volume, ...]}</p>
 */
@Component
public class BinanceRestClient {

    private static final Logger log = Logger.getLogger(BinanceRestClient.class.getName());

    @Value("${candlesticks.binance.rest-url:https://api.binance.com/api/v3/klines}")
    private String restUrl;

    private final HttpClient http = HttpClient.newHttpClient();

    /**
     * Fetch the last {@code limit} closed klines for the given symbol and interval.
     *
     * @return list of candles in ascending time order, or empty list on failure
     */
    public List<Candle> fetchHistory(String symbol, String interval, int limit) {
        String url = restUrl + "?symbol=" + symbol.toUpperCase()
                + "&interval=" + interval
                + "&limit=" + limit;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warning("Binance REST returned " + response.statusCode() + " for " + symbol + " " + interval);
                return List.of();
            }

            return parseKlines(response.body());

        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to fetch history for " + symbol + "/" + interval + ": " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Parse Binance klines JSON without an external library.
     *
     * <p>Format: {@code [[1499040000000,"0.01","0.80","0.01","0.015","148976.11",...], ...]}</p>
     */
    private List<Candle> parseKlines(String json) {
        List<Candle> candles = new ArrayList<>();
        // strip outer brackets
        String trimmed = json.trim();
        if (trimmed.equals("[]")) return candles;
        if (trimmed.startsWith("[")) trimmed = trimmed.substring(1, trimmed.length() - 1);

        // split on "],[" to get each row
        String[] rows = trimmed.split("\\],\\s*\\[");
        for (String row : rows) {
            row = row.replaceAll("[\\[\\]]", "").trim();
            String[] parts = row.split(",");
            if (parts.length < 6) continue;
            try {
                long   ts     = Long.parseLong(parts[0].trim());
                double open   = Double.parseDouble(parts[1].trim().replace("\"", ""));
                double high   = Double.parseDouble(parts[2].trim().replace("\"", ""));
                double low    = Double.parseDouble(parts[3].trim().replace("\"", ""));
                double close  = Double.parseDouble(parts[4].trim().replace("\"", ""));
                double volume = Double.parseDouble(parts[5].trim().replace("\"", ""));
                candles.add(new Candle(ts, open, high, low, close, volume));
            } catch (NumberFormatException e) {
                // skip malformed row
            }
        }
        return candles;
    }
}
