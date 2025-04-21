package com.people.job.payment.repository;

import com.people.job.payment.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    List<PaymentEntity> findByUserNo(Long userNo);

    List<PaymentEntity> findByJobopeningNo(Long jobopeningNo);
}
