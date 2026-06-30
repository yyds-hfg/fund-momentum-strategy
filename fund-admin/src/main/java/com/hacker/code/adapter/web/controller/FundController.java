package com.hacker.code.adapter.web.controller;

import com.hacker.code.adapter.web.common.PageResult;
import com.hacker.code.application.assembler.FundAssembler;
import com.hacker.code.application.dto.FundDTO;
import com.hacker.code.application.dto.SyncResult;
import com.hacker.code.application.service.FundAppService;
import com.hacker.code.application.service.FundDataSyncAppService;
import com.hacker.code.domain.fund.entity.Fund;
import com.hacker.code.domain.fund.repository.FundRepository;
import com.hacker.code.domain.fund.repository.NavDataRepository;
import com.hacker.code.domain.fund.valueobject.Nav;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/funds")
@RequiredArgsConstructor
public class FundController {

    private final FundRepository fundRepository;
    private final NavDataRepository navDataRepository;
    private final FundDataSyncAppService fundDataSyncAppService;
    private final FundAppService fundAppService;
    private final FundAssembler fundAssembler;

    @GetMapping
    public List<FundDTO> list(@RequestParam(name = "keyword", required = false) String keyword,
                              @RequestParam(name = "fundTypes", required = false) String fundTypes,
                              @RequestParam(name = "includeDisabled", required = false, defaultValue = "false") boolean includeDisabled) {
        List<String> typeList = parseFundTypes(fundTypes);
        List<Fund> funds = fundRepository.findByConditions(keyword, typeList, includeDisabled);
        return funds.stream().map(fundAssembler::toDTO).collect(Collectors.toList());
    }

    @GetMapping("/page")
    public PageResult<FundDTO> page(@RequestParam(name = "keyword", required = false) String keyword,
                                    @RequestParam(name = "fundTypes", required = false) String fundTypes,
                                    @RequestParam(name = "includeDisabled", required = false, defaultValue = "false") boolean includeDisabled,
                                    @RequestParam(name = "page", defaultValue = "1") long page,
                                    @RequestParam(name = "size", defaultValue = "10") long size) {
        List<String> typeList = parseFundTypes(fundTypes);
        var fundPage = fundRepository.findByConditions(keyword, typeList, includeDisabled, page, size);
        List<FundDTO> records = fundPage.getRecords().stream()
                .map(fundAssembler::toDTO)
                .collect(Collectors.toList());
        return new PageResult<>(records, fundPage.getTotal(), fundPage.getSize(), fundPage.getCurrent());
    }

    @GetMapping("/{code}")
    public FundDTO detail(@PathVariable(name = "code") String code) {
        return fundAssembler.toDTO(fundAppService.getFund(code));
    }

    @PostMapping
    public void add(@RequestBody FundDTO dto) {
        fundAppService.addFund(dto.getFundCode(), dto.getFundName(), dto.getFundType(), dto.getDescription());
    }

    @PutMapping("/{code}")
    public void update(@PathVariable(name = "code") String code, @RequestBody FundDTO dto) {
        fundAppService.updateFund(code, dto.getFundName(), dto.getFundType(), dto.getDescription(), dto.getStatus());
    }

    @DeleteMapping("/{code}")
    public void delete(@PathVariable(name = "code") String code) {
        fundAppService.deleteFund(code);
    }

    @GetMapping("/{code}/nav")
    public List<Nav> navHistory(@PathVariable(name = "code") String code,
                                @RequestParam(name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return navDataRepository.findByDateRange(code, startDate, endDate);
    }

    @PostMapping("/sync")
    public SyncResult syncNav(@RequestParam(name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam(name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return fundDataSyncAppService.syncNavData(startDate, endDate);
    }

    @PostMapping("/{code}/sync")
    public SyncResult syncFundNav(@PathVariable(name = "code") String code,
                                  @RequestParam(name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                  @RequestParam(name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return fundDataSyncAppService.syncNavDataForFund(code, startDate, endDate);
    }

    private List<String> parseFundTypes(String fundTypes) {
        if (fundTypes == null || fundTypes.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(fundTypes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
