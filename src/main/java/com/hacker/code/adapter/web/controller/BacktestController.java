package com.hacker.code.adapter.web.controller;

import com.hacker.code.application.dto.BacktestRequest;
import com.hacker.code.application.dto.BacktestResponse;
import com.hacker.code.application.service.BacktestAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/backtest")
@RequiredArgsConstructor
public class BacktestController {

    private final BacktestAppService backtestAppService;

    @PostMapping("/run")
    public BacktestResponse run(@RequestBody BacktestRequest request) {
        return backtestAppService.runBacktest(request.getStartDate(), request.getEndDate());
    }
}
