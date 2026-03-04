package com.candlesticks.loader;

import com.candlesticks.Candle;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads OHLCV candlestick data from a CSV file into a {@link List} of {@link Candle}.
 *
 * <h3>Supported column layouts (auto-detected)</h3>
 * <pre>
 * 6-column: timestamp, open, high, low, close, volume
 * 4-column: open, high, low, close                     (timestamp=0, volume=0)
 * </pre>
 *
 * <p>Headers are auto-detected: if the first token of the first row cannot be parsed
 * as a number the row is skipped.</p>
 *
 * <p>Delimiters accepted: comma {@code ,}, semicolon {@code ;}, or tab {@code \t}.</p>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * List<Candle> candles = CsvLoader.load(Path.of("data/btcusdt.csv"));
 * }</pre>
 */
public final class CsvLoader {

    private CsvLoader() {}

    /**
     * Load candles from a CSV file using comma, semicolon, or tab as delimiter.
     *
     * @param path path to the CSV file
     * @return ordered list of {@link Candle}, oldest first
     * @throws IOException if the file cannot be read
     */
    public static List<Candle> load(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        List<Candle> candles = new ArrayList<>(lines.size());

        int start = skipHeader(lines);
        for (int i = start; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String[] parts = line.split("[,;\t]");
            try {
                Candle c = parseParts(parts);
                if (c != null) candles.add(c);
            } catch (NumberFormatException ignored) {
                // skip malformed rows silently
            }
        }
        return candles;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static int skipHeader(List<String> lines) {
        if (lines.isEmpty()) return 0;
        String firstToken = lines.get(0).split("[,;\t]")[0].trim();
        try {
            Double.parseDouble(firstToken);
            return 0; // first token is a number → no header
        } catch (NumberFormatException e) {
            return 1; // skip the header row
        }
    }

    private static Candle parseParts(String[] p) {
        if (p.length >= 6) {
            long   ts = Long.parseLong(p[0].trim());
            double o  = Double.parseDouble(p[1].trim());
            double h  = Double.parseDouble(p[2].trim());
            double l  = Double.parseDouble(p[3].trim());
            double c  = Double.parseDouble(p[4].trim());
            double v  = Double.parseDouble(p[5].trim());
            return new Candle(ts, o, h, l, c, v);
        }
        if (p.length >= 4) {
            double o = Double.parseDouble(p[0].trim());
            double h = Double.parseDouble(p[1].trim());
            double l = Double.parseDouble(p[2].trim());
            double c = Double.parseDouble(p[3].trim());
            return new Candle(o, h, l, c);
        }
        return null;
    }
}
