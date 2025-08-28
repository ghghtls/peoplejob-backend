package com.people.job.peoplejob_backend.file.controller;

import com.people.job.file.controller.FileController;
import com.people.job.file.service.FileService;
import com.people.job.file.service.FileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
@ActiveProfiles("test")
@DisplayName("파일 컨트롤러 테스트")
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    private MockMultipartFile testImageFile;
    private MockMultipartFile testDocumentFile;

    @BeforeEach
    void setUp() {
        testImageFile = new MockMultipartFile(
                "file",
                "profile.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake image content".getBytes()
        );

        testDocumentFile = new MockMultipartFile(
                "file",
                "resume.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "fake pdf content".getBytes()
        );
    }

    @Test
    @DisplayName("이력서 이미지 업로드 성공 테스트")
    void uploadResumeImageSuccess() throws Exception {
        // Given
        String fileUrl = "/uploads/resume/image/profile.jpg";
        when(fileService.uploadResumeImage(any(), anyInt())).thenReturn(fileUrl);

        // When & Then
        mockMvc.perform(multipart("/api/files/upload/resume/image") // 실제 매핑 경로
                        .file(testImageFile)
                        .param("resumeId", "1")
                        .param("type", "resume_image"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileUrl").value(fileUrl));
    }

    @Test
    @DisplayName("이력서 파일 업로드 성공 테스트")
    void uploadResumeFileSuccess() throws Exception {
        // Given
        Map<String, String> result = new HashMap<>();
        result.put("fileUrl", "/uploads/resume/file/resume.pdf");
        result.put("fileName", "resume.pdf");
        result.put("fileSize", "1024");

        when(fileService.uploadResumeFile(any(), anyInt())).thenReturn(result);

        // When & Then
        mockMvc.perform(multipart("/api/files/upload/resume/file") // 실제 매핑 경로
                        .file(testDocumentFile)
                        .param("resumeId", "1")
                        .param("type", "resume_file"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileUrl").value("/uploads/resume/file/resume.pdf"))
                .andExpect(jsonPath("$.fileName").value("resume.pdf"));
    }

    @Test
    @DisplayName("채용공고 파일 업로드 성공 테스트")
    void uploadJobFileSuccess() throws Exception {
        // Given
        Map<String, String> result = new HashMap<>();
        result.put("fileUrl", "/uploads/job/file/job_description.pdf");
        result.put("fileName", "job_description.pdf");

        when(fileService.uploadJobFile(any(), anyInt())).thenReturn(result);

        // When & Then
        mockMvc.perform(multipart("/api/files/upload/job/file") // 실제 매핑 경로
                        .file(testDocumentFile)
                        .param("jobId", "1")
                        .param("type", "job_file"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileUrl").value("/uploads/job/file/job_description.pdf"));
    }

    @Test
    @DisplayName("게시판 이미지 업로드 성공 테스트")
    void uploadBoardImageSuccess() throws Exception {
        // Given
        String fileUrl = "/uploads/board/image/board_image.jpg";
        when(fileService.uploadBoardImage(any())).thenReturn(fileUrl);

        // When & Then
        mockMvc.perform(multipart("/api/files/upload/board/image") // 실제 매핑 경로
                        .file(testImageFile)
                        .param("type", "board_image"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileUrl").value(fileUrl));
    }

    @Test
    @DisplayName("파일 삭제 성공 테스트")
    void deleteFileSuccess() throws Exception {
        // Given
        when(fileService.deleteFile(anyString())).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/files/delete") // 실제 매핑 경로
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileUrl\":\"/uploads/test.jpg\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("파일 삭제 완료")); // 실제 응답 메시지
    }

    @Test
    @DisplayName("파일 다운로드 성공 테스트")
    void downloadFileSuccess() throws Exception {
        // Given
        ResponseEntity<?> response = ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=test.pdf")
                .body("file content".getBytes());


        // When & Then
        mockMvc.perform(get("/api/files/download") // 실제 매핑 경로
                        .param("fileUrl", "/uploads/test.pdf"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("파일 목록 조회 성공 테스트")
    void getFileListSuccess() throws Exception {
        // Given
        Map<String, Object> result = new HashMap<>();
        result.put("files", java.util.Arrays.asList(
                Map.of("fileName", "file1.jpg", "fileSize", 1024L),
                Map.of("fileName", "file2.pdf", "fileSize", 2048L)
        ));
        result.put("totalCount", 2);
        result.put("totalPages", 1);

        when(fileService.getFileList(anyString(), anyInt(), anyInt())).thenReturn(result);

        // When & Then
        mockMvc.perform(get("/api/files/admin/list") // 실제 매핑 경로
                        .param("type", "resume_image")
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.files").isArray());
    }

    @Test
    @DisplayName("파일 정보 조회 성공 테스트")
    void getFileInfoSuccess() throws Exception {
        // Given
        Map<String, Object> fileInfo = new HashMap<>();
        fileInfo.put("fileName", "test.jpg");
        fileInfo.put("fileSize", 1024L);
        fileInfo.put("fileType", "image/jpeg");
        fileInfo.put("uploadDate", "2024-01-01");

        // FileServiceImpl을 Mock으로 처리
        FileServiceImpl fileServiceImpl = mock(FileServiceImpl.class);
        when(fileServiceImpl.getFileInfo(anyString())).thenReturn(fileInfo);

        // When & Then
        mockMvc.perform(get("/api/files/info") // 실제 매핑 경로
                        .param("fileUrl", "/uploads/test.jpg"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("파일 업로드 실패 테스트 - 파일 크기 초과")
    void uploadFileFailSizeExceeded() throws Exception {
        // Given
        when(fileService.uploadResumeImage(any(), anyInt()))
                .thenThrow(new RuntimeException("파일 크기가 너무 큽니다."));

        // When & Then
        mockMvc.perform(multipart("/api/files/upload/resume/image")
                        .file(testImageFile)
                        .param("resumeId", "1"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("이미지 업로드 실패: 파일 크기가 너무 큽니다."));
    }

    @Test
    @DisplayName("파일 업로드 실패 테스트 - 지원하지 않는 형식")
    void uploadFileFailUnsupportedFormat() throws Exception {
        // Given
        MockMultipartFile unsupportedFile = new MockMultipartFile(
                "file",
                "virus.exe",
                "application/octet-stream",
                "malicious content".getBytes()
        );

        when(fileService.uploadResumeFile(any(), anyInt()))
                .thenThrow(new RuntimeException("지원하지 않는 파일 형식입니다."));

        // When & Then
        mockMvc.perform(multipart("/api/files/upload/resume/file")
                        .file(unsupportedFile)
                        .param("resumeId", "1"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("파일 업로드 실패: 지원하지 않는 파일 형식입니다."));
    }

    @Test
    @DisplayName("파일 삭제 실패 테스트")
    void deleteFileFailure() throws Exception {
        // Given
        when(fileService.deleteFile(anyString())).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/files/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileUrl\":\"/uploads/nonexistent.jpg\"}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("파일 삭제 실패"));
    }

    @Test
    @DisplayName("파일 다운로드 실패 테스트 - 파일 없음")
    void downloadFileNotFound() throws Exception {
        // Given
        when(fileService.downloadFile(anyString()))
                .thenThrow(new RuntimeException("파일을 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/api/files/download")
                        .param("fileUrl", "/uploads/nonexistent.pdf"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("파일 다운로드 실패: 파일을 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("빈 파일 업로드 시도 테스트")
    void uploadEmptyFile() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.txt",
                MediaType.TEXT_PLAIN_VALUE,
                new byte[0]
        );

        when(fileService.uploadResumeFile(any(), anyInt()))
                .thenThrow(new RuntimeException("빈 파일은 업로드할 수 없습니다."));

        // When & Then
        mockMvc.perform(multipart("/api/files/upload/resume/file")
                        .file(emptyFile)
                        .param("resumeId", "1"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("파일 업로드 실패: 빈 파일은 업로드할 수 없습니다."));
    }
}