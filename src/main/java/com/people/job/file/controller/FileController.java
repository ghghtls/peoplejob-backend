package com.people.job.file.controller;

import com.people.job.file.service.FileService;
import com.people.job.file.service.FileServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    // 이력서 이미지 업로드
    @PostMapping("/upload/resume/image")
    public ResponseEntity<?> uploadResumeImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("resumeId") Integer resumeId,
            @RequestParam(value = "type", defaultValue = "resume_image") String type) {

        try {
            String fileUrl = fileService.uploadResumeImage(file, resumeId);
            return ResponseEntity.ok(Map.of("fileUrl", fileUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "이미지 업로드 실패: " + e.getMessage()));
        }
    }

    // 이력서 파일 업로드
    @PostMapping("/upload/resume/file")
    public ResponseEntity<?> uploadResumeFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("resumeId") Integer resumeId,
            @RequestParam(value = "type", defaultValue = "resume_file") String type) {

        try {
            Map<String, String> result = fileService.uploadResumeFile(file, resumeId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "파일 업로드 실패: " + e.getMessage()));
        }
    }

    // 채용공고 파일 업로드
    @PostMapping("/upload/job/file")
    public ResponseEntity<?> uploadJobFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jobId") Integer jobId,
            @RequestParam(value = "type", defaultValue = "job_file") String type) {

        try {
            Map<String, String> result = fileService.uploadJobFile(file, jobId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "파일 업로드 실패: " + e.getMessage()));
        }
    }

    // 게시판 이미지 업로드
    @PostMapping("/upload/board/image")
    public ResponseEntity<?> uploadBoardImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "board_image") String type) {

        try {
            String fileUrl = fileService.uploadBoardImage(file);
            return ResponseEntity.ok(Map.of("fileUrl", fileUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "이미지 업로드 실패: " + e.getMessage()));
        }
    }

    // 채용공고 첨부파일 업로드 (jobId 불필요)
    @PostMapping("/upload/job/attachment")
    public ResponseEntity<?> uploadJobAttachment(
            @RequestParam("file") MultipartFile file) {
        try {
            Map<String, String> result = fileService.uploadJobAttachment(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "파일 업로드 실패: " + e.getMessage()));
        }
    }

    // 게시판 파일 업로드 (이미지 + 문서)
    @PostMapping("/upload/board/file")
    public ResponseEntity<?> uploadBoardFile(
            @RequestParam("file") MultipartFile file) {
        try {
            Map<String, String> result = fileService.uploadBoardFile(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "파일 업로드 실패: " + e.getMessage()));
        }
    }

    // 파일 삭제
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteFile(@RequestBody Map<String, String> request) {
        try {
            String fileUrl = request.get("fileUrl");
            boolean success = fileService.deleteFile(fileUrl);

            if (success) {
                return ResponseEntity.ok(Map.of("message", "파일 삭제 완료"));
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "파일 삭제 실패"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "파일 삭제 중 오류: " + e.getMessage()));
        }
    }

    // 다중 파일 ZIP 다운로드
    @PostMapping("/download/multiple")
    public ResponseEntity<byte[]> downloadMultiple(@RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<String> fileUrls = (List<String>) body.get("fileUrls");
            String zipFileName = (String) body.getOrDefault("zipFileName", "download.zip");
            byte[] zipBytes = fileService.downloadMultipleAsZip(fileUrls, zipFileName);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + zipFileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(zipBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 파일 다운로드
    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(@RequestParam("fileUrl") String fileUrl) {
        try {
            return fileService.downloadFile(fileUrl);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "파일 다운로드 실패: " + e.getMessage()));
        }
    }

    // 관리자용 파일 목록 조회
    @GetMapping("/admin/list")
    public ResponseEntity<?> getFileList(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        try {
            Map<String, Object> result = fileService.getFileList(type, page, size);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "파일 목록 조회 실패: " + e.getMessage()));
        }
    }

    // 파일 정보 조회
    @GetMapping("/info")
    public ResponseEntity<?> getFileInfo(@RequestParam("fileUrl") String fileUrl) {
        try {
            Map<String, Object> fileInfo = ((FileServiceImpl) fileService).getFileInfo(fileUrl);
            return ResponseEntity.ok(fileInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "파일 정보 조회 실패: " + e.getMessage()));
        }
    }
}