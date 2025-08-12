package com.people.job.admin.service;

import com.people.job.admin.dto.DashboardDTO;
import com.people.job.inquiry.dto.InquiryDTO;
import com.people.job.inquiry.entity.InquiryEntity;
import com.people.job.inquiry.repository.InquiryRepository;
import com.people.job.job.dto.JobopeningDTO;
import com.people.job.job.entity.JobopeningEntity;
import com.people.job.job.repository.JobopeningRepository;
import com.people.job.payment.dto.PaymentDTO;
import com.people.job.payment.repository.PaymentRepository;
import com.people.job.user.entity.UserEntity;
import com.people.job.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final JobopeningRepository jobopeningRepository;
    private final InquiryRepository inquiryRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(Long userNo) {
        userRepository.deleteById(userNo);
    }

    @Override
    public List<JobopeningDTO> getAllJobopenings() {
        return jobopeningRepository.findAll().stream()
                .map(job -> JobopeningDTO.builder()
                        .jobopeningNo(job.getJobopeningNo())
                        .title(job.getTitle())
                        .companyNo(job.getCompany().getUserNo())
                        .regdate(job.getRegdate())
                        .deadline(job.getDeadline())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void deleteJobopening(Long jobopeningNo) {
        jobopeningRepository.deleteById(jobopeningNo);
    }

    @Override
    public List<InquiryDTO> getAllInquiries() {
        return inquiryRepository.findAll().stream()
                .map(inq -> InquiryDTO.builder()
                        .inquiryNo(inq.getInquiryNo())
                        .userNo(inq.getUserNo())
                        .title(inq.getTitle())
                        .content(inq.getContent())
                        .status(inq.getStatus())
                        .regdate(inq.getRegdate())
                        .answer(inq.getAnswer())
                        .answerDate(inq.getAnswerDate())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void deleteInquiry(Long inquiryNo) {
        inquiryRepository.deleteById(inquiryNo);
    }

    @Override
    public void answerInquiry(Long inquiryNo, String answer, String answerBy) {
        InquiryEntity entity = inquiryRepository.findById(inquiryNo)
                .orElseThrow(() -> new RuntimeException("문의가 존재하지 않습니다."));

        entity.setAnswer(answer);
        entity.setAnswerDate(LocalDate.now());
        entity.setStatus("ANSWERED");

        inquiryRepository.save(entity);
    }

    @Override
    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(pay -> PaymentDTO.builder()
                        .paymentNo(pay.getPaymentNo())
                        .userNo(pay.getUserNo())
                        .jobopeningNo(pay.getJobopeningNo())
                        .productName(pay.getProductName())
                        .price(pay.getPrice())
                        .status(pay.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public DashboardDTO getDashboardStats() {
        long userCount = userRepository.count();
        long jobCount = jobopeningRepository.count();
        long inquiryCount = inquiryRepository.count();
        long paymentCount = paymentRepository.count();

        return DashboardDTO.builder()
                .totalUsers(userCount)
                .totalJobs(jobCount)
                .totalInquiries(inquiryCount)
                .totalPayments(paymentCount)
                .build();
    }
}