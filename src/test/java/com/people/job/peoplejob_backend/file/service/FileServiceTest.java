package com.people.job.peoplejob_backend.file.service;

import com.people.job.file.service.FileServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("파일 서비스 테스트")
class FileServiceTest {

    @InjectMocks
    private FileServiceImpl fileService;

    // ===== isValidImageFile =====

    @Test
    @DisplayName("이미지 파일 유효성 - null 파일")
    void isValidImageFile_null_returnsFalse() {
        assertFalse(fileService.isValidImageFile(null));
    }

    @Test
    @DisplayName("이미지 파일 유효성 - 빈 파일")
    void isValidImageFile_empty_returnsFalse() {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);
        assertFalse(fileService.isValidImageFile(file));
    }

    @Test
    @DisplayName("이미지 파일 유효성 - 허용된 확장자 (jpg)")
    void isValidImageFile_jpg_returnsTrue() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "data".getBytes());
        assertTrue(fileService.isValidImageFile(file));
    }

    @Test
    @DisplayName("이미지 파일 유효성 - 허용된 확장자 (png, gif, webp)")
    void isValidImageFile_otherImageExtensions_returnsTrue() {
        for (String ext : new String[]{"jpeg", "png", "gif", "bmp", "webp"}) {
            MockMultipartFile file = new MockMultipartFile("file", "photo." + ext, "image/" + ext, "data".getBytes());
            assertTrue(fileService.isValidImageFile(file), "Should be valid: " + ext);
        }
    }

    @Test
    @DisplayName("이미지 파일 유효성 - 허용되지 않은 확장자")
    void isValidImageFile_invalidExtension_returnsFalse() {
        MockMultipartFile file = new MockMultipartFile("file", "virus.exe", "application/octet-stream", "data".getBytes());
        assertFalse(fileService.isValidImageFile(file));
    }

    @Test
    @DisplayName("이미지 파일 유효성 - 문서 확장자는 이미지 아님")
    void isValidImageFile_documentExtension_returnsFalse() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "data".getBytes());
        assertFalse(fileService.isValidImageFile(file));
    }

    // ===== isValidDocumentFile =====

    @Test
    @DisplayName("문서 파일 유효성 - null 파일")
    void isValidDocumentFile_null_returnsFalse() {
        assertFalse(fileService.isValidDocumentFile(null));
    }

    @Test
    @DisplayName("문서 파일 유효성 - 빈 파일")
    void isValidDocumentFile_empty_returnsFalse() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", new byte[0]);
        assertFalse(fileService.isValidDocumentFile(file));
    }

    @Test
    @DisplayName("문서 파일 유효성 - 허용된 확장자 (pdf)")
    void isValidDocumentFile_pdf_returnsTrue() {
        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "data".getBytes());
        assertTrue(fileService.isValidDocumentFile(file));
    }

    @Test
    @DisplayName("문서 파일 유효성 - 허용된 확장자 (doc, docx, hwp, txt, xls, xlsx)")
    void isValidDocumentFile_otherDocExtensions_returnsTrue() {
        for (String ext : new String[]{"doc", "docx", "hwp", "txt", "xls", "xlsx"}) {
            MockMultipartFile file = new MockMultipartFile("file", "file." + ext, "application/octet-stream", "data".getBytes());
            assertTrue(fileService.isValidDocumentFile(file), "Should be valid: " + ext);
        }
    }

    @Test
    @DisplayName("문서 파일 유효성 - 허용되지 않은 확장자")
    void isValidDocumentFile_invalidExtension_returnsFalse() {
        MockMultipartFile file = new MockMultipartFile("file", "data.csv", "text/csv", "data".getBytes());
        assertFalse(fileService.isValidDocumentFile(file));
    }

    // ===== generateFileName =====

    @Test
    @DisplayName("파일명 생성 - jpg 확장자")
    void generateFileName_jpg() {
        String result = fileService.generateFileName("profile.jpg", "resume_1_image");
        assertNotNull(result);
        assertTrue(result.startsWith("resume_1_image_"), "Should start with prefix: " + result);
        assertTrue(result.endsWith(".jpg"), "Should end with .jpg: " + result);
    }

    @Test
    @DisplayName("파일명 생성 - pdf 확장자")
    void generateFileName_pdf() {
        String result = fileService.generateFileName("document.pdf", "board_file");
        assertTrue(result.startsWith("board_file_"));
        assertTrue(result.endsWith(".pdf"));
    }

    @Test
    @DisplayName("파일명 생성 - 두 파일명이 서로 다름 (UUID 포함)")
    void generateFileName_isUnique() {
        String name1 = fileService.generateFileName("file.jpg", "prefix");
        String name2 = fileService.generateFileName("file.jpg", "prefix");
        assertNotEquals(name1, name2);
    }

    // ===== getFileInfo =====

    @Test
    @DisplayName("파일 정보 조회 - 존재하지 않는 경로")
    void getFileInfo_nonExistentFile_returnsNotExist() {
        ReflectionTestUtils.setField(fileService, "uploadPath", "/tmp/nonexistent_peoplejob_xyz");
        ReflectionTestUtils.setField(fileService, "uploadUrl", "http://localhost:8080/uploads");

        Map<String, Object> info = fileService.getFileInfo("http://localhost:8080/uploads/nofile.txt");

        assertNotNull(info);
        assertFalse((Boolean) info.get("exists"));
    }
}
