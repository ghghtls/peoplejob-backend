package com.people.job.inquiry.repository;

import com.people.job.inquiry.entity.InquiryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryRepository extends JpaRepository<InquiryEntity, Long> {


}
