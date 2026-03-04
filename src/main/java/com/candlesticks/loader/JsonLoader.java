package com.candlesticks.loader;

import com.candlesticks.Candle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads OHLCV data from a JSON file in <strong>CCXT array format</strong>.
 *
 * <h3>Expected format</h3>
 * <pre>
 * [[timestamp, open, high, low, close, volume], ...]
 * </pre>
 * This matches the array returned by CCXT's {@code fetch_ohlcv()}.
 *
 * <p>No external JSON library is required — the format is regular enough for
 * lightweight string parsing.</p>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * List<Candle> candles = JsonLoader.load(Path.of("data/btcusdt.json"));
 * }</pre>
 */
public final class JsonLoader {

    private JsonLoader() {}

    /**
     * Load candles from a CCXT-format JSON file.
     *
     * @param path path to the JSON file
     * @return ordered list of {@link Candle}, oldest first
     * @throws IOException if the file cannot be read
     */
    public static List<Candle> load(Path path) throws IOException {
        String raw = Files.readString(path, StandardCharsets.UTF_8).trim();
        return parse(raw);
    }

    /**
     * Parse a CCXT JSON string directly (useful for testing or network responses).
     *
     * @param json raw JSON string {@code [[ts,o,h,l,c,v], ...]}
     * @return list of candles
     */
    public static List<Candle> parse(String json) {
        String content = json.trim();

        // Strip outer array brackets
        if (content.startsWith("[")) content = content.substring(1);
        if (content.endsWith("]"))   content = content.substring(0, content.length() - 1);
        content = content.trim();

        if (content.isEmpty()) return List.of();

        // Split on ],[  — each element is one row
        String[] rows = content.split("\\],\\s*\\[");
        List<Candle> candles = new ArrayList<>(rows.length);

        for (String row : rows) {
            row = row.trim();
            if (row.startsWith("[")) row = row.substring(1);
            if (row.endsWith("]"))   row = row.substring(0, row.length() - 1);

            String[] parts = row.split(",");
            if (parts.length < 6) continue;

            try {
                long   ts = Long.parseLong(parts[0].trim());
                double o  = Double.parseDouble(parts[1].trim());
                double h  = Double.parseDouble(parts[2].trim());
                double l  = Double.parseDouble(parts[3].trim());
                double c  = Double.parseDouble(parts[4].trim());
                double v  = Double.parseDouble(parts[5].trim());
                candles.add(new Candle(ts, o, h, l, c, v));
            } catch (NumberFormatException ignored) {
                // skip malformed rows
            }
        }
        return candles;
    }
}
