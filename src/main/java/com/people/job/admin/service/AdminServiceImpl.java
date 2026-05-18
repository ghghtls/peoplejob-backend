package com.people.job.admin.service;

import com.people.job.admin.dto.DashboardDTO;
import com.people.job.apply.repository.ApplyRepository;
import com.people.job.inquiry.dto.InquiryDTO;
import com.people.job.inquiry.entity.InquiryEntity;
import com.people.job.inquiry.repository.InquiryRepository;
import com.people.job.job.dto.JobopeningDTO;
import com.people.job.job.repository.JobopeningRepository;
import com.people.job.payment.dto.PaymentDTO;
import com.people.job.payment.repository.PaymentRepository;
import com.people.job.user.entity.UserEntity;
import com.people.job.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final JobopeningRepository jobopeningRepository;
    private final InquiryRepository inquiryRepository;
    private final PaymentRepository paymentRepository;
    private final ApplyRepository applyRepository;

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
                        .jobNo(job.getJobNo()) // 실제 DB 필드명
                        .title(job.getTitle())
                        .company(job.getCompany()) // String 타입 필드
                        .regdate(LocalDate.from(job.getRegdate()))
                        .deadline(job.getDeadline())
                        .userNo(job.getUserNo()) // 실제 DB 필드명
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void deleteJobopening(Long jobNo) { // 매개변수명 수정
        jobopeningRepository.deleteById(jobNo);
    }

    @Override
    public List<InquiryDTO> getAllInquiries() {
        return inquiryRepository.findAll().stream()
                .map(inq -> InquiryDTO.builder()
                        .inquiryNo(inq.getInquiryNo())
                        .writer(inq.getWriter()) // 실제 DB 필드명
                        .email(inq.getEmail()) // 실제 DB 필드명
                        .title(inq.getTitle())
                        .content(inq.getContent())
                        .category(inq.getCategory()) // 실제 DB 필드명
                        .isAnswered(inq.getIsAnswered()) // 실제 DB 필드명
                        .regdate(inq.getRegdate())
                        .answer(inq.getAnswer())
                        .answerDate(inq.getAnswerDate())
                        .answerBy(inq.getAnswerBy()) // 실제 DB 필드명
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
        entity.setAnswerBy(answerBy); // 실제 DB 필드명
        entity.setIsAnswered(true); // 실제 DB 필드명 (boolean 타입)

        inquiryRepository.save(entity);
    }

    @Override
    public List<PaymentDTO> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(pay -> PaymentDTO.builder()
                        .paymentNo(pay.getPaymentNo())
                        .userNo(pay.getUserNo())
                        .amount(pay.getAmount()) // 실제 DB 필드명
                        .paymentMethod(pay.getPaymentMethod()) // 실제 DB 필드명
                        .paymentStatus(pay.getPaymentStatus()) // 실제 DB 필드명
                        .paymentDate(pay.getPaymentDate()) // 실제 DB 필드명
                        .description(pay.getDescription()) // 실제 DB 필드명
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getAllApplicants() {
        return applyRepository.findAll().stream().map(apply -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("applyNo", apply.getApplyNo());
            m.put("jobNo", apply.getJobNo());
            m.put("userNo", apply.getUserNo());
            m.put("resumeNo", apply.getResumeNo());
            m.put("applyDate", apply.getApplyDate());
            m.put("status", apply.getStatus());
            m.put("message", apply.getMessage());
            jobopeningRepository.findById(apply.getJobNo()).ifPresent(job -> {
                m.put("jobTitle", job.getTitle());
                m.put("company", job.getCompany());
            });
            userRepository.findById(apply.getUserNo()).ifPresent(user -> {
                m.put("applicantName", user.getUserRealName());
                m.put("applicantEmail", user.getEmail());
            });
            return m;
        }).collect(Collectors.toList());
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