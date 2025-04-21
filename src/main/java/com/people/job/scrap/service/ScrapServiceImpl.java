package com.people.job.scrap.service;

import com.people.job.scrap.dto.ScrapDTO;
import com.people.job.scrap.entity.ScrapEntity;
import com.people.job.scrap.repository.ScrapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScrapServiceImpl implements ScrapService {

    private final ScrapRepository scrapRepository;

    @Override
    public void addScrap(ScrapDTO dto) {
        boolean already = scrapRepository.existsByUserNoAndJobopeningNo(dto.getUserNo(), dto.getJobopeningNo());

        if (already) {
            throw new RuntimeException("이미 스크랩한 공고입니다.");
        }

        ScrapEntity entity = ScrapEntity.builder()
                .userNo(dto.getUserNo())
                .jobopeningNo(dto.getJobopeningNo())
                .regdate(LocalDate.now())
                .build();

        scrapRepository.save(entity);
    }

    @Override
    public List<ScrapDTO> getScrapsByUser(Long userNo) {
        return scrapRepository.findByUserNo(userNo).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteScrap(Long scrapNo) {
        scrapRepository.deleteById(scrapNo);
    }

    @Override
    public void deleteScrapByUserAndJob(Long userNo, Long jobopeningNo) {
        ScrapEntity entity = scrapRepository.findByUserNoAndJobopeningNo(userNo, jobopeningNo)
                .orElseThrow(() -> new RuntimeException("스크랩 내역이 없습니다."));
        scrapRepository.delete(entity);
    }

    private ScrapDTO toDTO(ScrapEntity e) {
        return ScrapDTO.builder()
                .scrapNo(e.getScrapNo())
                .userNo(e.getUserNo())
                .jobopeningNo(e.getJobopeningNo())
                .regdate(e.getRegdate())
                .build();
    }
}
