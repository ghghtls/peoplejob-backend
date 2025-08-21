package com.people.job.file.controller;

import com.people.job.file.service.FileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("파일 컨트롤러 테스트")
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileService fileService;

    @Test
    @DisplayName("파일 업로드 테스트")
    void uploadFile() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "test content".getBytes());

        Map<String, String> uploadResult = new HashMap<>();
        uploadResult.put("filename", "test.txt");
        uploadResult.put("originalFilename", "test.txt");
        uploadResult.put("filePath", "/uploads/test.txt");
        uploadResult.put("fileSize", "12");

        when(fileService.uploadFile(any())).thenReturn(uploadResult);

        // When & Then
        mockMvc.perform(multipart("/api/files/upload").file(file))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value("test.txt"))
                .andExpect(jsonPath("$.originalFilename").value("test.txt"));
    }

    @Test
    @DisplayName("이미지 파일 업로드 테스트")
    void uploadImageFile() throws Exception {
        // Given
        MockMultipartFile imageFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "image content".getBytes());

        Map<String, String> uploadResult = new HashMap<>();
        uploadResult.put("filename", "test.jpg");
        uploadResult.put("originalFilename", "test.jpg");
        uploadResult.put("filePath", "/uploads/images/test.jpg");
        uploadResult.put("fileSize", "13");

        when(fileService.uploadImageFile(any())).thenReturn(uploadResult);

        // When & Then
        mockMvc.perform(multipart("/api/files/upload/image").file(imageFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value("test.jpg"))
                .andExpect(jsonPath("$.filePath").value("/uploads/images/test.jpg"));
    }

    @Test
    @DisplayName("파일 다운로드 테스트")
    void downloadFile() throws Exception {
        // Given
        String filename = "test.txt";
        byte[] fileContent = "test content".getBytes();

        when(fileService.downloadFile(filename)).thenReturn(fileContent);

        // When & Then
        mockMvc.perform(get("/api/files/download/{filename}", filename))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + filename + "\""));
    }

    @Test
    @DisplayName("파일 삭제 테스트")
    void deleteFile() throws Exception {
        // Given
        String filename = "test.txt";

        when(fileService.deleteFile(filename)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/files/delete/{filename}", filename))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("파일이 성공적으로 삭제되었습니다."));
    }

    @Test
    @DisplayName("빈 파일 업로드 시 400 에러 테스트")
    void uploadEmptyFile() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.txt", "text/plain", new byte[0]);

        when(fileService.uploadFile(any()))
                .thenThrow(new IllegalArgumentException("빈 파일은 업로드할 수 없습니다."));

        // When & Then
        mockMvc.perform(multipart("/api/files/upload").file(emptyFile))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("허용되지 않은 파일 확장자 업로드 시 400 에러 테스트")
    void uploadInvalidFileExtension() throws Exception {
        // Given
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file", "test.exe", "application/octet-stream", "executable content".getBytes());

        when(fileService.uploadFile(any()))
                .thenThrow(new IllegalArgumentException("허용되지 않은 파일 확장자입니다."));

        // When & Then
        mockMvc.perform(multipart("/api/files/upload").file(invalidFile))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 파일 다운로드 시 404 에러 테스트")
    void downloadNonExistentFile() throws Exception {
        // Given
        String filename = "nonexistent.txt";

        when(fileService.downloadFile(filename))
                .thenThrow(new RuntimeException("파일을 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/api/files/download/{filename}", filename))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("파일 크기 제한 초과 시 400 에러 테스트")
    void uploadOversizedFile() throws Exception {
        // Given
        byte[] largeContent = new byte[10 * 1024 * 1024]; // 10MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "large.txt", "text/plain", largeContent);

        when(fileService.uploadFile(any()))
                .thenThrow(new IllegalArgumentException("파일 크기가 제한을 초과했습니다."));

        // When & Then
        mockMvc.perform(multipart("/api/files/upload").file(largeFile))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}