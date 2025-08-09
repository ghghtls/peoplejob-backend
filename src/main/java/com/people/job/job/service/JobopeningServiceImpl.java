package com.people.job.job.service;

import com.people.job.job.dto.JobopeningDTO;
import com.people.job.job.entity.JobopeningEntity;
import com.people.job.job.repository.JobopeningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;
import com.people.job.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class JobopeningServiceImpl implements JobopeningService {

    private final JobopeningRepository jobopeningRepository;
    private final UserRepository userRepository;

    @Override
    public void insertJobopening(JobopeningDTO dto) {
        JobopeningEntity entity = JobopeningEntity.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .jobtype(dto.getJobtype())
                .location(dto.getLocation())
                .education(dto.getEducation())
                .career(dto.getCareer())
                .salary(dto.getSalary())
                .regdate(dto.getRegdate())
                .deadline(dto.getDeadline())
                .company( // ✅ UserEntity로 변환
                        userRepository.findById(dto.getCompanyNo())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회사 정보가 없습니다."))
                )
                .filename(dto.getFilename())
                .originalFilename(dto.getOriginalFilename())
                .build();

        jobopeningRepository.save(entity);
    }

    @Override
    public List<JobopeningDTO> selectAll() {
        return jobopeningRepository.findAll().stream()
                .map(entity -> JobopeningDTO.builder()
                        .jobopeningNo(entity.getJobopeningNo())
                        .title(entity.getTitle())
                        .content(entity.getContent())
                        .jobtype(entity.getJobtype())
                        .location(entity.getLocation())
                        .education(entity.getEducation())
                        .career(entity.getCareer())
                        .salary(entity.getSalary())
                        .regdate(entity.getRegdate())
                        .deadline(entity.getDeadline())
                        .companyNo(entity.getCompany().getUserNo())
                        .filename(entity.getFilename())
                        .originalFilename(entity.getOriginalFilename())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public JobopeningDTO selectByNo(Long jobopeningNo) {
        JobopeningEntity entity = jobopeningRepository.findById(jobopeningNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공고가 존재하지 않습니다."));

        return JobopeningDTO.builder()
                .jobopeningNo(entity.getJobopeningNo())
                .title(entity.getTitle())
                .content(entity.getContent())
                .jobtype(entity.getJobtype())
                .location(entity.getLocation())
                .education(entity.getEducation())
                .career(entity.getCareer())
                .salary(entity.getSalary())
                .regdate(entity.getRegdate())
                .deadline(entity.getDeadline())
                .companyNo(entity.getCompany().getUserNo())
                .originalFilename(entity.getOriginalFilename())
                .build();
    }

    @Override
    public void updateJobopening(JobopeningDTO dto) {
        JobopeningEntity entity = jobopeningRepository.findById(dto.getJobopeningNo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공고가 존재하지 않습니다."));

        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setJobtype(dto.getJobtype());
        entity.setLocation(dto.getLocation());
        entity.setEducation(dto.getEducation());
        entity.setCareer(dto.getCareer());
        entity.setSalary(dto.getSalary());
        entity.setRegdate(dto.getRegdate());
        entity.setDeadline(dto.getDeadline());
        entity.setCompany(
                userRepository.findById(dto.getCompanyNo())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "회사 정보가 없습니다."))
        );
        entity.setFilename(dto.getFilename());
        entity.setOriginalFilename(dto.getOriginalFilename());

        jobopeningRepository.save(entity);
    }

    @Override
    public void deleteJobopening(Long jobopeningNo) {
        if (!jobopeningRepository.existsById(jobopeningNo)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "삭제할 공고가 존재하지 않습니다.");
        }
        jobopeningRepository.deleteById(jobopeningNo);
    }
}
