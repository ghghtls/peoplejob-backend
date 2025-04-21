package com.people.job.resume.service;

import com.people.job.resume.dto.ResumeDTO;
import com.people.job.resume.entity.ResumeEntity;
import com.people.job.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;

    @Override
    public void insertResume(ResumeDTO dto) {
        ResumeEntity entity = dtoToEntity(dto);
        resumeRepository.save(entity);
    }

    @Override
    public List<ResumeDTO> selectAll() {
        return resumeRepository.findAll().stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ResumeDTO selectByNo(Long resumeNo) {
        ResumeEntity entity = resumeRepository.findById(resumeNo)
                .orElseThrow(() -> new RuntimeException("이력서를 찾을 수 없습니다."));
        return entityToDTO(entity);
    }

    @Override
    public void updateResume(ResumeDTO dto) {
        ResumeEntity entity = resumeRepository.findById(dto.getResumeNo())
                .orElseThrow(() -> new RuntimeException("해당 이력서가 없습니다."));

        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setEducation(dto.getEducation());
        entity.setCareer(dto.getCareer());
        entity.setCertificate(dto.getCertificate());
        entity.setHopeJobtype(dto.getHopeJobtype());
        entity.setHopeLocation(dto.getHopeLocation());
        entity.setSalary(dto.getSalary());
        entity.setWorkType(dto.getWorkType());
        entity.setRegdate(dto.getRegdate());
        entity.setImagePath(dto.getImagePath());
        entity.setOriginalImage(dto.getOriginalImage());

        resumeRepository.save(entity);
    }

    @Override
    public void deleteResume(Long resumeNo) {
        resumeRepository.deleteById(resumeNo);
    }

    @Override
    public List<ResumeDTO> selectByUserNo(Long userNo) {
        return resumeRepository.findByUserNo(userNo).stream()
                .map(this::entityToDTO)
                .collect(Collectors.toList());
    }

    private ResumeEntity dtoToEntity(ResumeDTO dto) {
        return ResumeEntity.builder()
                .resumeNo(dto.getResumeNo())
                .title(dto.getTitle())
                .content(dto.getContent())
                .education(dto.getEducation())
                .career(dto.getCareer())
                .certificate(dto.getCertificate())
                .hopeJobtype(dto.getHopeJobtype())
                .hopeLocation(dto.getHopeLocation())
                .salary(dto.getSalary())
                .workType(dto.getWorkType())
                .regdate(dto.getRegdate())
                .imagePath(dto.getImagePath())
                .originalImage(dto.getOriginalImage())
                .userNo(dto.getUserNo())
                .build();
    }

    private ResumeDTO entityToDTO(ResumeEntity entity) {
        return ResumeDTO.builder()
                .resumeNo(entity.getResumeNo())
                .title(entity.getTitle())
                .content(entity.getContent())
                .education(entity.getEducation())
                .career(entity.getCareer())
                .certificate(entity.getCertificate())
                .hopeJobtype(entity.getHopeJobtype())
                .hopeLocation(entity.getHopeLocation())
                .salary(entity.getSalary())
                .workType(entity.getWorkType())
                .regdate(entity.getRegdate())
                .imagePath(entity.getImagePath())
                .originalImage(entity.getOriginalImage())
                .userNo(entity.getUserNo())
                .build();
    }
}
