package com.candlesticks.binance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Jackson POJO for a Binance kline WebSocket event.
 *
 * <pre>
 * {
 *   "e": "kline",
 *   "s": "BTCUSDT",
 *   "k": {
 *     "t": 1234567860000,  // open time (epoch ms)
 *     "o": "29000.00",
 *     "h": "29100.00",
 *     "l": "28950.00",
 *     "c": "29050.00",
 *     "v": "12.345",
 *     "x": false           // true = kline closed
 *   }
 * }
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BinanceKlineMessage {

    @JsonProperty("e") private String type;
    @JsonProperty("s") private String symbol;
    @JsonProperty("k") private Kline kline;

    public String getType()   { return type; }
    public String getSymbol() { return symbol; }
    public Kline  getKline()  { return kline; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Kline {
        @JsonProperty("t") private long   openTime;
        @JsonProperty("o") private String open;
        @JsonProperty("h") private String high;
        @JsonProperty("l") private String low;
        @JsonProperty("c") private String close;
        @JsonProperty("v") private String volume;
        @JsonProperty("x") private boolean closed;

        public long    getOpenTime() { return openTime; }
        public String  getOpen()     { return open; }
        public String  getHigh()     { return high; }
        public String  getLow()      { return low; }
        public String  getClose()    { return close; }
        public String  getVolume()   { return volume; }
        public boolean isClosed()    { return closed; }
    }
}
