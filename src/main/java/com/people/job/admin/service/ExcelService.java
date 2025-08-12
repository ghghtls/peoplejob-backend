package com.people.job.admin.service;

import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;

public interface ExcelService {

    ResponseEntity<byte[]> exportUsersToExcel() throws Exception;

    ResponseEntity<byte[]> exportJobsToExcel() throws Exception;

    ResponseEntity<byte[]> exportInquiriesToExcel() throws Exception;

    ResponseEntity<byte[]> exportApplicantsToExcel(Long jobNo) throws Exception;

    ResponseEntity<byte[]> exportPaymentsToExcel() throws Exception;

    ResponseEntity<byte[]> exportCustomDataToExcel(List<Map<String, Object>> data, String fileName, List<String> headers) throws Exception;
}