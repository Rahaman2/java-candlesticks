package com.candlesticks.api;

import com.candlesticks.api.dto.BacktestRequestDto;
import com.candlesticks.api.dto.BacktestResponseDto;
import com.candlesticks.backtest.BacktestConfig;
import com.candlesticks.service.BacktestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/backtest")
public class BacktestController {

    private final BacktestService backtestService;

    public BacktestController(BacktestService backtestService) {
        this.backtestService = backtestService;
    }

    /** GET /api/v1/backtest/strategies — list available strategy names */
    @GetMapping("/strategies")
    public List<String> strategies() {
        return backtestService.availableStrategies();
    }

    /**
     * POST /api/v1/backtest
     * Run a named strategy on the current rolling window.
     */
    @PostMapping
    public ResponseEntity<BacktestResponseDto> run(@RequestBody BacktestRequestDto req) {
        BacktestConfig config = buildConfig(req);
        BacktestResponseDto result = BacktestResponseDto.from(
                backtestService.run(req.symbol(), req.interval(), req.strategyName(), config));
        return ResponseEntity.ok(result);
    }

    private BacktestConfig buildConfig(BacktestRequestDto req) {
        BacktestConfig defaults = BacktestConfig.defaults();
        return new BacktestConfig(
                req.initialCapital()  != null ? BigDecimal.valueOf(req.initialCapital())  : defaults.initialCapital(),
                req.positionSize()    != null ? BigDecimal.valueOf(req.positionSize())    : defaults.positionSize(),
                req.commissionRate()  != null ? req.commissionRate()  : defaults.commissionRate(),
                req.slippage()        != null ? req.slippage()        : defaults.slippage()
        );
    }
}
