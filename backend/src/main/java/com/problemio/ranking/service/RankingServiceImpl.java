package com.problemio.ranking.service;

import com.problemio.ranking.domain.RankingPeriod;
import com.problemio.ranking.dto.RankingResponseDto;
import com.problemio.ranking.dto.RankingRowDto;
import com.problemio.ranking.mapper.RankingMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RankingServiceImpl implements RankingService {

    private final RankingMapper rankingMapper;

    public RankingServiceImpl(RankingMapper rankingMapper) {
        this.rankingMapper = rankingMapper;
    }

    @Override
    @Cacheable(
            value = "ranking",
            key = "#period.name() + '_' + #limit",
            unless = "#result == null || #result.isEmpty()"
    )
    public List<RankingResponseDto> getRanking(RankingPeriod period, int limit) {
        int topN = (limit > 0 && limit <= 100) ? limit : 20;

        LocalDate today = LocalDate.now();
        LocalDateTime start;
        LocalDateTime end;

        switch (period) {
            case TODAY -> {
                start = today.atStartOfDay();
                end = today.plusDays(1).atStartOfDay();
            }
            case YESTERDAY -> {
                start = today.minusDays(1).atStartOfDay();
                end = today.atStartOfDay();
            }
            case WEEK -> {
                start = today.minusDays(6).atStartOfDay();
                end = today.plusDays(1).atStartOfDay();
            }
            default -> throw new IllegalArgumentException("Unsupported period: " + period);
        }

        List<RankingRowDto> rows = rankingMapper.findRankingInPeriod(start, end, topN);

        return rows.stream()
                .map(r -> RankingResponseDto.of(r, calcScore(r)))
                .toList();
    }

    private int calcScore(RankingRowDto r) {
        double score = r.getSolvedQuizCount() * 50.0 + r.getAccuracy() * 1000.0;
        return (int) Math.round(score);
    }
}
