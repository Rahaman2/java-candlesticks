package com.candlesticks.api;

import com.candlesticks.api.dto.PatternResultDto;
import com.candlesticks.service.CandleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patterns")
public class PatternController {

    private final CandleService candleService;

    public PatternController(CandleService candleService) {
        this.candleService = candleService;
    }

    /** GET /api/v1/patterns/{symbol}/{interval}?minConfidence=0.0 */
    @GetMapping("/{symbol}/{interval}")
    public List<PatternResultDto> getPatterns(
            @PathVariable String symbol,
            @PathVariable String interval,
            @RequestParam(defaultValue = "0.0") double minConfidence) {

        return candleService.getPatterns(symbol.toUpperCase(), interval, minConfidence)
                .stream()
                .map(PatternResultDto::from)
                .toList();
    }
}
