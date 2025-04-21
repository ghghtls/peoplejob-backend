package com.people.job.inquiry.service;

import com.people.job.inquiry.dto.InquiryDTO;
import com.people.job.inquiry.entity.InquiryEntity;
import com.people.job.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;

    @Override
    public void insertInquiry(InquiryDTO dto) {
        InquiryEntity entity = InquiryEntity.builder()
                .userNo(dto.getUserNo())
                .title(dto.getTitle())
                .content(dto.getContent())
                .regdate(LocalDate.now())
                .status("WAIT")
                .build();
        inquiryRepository.save(entity);
    }

    @Override
    public List<InquiryDTO> getAllInquiries() {
        return inquiryRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InquiryDTO> getInquiriesByUser(Long userNo) {
        return inquiryRepository.findByUserNo(userNo).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InquiryDTO getInquiry(Long inquiryNo) {
        InquiryEntity entity = inquiryRepository.findById(inquiryNo)
                .orElseThrow(() -> new RuntimeException("문의가 존재하지 않습니다."));
        return toDTO(entity);
    }

    @Override
    public void updateInquiry(InquiryDTO dto) {
        InquiryEntity entity = inquiryRepository.findById(dto.getInquiryNo())
                .orElseThrow(() -> new RuntimeException("문의가 존재하지 않습니다."));

        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());

        inquiryRepository.save(entity);
    }

    @Override
    public void deleteInquiry(Long inquiryNo) {
        inquiryRepository.deleteById(inquiryNo);
    }

    @Override
    public void answerInquiry(Long inquiryNo, String answer) {
        InquiryEntity entity = inquiryRepository.findById(inquiryNo)
                .orElseThrow(() -> new RuntimeException("문의가 존재하지 않습니다."));

        entity.setAnswer(answer);
        entity.setAnswerDate(LocalDate.now());
        entity.setStatus("ANSWERED");

        inquiryRepository.save(entity);
    }

    private InquiryDTO toDTO(InquiryEntity e) {
        return InquiryDTO.builder()
                .inquiryNo(e.getInquiryNo())
                .userNo(e.getUserNo())
                .title(e.getTitle())
                .content(e.getContent())
                .regdate(e.getRegdate())
                .answer(e.getAnswer())
                .answerDate(e.getAnswerDate())
                .status(e.getStatus())
                .build();
    }
}
