package com.people.job.admin.service.impl;

import com.people.job.admin.service.ExcelService;
import com.people.job.apply.repository.ApplyRepository;
import com.people.job.inquiry.repository.InquiryRepository;
import com.people.job.job.repository.JobopeningRepository;
import com.people.job.payment.repository.PaymentRepository;
import com.people.job.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService {

    private final UserRepository userRepository;
    private final JobopeningRepository jobRepository;
    private final InquiryRepository inquiryRepository;
    private final ApplyRepository applyRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public ResponseEntity<byte[]> exportUsersToExcel() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("회원목록");
            CellStyle header = headerStyle(wb);

            String[] cols = {"회원번호", "아이디", "이름", "이메일", "전화번호", "회원유형", "역할", "활성여부"};
            writeHeader(sheet, header, cols);

            int row = 1;
            for (var u : userRepository.findAll()) {
                Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(safe(u.getUserNo()));
                r.createCell(1).setCellValue(safe(u.getUserid()));
                r.createCell(2).setCellValue(safe(u.getUserRealName()));
                r.createCell(3).setCellValue(safe(u.getEmail()));
                r.createCell(4).setCellValue(safe(u.getPhone()));
                r.createCell(5).setCellValue(u.getUserType() != null ? u.getUserType().name() : "");
                r.createCell(6).setCellValue(u.getRole() != null ? u.getRole().name() : "");
                r.createCell(7).setCellValue(u.isAccountNonLocked() ? "활성" : "비활성");
            }
            autoSize(sheet, cols.length);
            return toResponse(wb, "회원목록.xlsx");
        }
    }

    @Override
    public ResponseEntity<byte[]> exportJobsToExcel() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("채용공고");
            CellStyle header = headerStyle(wb);

            String[] cols = {"공고번호", "회사명", "제목", "위치", "고용형태", "급여", "마감일", "상태"};
            writeHeader(sheet, header, cols);

            int row = 1;
            for (var j : jobRepository.findAll()) {
                Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(safe(j.getJobNo()));
                r.createCell(1).setCellValue(safe(j.getCompany()));
                r.createCell(2).setCellValue(safe(j.getTitle()));
                r.createCell(3).setCellValue(safe(j.getLocation()));
                r.createCell(4).setCellValue(safe(j.getJobType()));
                r.createCell(5).setCellValue(safe(j.getSalary()));
                r.createCell(6).setCellValue(j.getDeadline() != null ? j.getDeadline().toString() : "");
                r.createCell(7).setCellValue(j.getStatus() != null ? j.getStatus().name() : "");
            }
            autoSize(sheet, cols.length);
            return toResponse(wb, "채용공고목록.xlsx");
        }
    }

    @Override
    public ResponseEntity<byte[]> exportInquiriesToExcel() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("문의사항");
            CellStyle header = headerStyle(wb);

            String[] cols = {"문의번호", "작성자", "이메일", "카테고리", "제목", "답변여부", "등록일"};
            writeHeader(sheet, header, cols);

            int row = 1;
            for (var i : inquiryRepository.findAll()) {
                Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(safe(i.getInquiryNo()));
                r.createCell(1).setCellValue(safe(i.getWriter()));
                r.createCell(2).setCellValue(safe(i.getEmail()));
                r.createCell(3).setCellValue(safe(i.getCategory()));
                r.createCell(4).setCellValue(safe(i.getTitle()));
                r.createCell(5).setCellValue(Boolean.TRUE.equals(i.getIsAnswered()) ? "답변완료" : "답변대기");
                r.createCell(6).setCellValue(i.getRegdate() != null ? i.getRegdate().toString() : "");
            }
            autoSize(sheet, cols.length);
            return toResponse(wb, "문의사항목록.xlsx");
        }
    }

    @Override
    public ResponseEntity<byte[]> exportApplicantsToExcel(Long jobNo) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("지원자목록");
            CellStyle header = headerStyle(wb);

            String[] cols = {"지원번호", "공고번호", "회원번호", "이력서번호", "지원일", "상태", "메시지"};
            writeHeader(sheet, header, cols);

            var list = (jobNo == null || jobNo == 0)
                    ? applyRepository.findAll()
                    : applyRepository.findAll().stream()
                        .filter(a -> jobNo.equals(a.getJobNo())).toList();

            int row = 1;
            for (var a : list) {
                Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(safe(a.getApplyNo()));
                r.createCell(1).setCellValue(safe(a.getJobNo()));
                r.createCell(2).setCellValue(safe(a.getUserNo()));
                r.createCell(3).setCellValue(safe(a.getResumeNo()));
                r.createCell(4).setCellValue(a.getApplyDate() != null ? a.getApplyDate().toString() : "");
                r.createCell(5).setCellValue(safe(a.getStatus()));
                r.createCell(6).setCellValue(safe(a.getMessage()));
            }
            autoSize(sheet, cols.length);
            return toResponse(wb, "지원자목록.xlsx");
        }
    }

    @Override
    public ResponseEntity<byte[]> exportPaymentsToExcel() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("결제내역");
            CellStyle header = headerStyle(wb);

            String[] cols = {"결제번호", "회원번호", "금액", "결제방법", "상태", "결제일", "설명"};
            writeHeader(sheet, header, cols);

            int row = 1;
            for (var p : paymentRepository.findAll()) {
                Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(safe(p.getPaymentNo()));
                r.createCell(1).setCellValue(safe(p.getUserNo()));
                r.createCell(2).setCellValue(p.getAmount() != null ? p.getAmount().toPlainString() : "");
                r.createCell(3).setCellValue(safe(p.getPaymentMethod()));
                r.createCell(4).setCellValue(safe(p.getPaymentStatus()));
                r.createCell(5).setCellValue(p.getPaymentDate() != null ? p.getPaymentDate().toString() : "");
                r.createCell(6).setCellValue(safe(p.getDescription()));
            }
            autoSize(sheet, cols.length);
            return toResponse(wb, "결제내역목록.xlsx");
        }
    }

    @Override
    public ResponseEntity<byte[]> exportCustomDataToExcel(
            List<Map<String, Object>> data, String fileName, List<String> headers) throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("데이터");
            CellStyle headerStyle = headerStyle(wb);

            if (headers != null && !headers.isEmpty()) {
                writeHeader(sheet, headerStyle, headers.toArray(new String[0]));
                int row = 1;
                for (var record : data) {
                    Row r = sheet.createRow(row++);
                    int col = 0;
                    for (String h : headers) {
                        Object val = record.get(h);
                        r.createCell(col++).setCellValue(val != null ? val.toString() : "");
                    }
                }
                autoSize(sheet, headers.size());
            }
            return toResponse(wb, (fileName != null ? fileName : "export") + ".xlsx");
        }
    }

    // ── 공통 유틸 ────────────────────────────────────────────────────────────

    private CellStyle headerStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private void writeHeader(Sheet sheet, CellStyle style, String[] cols) {
        Row row = sheet.createRow(0);
        for (int i = 0; i < cols.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(cols[i]);
            cell.setCellStyle(style);
        }
    }

    private void autoSize(Sheet sheet, int colCount) {
        for (int i = 0; i < colCount; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private ResponseEntity<byte[]> toResponse(XSSFWorkbook wb, String filename) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        byte[] bytes = out.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8).build());
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    private String safe(Object val) {
        return val != null ? val.toString() : "";
    }
}
