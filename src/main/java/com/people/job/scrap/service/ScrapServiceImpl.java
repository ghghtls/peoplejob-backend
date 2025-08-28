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
        // jobopeningNo -> jobNo
        boolean already = scrapRepository.existsByUserNoAndJobNo(dto.getUserNo(), dto.getJobNo());
        if (already) {
            throw new RuntimeException("이미 스크랩한 공고입니다.");
        }

        ScrapEntity entity = ScrapEntity.builder()
                .userNo(dto.getUserNo())
                .jobNo(dto.getJobNo())               // jobopeningNo -> jobNo
                .scrapDate(LocalDate.now())          // regdate -> scrapDate
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
    public void deleteScrapByUserAndJob(Long userNo, Long jobNo) {
        // findByUserNoAndJobopeningNo -> findByUserNoAndJobNo
        ScrapEntity entity = scrapRepository.findByUserNoAndJobNo(userNo, jobNo)
                .orElseThrow(() -> new RuntimeException("스크랩 내역이 없습니다."));
        scrapRepository.delete(entity);
    }

    private ScrapDTO toDTO(ScrapEntity e) {
        return ScrapDTO.builder()
                .scrapNo(e.getScrapNo())
                .userNo(e.getUserNo())
                .jobNo(e.getJobNo())                 // jobopeningNo -> jobNo
                .scrapDate(e.getScrapDate())         // regdate -> scrapDate
                .build();
    }
}
