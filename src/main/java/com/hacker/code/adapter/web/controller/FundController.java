package com.hacker.code.adapter.web.controller;

import com.hacker.code.application.assembler.FundAssembler;
import com.hacker.code.application.dto.FundDTO;
import com.hacker.code.application.dto.FundTagDTO;
import com.hacker.code.application.dto.SyncResult;
import com.hacker.code.application.service.FundAppService;
import com.hacker.code.application.service.FundDataSyncAppService;
import com.hacker.code.domain.fund.entity.Fund;
import com.hacker.code.domain.fund.repository.FundRepository;
import com.hacker.code.domain.fund.repository.FundTagRepository;
import com.hacker.code.domain.fund.repository.NavDataRepository;
import com.hacker.code.domain.fund.valueobject.FundTag;
import com.hacker.code.domain.fund.valueobject.Nav;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/funds")
@RequiredArgsConstructor
public class FundController {

    private final FundRepository fundRepository;
    private final FundTagRepository fundTagRepository;
    private final NavDataRepository navDataRepository;
    private final FundDataSyncAppService fundDataSyncAppService;
    private final FundAppService fundAppService;
    private final FundAssembler fundAssembler;

    @GetMapping
    public List<FundDTO> list(@RequestParam(required = false) Long tagId,
                              @RequestParam(required = false, defaultValue = "false") boolean includeDisabled) {
        List<Fund> funds;
        if (tagId != null) {
            funds = fundRepository.findByTag(tagId);
        } else if (includeDisabled) {
            funds = fundRepository.findAll();
        } else {
            funds = fundRepository.findAllEnabled();
        }
        return funds.stream().map(fundAssembler::toDTO).collect(Collectors.toList());
    }

    @GetMapping("/{code}")
    public FundDTO detail(@PathVariable String code) {
        return fundAssembler.toDTO(fundAppService.getFund(code));
    }

    @PostMapping
    public void add(@RequestBody FundDTO dto) {
        List<Long> tagIds = extractTagIds(dto);
        fundAppService.addFund(dto.getFundCode(), dto.getFundName(), dto.getFundType(), tagIds);
    }

    @PutMapping("/{code}")
    public void update(@PathVariable String code, @RequestBody FundDTO dto) {
        List<Long> tagIds = extractTagIds(dto);
        fundAppService.updateFund(code, dto.getFundName(), dto.getFundType(), dto.getStatus(), tagIds);
    }

    private List<Long> extractTagIds(FundDTO dto) {
        if (dto.getTags() == null) {
            return List.of();
        }
        return dto.getTags().stream().map(FundTagDTO::getId).collect(Collectors.toList());
    }

    @DeleteMapping("/{code}")
    public void delete(@PathVariable String code) {
        fundAppService.deleteFund(code);
    }

    @GetMapping("/tags")
    public List<FundTagDTO> listTags() {
        return fundTagRepository.findAll().stream()
                .map(fundAssembler::toDTO)
                .collect(Collectors.toList());
    }

    @PostMapping("/tags")
    public void addTag(@RequestBody FundTagDTO dto) {
        fundTagRepository.findByCode(dto.getTagCode()).ifPresent(t -> {
            throw new IllegalArgumentException("Tag code already exists: " + dto.getTagCode());
        });
        FundTag tag = new FundTag(null, dto.getTagCode(), dto.getTagName(), dto.getColor());
        fundTagRepository.save(tag);
    }

    @PutMapping("/tags/{id}")
    public void updateTag(@PathVariable Long id, @RequestBody FundTagDTO dto) {
        FundTag existing = fundTagRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + id));
        fundTagRepository.findByCode(dto.getTagCode()).ifPresent(t -> {
            if (!t.getId().equals(id)) {
                throw new IllegalArgumentException("Tag code already exists: " + dto.getTagCode());
            }
        });
        FundTag tag = new FundTag(id, dto.getTagCode(), dto.getTagName(), dto.getColor());
        fundTagRepository.update(tag);
    }

    @DeleteMapping("/tags/{id}")
    public void deleteTag(@PathVariable Long id) {
        fundTagRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + id));
        fundTagRepository.deleteById(id);
    }

    @PutMapping("/{code}/tags")
    public void setTags(@PathVariable String code, @RequestBody List<Long> tagIds) {
        fundTagRepository.clearTags(code);
        fundAppService.bindTags(code, tagIds);
    }

    @GetMapping("/{code}/nav")
    public List<Nav> navHistory(@PathVariable String code,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return navDataRepository.findByDateRange(code, startDate, endDate);
    }

    @PostMapping("/sync")
    public SyncResult syncNav(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return fundDataSyncAppService.syncNavData(startDate, endDate);
    }

    @PostMapping("/{code}/sync")
    public SyncResult syncFundNav(@PathVariable String code,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                  @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return fundDataSyncAppService.syncNavDataForFund(code, startDate, endDate);
    }
}
