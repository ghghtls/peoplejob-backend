package com.people.job.file.controller;

import com.people.job.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    // 파일 다운로드
    @GetMapping("/download")
    public ResponseEntity<?> downloadFile(@RequestParam("fileUrl") String fileUrl) {
        try {
            // 파일 다운로드 로직 구현
            return ResponseEntity.ok("파일 다운로드 기능 구현 예정");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "파일 다운로드 실패: " + e.getMessage()));
        }
    }
}