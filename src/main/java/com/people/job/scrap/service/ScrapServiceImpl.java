package com.people.job.scrap.service;

import com.people.job.job.entity.JobopeningEntity;
import com.people.job.job.repository.JobopeningRepository;
import com.people.job.scrap.dto.ScrapDTO;
import com.people.job.scrap.entity.ScrapEntity;
import com.people.job.scrap.repository.ScrapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScrapServiceImpl implements ScrapService {

    private final ScrapRepository scrapRepository;
    private final JobopeningRepository jobopeningRepository;

    @Override
    @Transactional
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
    @Transactional(readOnly = true)
    public List<ScrapDTO> getScrapsByUser(Long userNo) {
        return scrapRepository.findByUserNo(userNo).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteScrap(Long scrapNo) {
        scrapRepository.deleteById(scrapNo);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isScraped(Long userNo, Long jobNo) {
        return scrapRepository.existsByUserNoAndJobNo(userNo, jobNo);
    }

    @Override
    @Transactional
    public void deleteScrapByUserAndJob(Long userNo, Long jobNo) {
        // findByUserNoAndJobopeningNo -> findByUserNoAndJobNo
        ScrapEntity entity = scrapRepository.findByUserNoAndJobNo(userNo, jobNo)
                .orElseThrow(() -> new RuntimeException("스크랩 내역이 없습니다."));
        scrapRepository.delete(entity);
    }

    private ScrapDTO toDTO(ScrapEntity e) {
        ScrapDTO.ScrapDTOBuilder builder = ScrapDTO.builder()
                .scrapNo(e.getScrapNo())
                .userNo(e.getUserNo())
                .jobNo(e.getJobNo())
                .scrapDate(e.getScrapDate());

        jobopeningRepository.findById(e.getJobNo()).ifPresent(job -> {
            builder.jobTitle(job.getTitle())
                   .companyName(job.getCompany())
                   .location(job.getLocation())
                   .jobType(job.getJobType())
                   .deadline(job.getDeadline())
                   .isExpired(job.getDeadline() != null && LocalDate.now().isAfter(job.getDeadline()));
        });

        return builder.build();
    }
}
