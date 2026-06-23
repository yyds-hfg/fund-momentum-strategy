package com.hacker.code.adapter.web.controller;

import com.hacker.code.application.assembler.StrategyAssembler;
import com.hacker.code.application.dto.RebalanceAdviceDTO;
import com.hacker.code.application.service.ReportAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportAppService reportAppService;
    private final StrategyAssembler strategyAssembler;

    @GetMapping("/weekly")
    public String weeklyReport(Model model) {
        model.addAttribute("advice", strategyAssembler.toDTO(reportAppService.getLatestWeeklyReport()));
        return "weekly-report";
    }

    @GetMapping("/weekly/api")
    @ResponseBody
    public RebalanceAdviceDTO weeklyReportApi() {
        return strategyAssembler.toDTO(reportAppService.getLatestWeeklyReport());
    }

    @GetMapping("/weekly/download")
    public ResponseEntity<byte[]> downloadWeeklyReport() {
        String html = reportAppService.generateWeeklyReportHtml();
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=weekly-report.html")
                .contentType(MediaType.TEXT_HTML)
                .contentLength(bytes.length)
                .body(bytes);
    }
}
