package com.candlesticks.api;

import com.candlesticks.api.dto.CandleDto;
import com.candlesticks.service.CandleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/candles")
public class CandleController {

    private final CandleService candleService;

    public CandleController(CandleService candleService) {
        this.candleService = candleService;
    }

    /** GET /api/v1/candles/{symbol}/{interval}?limit=100 */
    @GetMapping("/{symbol}/{interval}")
    public List<CandleDto> getCandles(
            @PathVariable String symbol,
            @PathVariable String interval,
            @RequestParam(defaultValue = "0") int limit) {

        return candleService.getCandles(symbol.toUpperCase(), interval, limit)
                .stream()
                .map(c -> CandleDto.from(c, false))
                .toList();
    }
}
