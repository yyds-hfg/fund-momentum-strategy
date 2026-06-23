package com.hacker.code.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hacker.code.domain.fund.repository.NavDataRepository;
import com.hacker.code.domain.fund.valueobject.Nav;
import com.hacker.code.infrastructure.mapper.FundNavMapper;
import com.hacker.code.infrastructure.persistence.po.FundNavPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class NavDataRepositoryImpl implements NavDataRepository {

    private final FundNavMapper fundNavMapper;

    @Override
    public Optional<Nav> findLatest(String fundCode) {
        return Optional.ofNullable(fundNavMapper.selectLatest(fundCode)).map(this::toDomain);
    }

    @Override
    public List<Nav> findByDateRange(String fundCode, LocalDate startDate, LocalDate endDate) {
        return fundNavMapper.selectByDateRange(fundCode, startDate, endDate).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void save(String fundCode, Nav nav) {
        fundNavMapper.insert(toPO(fundCode, nav));
    }

    @Override
    public void batchSave(String fundCode, List<Nav> navList) {
        List<FundNavPO> pos = navList.stream()
                .map(nav -> toPO(fundCode, nav))
                .collect(Collectors.toList());
        for (FundNavPO po : pos) {
            fundNavMapper.insert(po);
        }
    }

    @Override
    public boolean exists(String fundCode, LocalDate navDate) {
        LambdaQueryWrapper<FundNavPO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FundNavPO::getFundCode, fundCode)
               .eq(FundNavPO::getNavDate, navDate);
        return fundNavMapper.selectCount(wrapper) > 0;
    }

    private Nav toDomain(FundNavPO po) {
        return new Nav(
                po.getNavDate(),
                po.getOpenNav(),
                po.getHighNav(),
                po.getLowNav(),
                po.getCloseNav(),
                po.getVolume()
        );
    }

    private FundNavPO toPO(String fundCode, Nav nav) {
        FundNavPO po = new FundNavPO();
        po.setFundCode(fundCode);
        po.setNavDate(nav.getDate());
        po.setOpenNav(nav.getOpenNav());
        po.setHighNav(nav.getHighNav());
        po.setLowNav(nav.getLowNav());
        po.setCloseNav(nav.getCloseNav());
        po.setVolume(nav.getVolume());
        return po;
    }
}
