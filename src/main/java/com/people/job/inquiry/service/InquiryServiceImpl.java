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
        InquiryEntity e = InquiryEntity.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .writer(dto.getWriter())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .category(dto.getCategory())
                .regdate(LocalDate.now())
                .isAnswered(false)
                .build();
        inquiryRepository.save(e);
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
        var e = inquiryRepository.findById(inquiryNo)
                .orElseThrow(() -> new RuntimeException("문의가 존재하지 않습니다."));
        e.setAnswer(answer);
        e.setAnswerDate(LocalDate.now());
        e.setIsAnswered(true);
        inquiryRepository.save(e);
    }

    private InquiryDTO toDTO(InquiryEntity e) {
        return InquiryDTO.builder()
                .inquiryNo(e.getInquiryNo())
                .title(e.getTitle())
                .content(e.getContent())
                .writer(e.getWriter())       // DB: writer
                .email(e.getEmail())         // DB: email
                .phone(e.getPhone())         // DB: phone
                .category(e.getCategory())   // DB: category
                .regdate(e.getRegdate())
                .isAnswered(e.getIsAnswered()) // DB: isAnswered (boolean)
                .answer(e.getAnswer())
                .answerDate(e.getAnswerDate())
                .answerBy(e.getAnswerBy())     // DB: answerBy
                .build();
    }
}
