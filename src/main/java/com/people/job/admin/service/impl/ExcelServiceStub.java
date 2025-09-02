package com.people.job.admin.service.impl;

import com.people.job.admin.service.ExcelService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public class ExcelServiceStub implements ExcelService {

    private ResponseEntity<byte[]> emptyFile(String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(filename).build()
        );
        return new ResponseEntity<>(new byte[0], headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<byte[]> exportUsersToExcel() {
        return emptyFile("users.xlsx");
    }

    @Override
    public ResponseEntity<byte[]> exportJobsToExcel() {
        return emptyFile("jobs.xlsx");
    }

    @Override
    public ResponseEntity<byte[]> exportInquiriesToExcel() {
        return emptyFile("inquiries.xlsx");
    }

    @Override
    public ResponseEntity<byte[]> exportApplicantsToExcel(Long jobNo) {
        return emptyFile("applicants-" + jobNo + ".xlsx");
    }

    @Override
    public ResponseEntity<byte[]> exportPaymentsToExcel() {
        return emptyFile("payments.xlsx");
    }

    @Override
    public ResponseEntity<byte[]> exportCustomDataToExcel(
            List<Map<String, Object>> data, String fileName, List<String> headers) {
        return emptyFile((fileName == null ? "export" : fileName) + ".xlsx");
    }
}
