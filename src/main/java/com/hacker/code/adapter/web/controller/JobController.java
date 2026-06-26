package com.hacker.code.adapter.web.controller;

import com.hacker.code.application.dto.RunJobResultDTO;
import com.hacker.code.application.service.BatchJobManualService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final BatchJobManualService batchJobManualService;

    @PostMapping("/{jobId}/run")
    public RunJobResultDTO run(@PathVariable(name = "jobId") String jobId) {
        return batchJobManualService.run(jobId);
    }
}
