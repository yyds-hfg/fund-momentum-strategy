package com.hacker.code.adapter.web.controller;

import com.hacker.code.application.dto.BacktestDetailDTO;
import com.hacker.code.application.dto.BacktestRecordDTO;
import com.hacker.code.application.dto.DashboardDTO;
import com.hacker.code.application.dto.FundMomentumRanksGroupDTO;
import com.hacker.code.application.service.DashboardAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardAppService dashboardAppService;

    @GetMapping
    public String index() {
        return "dashboard";
    }

    @GetMapping("/api/data")
    @ResponseBody
    public DashboardDTO data() {
        return dashboardAppService.getDashboardData();
    }

    @GetMapping("/api/backtest")
    @ResponseBody
    public List<BacktestRecordDTO> backtestRecords() {
        return dashboardAppService.getBacktestRecords();
    }

    @GetMapping("/api/momentum-ranks")
    @ResponseBody
    public List<FundMomentumRanksGroupDTO> allMomentumRanks() {
        return dashboardAppService.getAllMomentumRankGroupsForRealtime();
    }

    @GetMapping("/api/backtest/{id}")
    @ResponseBody
    public BacktestDetailDTO backtestDetail(@PathVariable(name = "id") Long id) {
        return dashboardAppService.getBacktestDetail(id);
    }
}
