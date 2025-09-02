package com.people.job.inquiry.service;

import com.people.job.inquiry.dto.InquiryDTO;

import java.util.List;

public interface InquiryService {

    void insertInquiry(InquiryDTO dto);

    List<InquiryDTO> getAllInquiries(); // 관리자용


    InquiryDTO getInquiry(Long inquiryNo);

    void updateInquiry(InquiryDTO dto); // 문의 수정 (답변 전)

    void deleteInquiry(Long inquiryNo);

    void answerInquiry(Long inquiryNo, String answer); // 관리자 답변
}
