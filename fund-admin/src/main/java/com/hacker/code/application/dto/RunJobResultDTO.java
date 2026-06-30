package com.hacker.code.application.dto;

import lombok.Data;

@Data
public class RunJobResultDTO {

    private boolean success;
    private String message;
    private String jobId;
}
