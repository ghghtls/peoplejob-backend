package com.people.job.admin.service;

import com.people.job.payment.dto.PaymentDTO;
import com.people.job.user.entity.UserEntity;
import com.people.job.job.dto.JobopeningDTO;
import com.people.job.inquiry.dto.InquiryDTO;
import com.people.job.admin.dto.DashboardDTO;

import java.util.List;
import java.util.Map;

public interface AdminService {

    List<UserEntity> getAllUsers();

    void deleteUser(Long userNo);

    List<JobopeningDTO> getAllJobopenings();

    void deleteJobopening(Long jobopeningNo);

    List<InquiryDTO> getAllInquiries();

    void deleteInquiry(Long inquiryNo);

    void answerInquiry(Long inquiryNo, String answer, String answerBy);

    List<PaymentDTO> getAllPayments();

    DashboardDTO getDashboardStats();

    List<Map<String, Object>> getAllApplicants();
}