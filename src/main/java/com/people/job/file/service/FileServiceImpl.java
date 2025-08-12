package com.people.job.file.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    @Value("${file.upload.path:/uploads}")
    private String uploadPath;

    @Value("${file.upload.url:http://localhost:8080/uploads}")
    private String uploadUrl;

    // 허용된 이미지 확장자
    private final List<String> ALLOWED_IMAGE_EXTENSIONS =
            Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");

    // 허용된 문서 확장자
    private final List<String> ALLOWED_DOCUMENT_EXTENSIONS =
            Arrays.asList("pdf", "doc", "docx", "hwp", "txt", "xls", "xlsx");

    // 최대 파일 크기 (바이트)
    private final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private final long MAX_DOCUMENT_SIZE = 10 * 1024 * 1024; // 10MB
    private final long MAX_JOB_FILE_SIZE = 15 * 1024 * 1024; // 15MB

    @Override
    public String uploadResumeImage(MultipartFile file, Integer resumeId) throws Exception {
        validateImageFile(file);

        String fileName = generateFileName(file.getOriginalFilename(), "resume_" + resumeId + "_image");
        String subDirectory = "resume/images/" + resumeId;

        return saveFile(file, fileName, subDirectory);
    }

    @Override
    public Map<String, String> uploadResumeFile(MultipartFile file, Integer resumeId) throws Exception {
        validateDocumentFile(file);

        String fileName = generateFileName(file.getOriginalFilename(), "resume_" + resumeId + "_file");
        String subDirectory = "resume/files/" + resumeId;

        String fileUrl = saveFile(file, fileName, subDirectory);

        Map<String, String> result = new HashMap<>();
        result.put("fileUrl", fileUrl);
        result.put("originalName", file.getOriginalFilename());
        result.put("fileName", fileName);
        result.put("fileSize", String.valueOf(file.getSize()));

        return result;
    }

    @Override
    public Map<String, String> uploadJobFile(MultipartFile file, Integer jobId) throws Exception {
        validateJobFile(file);

        String fileName = generateFileName(file.getOriginalFilename(), "job_" + jobId + "_file");
        String subDirectory = "job/files/" + jobId;

        String fileUrl = saveFile(file, fileName, subDirectory);

        Map<String, String> result = new HashMap<>();
        result.put("fileUrl", fileUrl);
        result.put("originalName", file.getOriginalFilename());
        result.put("fileName", fileName);
        result.put("fileSize", String.valueOf(file.getSize()));

        return result;
    }

    @Override
    public String uploadBoardImage(MultipartFile file) throws Exception {
        validateImageFile(file);

        String fileName = generateFileName(file.getOriginalFilename(), "board_image");
        String subDirectory = "board/images";

        return saveFile(file, fileName, subDirectory);
    }

    @Override
    public boolean deleteFile(String fileUrl) throws Exception {
        try {
            // URL에서 파일 경로 추출
            String relativePath = fileUrl.replace(uploadUrl, "");
            if (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1);
            }

            Path filePath = Paths.get(uploadPath, relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("파일 삭제 완료: {}", filePath);
                return true;
            } else {
                log.warn("삭제할 파일이 존재하지 않음: {}", filePath);
                return false;
            }
        } catch (Exception e) {
            log.error("파일 삭제 실패: {}", fileUrl, e);
            throw new Exception("파일 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> downloadFile(String fileUrl) throws Exception {
        try {
            // URL에서 파일 경로 추출
            String relativePath = fileUrl.replace(uploadUrl, "");
            if (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1);
            }

            Path filePath = Paths.get(uploadPath, relativePath);

            if (!Files.exists(filePath)) {
                throw new Exception("파일이 존재하지 않습니다: " + fileUrl);
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new Exception("파일을 읽을 수 없습니다: " + fileUrl);
            }

            // 파일명에서 원본 파일명 추출 (가능하면)
            String fileName = filePath.getFileName().toString();
            String originalFileName = extractOriginalFileName(fileName);

            // Content-Type 결정
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            log.info("파일 다운로드: {} -> {}", fileUrl, originalFileName);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + originalFileName + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            log.error("잘못된 파일 URL: {}", fileUrl, e);
            throw new Exception("잘못된 파일 URL입니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("파일 다운로드 실패: {}", fileUrl, e);
            throw new Exception("파일 다운로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getFileList(String type, int page, int size) throws Exception {
        try {
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                return createEmptyFileList(page, size);
            }

            // 파일 목록 수집
            List<Map<String, Object>> fileList;
            try (Stream<Path> walk = Files.walk(uploadDir)) {
                Stream<Path> fileStream = walk.filter(Files::isRegularFile);

                // 타입별 필터링
                if (type != null && !type.isEmpty()) {
                    fileStream = fileStream.filter(path -> {
                        String pathStr = path.toString();
                        return switch (type.toLowerCase()) {
                            case "resume" -> pathStr.contains("/resume/");
                            case "job" -> pathStr.contains("/job/");
                            case "board" -> pathStr.contains("/board/");
                            case "image" -> isImagePath(pathStr);
                            case "document" -> isDocumentPath(pathStr);
                            default -> true;
                        };
                    });
                }

                fileList = fileStream
                        .map(this::createFileInfo)
                        .sorted((f1, f2) -> ((String) f2.get("lastModified"))
                                .compareTo((String) f1.get("lastModified")))
                        .collect(Collectors.toList());
            }

            // 페이징 처리
            int totalElements = fileList.size();
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, totalElements);

            List<Map<String, Object>> pagedList = fromIndex < totalElements
                    ? fileList.subList(fromIndex, toIndex)
                    : List.of();

            Map<String, Object> result = new HashMap<>();
            result.put("content", pagedList);
            result.put("totalElements", totalElements);
            result.put("totalPages", (int) Math.ceil((double) totalElements / size));
            result.put("currentPage", page);
            result.put("size", size);
            result.put("hasNext", toIndex < totalElements);
            result.put("hasPrevious", page > 0);

            return result;

        } catch (IOException e) {
            log.error("파일 목록 조회 실패", e);
            throw new Exception("파일 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Override
    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String extension = getFileExtension(file.getOriginalFilename());
        return ALLOWED_IMAGE_EXTENSIONS.contains(extension.toLowerCase());
    }

    @Override
    public boolean isValidDocumentFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String extension = getFileExtension(file.getOriginalFilename());
        return ALLOWED_DOCUMENT_EXTENSIONS.contains(extension.toLowerCase());
    }

    @Override
    public String generateFileName(String originalName, String prefix) {
        String extension = getFileExtension(originalName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return String.format("%s_%s_%s.%s", prefix, timestamp, uuid, extension);
    }

    // Private helper methods

    private String saveFile(MultipartFile file, String fileName, String subDirectory) throws Exception {
        try {
            // 디렉토리 생성
            Path directoryPath = Paths.get(uploadPath, subDirectory);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            // 파일 저장
            Path filePath = directoryPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            // URL 생성
            String fileUrl = uploadUrl + "/" + subDirectory + "/" + fileName;

            log.info("파일 업로드 완료: {} -> {}", file.getOriginalFilename(), fileUrl);

            return fileUrl;
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", fileName, e);
            throw new Exception("파일 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void validateImageFile(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new Exception("파일이 비어있습니다.");
        }

        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new Exception("이미지 파일 크기는 5MB를 초과할 수 없습니다.");
        }

        if (!isValidImageFile(file)) {
            throw new Exception("허용되지 않은 이미지 파일 형식입니다. (jpg, jpeg, png, gif, bmp, webp만 허용)");
        }

        // MIME 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new Exception("올바른 이미지 파일이 아닙니다.");
        }
    }

    private void validateDocumentFile(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new Exception("파일이 비어있습니다.");
        }

        if (file.getSize() > MAX_DOCUMENT_SIZE) {
            throw new Exception("문서 파일 크기는 10MB를 초과할 수 없습니다.");
        }

        if (!isValidDocumentFile(file)) {
            throw new Exception("허용되지 않은 문서 파일 형식입니다. (pdf, doc, docx, hwp, txt, xls, xlsx만 허용)");
        }
    }

    private void validateJobFile(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new Exception("파일이 비어있습니다.");
        }

        if (file.getSize() > MAX_JOB_FILE_SIZE) {
            throw new Exception("파일 크기는 15MB를 초과할 수 없습니다.");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        List<String> allowedExtensions = Arrays.asList("pdf", "doc", "docx", "hwp", "txt", "xls", "xlsx", "ppt", "pptx");

        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new Exception("허용되지 않은 파일 형식입니다.");
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private String extractOriginalFileName(String generatedFileName) {
        // 생성된 파일명에서 원본 파일명 추출 시도
        // 형식: prefix_timestamp_uuid.extension
        String[] parts = generatedFileName.split("_");
        if (parts.length >= 3) {
            String lastPart = parts[parts.length - 1]; // uuid.extension
            String extension = getFileExtension(lastPart);
            return "download." + extension;
        }
        return generatedFileName;
    }

    private boolean isImagePath(String path) {
        String extension = getFileExtension(path).toLowerCase();
        return ALLOWED_IMAGE_EXTENSIONS.contains(extension);
    }

    private boolean isDocumentPath(String path) {
        String extension = getFileExtension(path).toLowerCase();
        return ALLOWED_DOCUMENT_EXTENSIONS.contains(extension);
    }

    private Map<String, Object> createFileInfo(Path path) {
        Map<String, Object> fileInfo = new HashMap<>();
        try {
            String relativePath = Paths.get(uploadPath).relativize(path).toString().replace("\\", "/");
            String fileUrl = uploadUrl + "/" + relativePath;

            fileInfo.put("fileName", path.getFileName().toString());
            fileInfo.put("fileUrl", fileUrl);
            fileInfo.put("filePath", relativePath);
            fileInfo.put("fileSize", Files.size(path));
            fileInfo.put("lastModified", Files.getLastModifiedTime(path).toString());
            fileInfo.put("extension", getFileExtension(path.getFileName().toString()));
            fileInfo.put("isImage", isImagePath(path.toString()));
            fileInfo.put("isDocument", isDocumentPath(path.toString()));

            // 파일 타입 분류
            String pathStr = path.toString();
            if (pathStr.contains("/resume/")) {
                fileInfo.put("category", "resume");
            } else if (pathStr.contains("/job/")) {
                fileInfo.put("category", "job");
            } else if (pathStr.contains("/board/")) {
                fileInfo.put("category", "board");
            } else {
                fileInfo.put("category", "other");
            }

        } catch (IOException e) {
            log.error("파일 정보 생성 실패: {}", path, e);
            fileInfo.put("error", e.getMessage());
        }
        return fileInfo;
    }

    private Map<String, Object> createEmptyFileList(int page, int size) {
        Map<String, Object> result = new HashMap<>();
        result.put("content", List.of());
        result.put("totalElements", 0);
        result.put("totalPages", 0);
        result.put("currentPage", page);
        result.put("size", size);
        result.put("hasNext", false);
        result.put("hasPrevious", false);
        return result;
    }

    // 파일 정보 조회
    public Map<String, Object> getFileInfo(String fileUrl) {
        Map<String, Object> fileInfo = new HashMap<>();

        try {
            String relativePath = fileUrl.replace(uploadUrl, "");
            if (relativePath.startsWith("/")) {
                relativePath = relativePath.substring(1);
            }

            Path filePath = Paths.get(uploadPath, relativePath);

            if (Files.exists(filePath)) {
                fileInfo.put("exists", true);
                fileInfo.put("size", Files.size(filePath));
                fileInfo.put("lastModified", Files.getLastModifiedTime(filePath).toString());
                fileInfo.put("isReadable", Files.isReadable(filePath));
            } else {
                fileInfo.put("exists", false);
            }
        } catch (Exception e) {
            log.error("파일 정보 조회 실패: {}", fileUrl, e);
            fileInfo.put("exists", false);
            fileInfo.put("error", e.getMessage());
        }

        return fileInfo;
    }

    // 디렉토리 정리 (오래된 파일 삭제)
    public void cleanupOldFiles(int daysOld) {
        try {
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                return;
            }

            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);

            Files.walk(uploadDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        try {
                            return Files.getLastModifiedTime(path).toInstant()
                                    .isBefore(cutoffDate.atZone(java.time.ZoneId.systemDefault()).toInstant());
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.info("오래된 파일 삭제: {}", path);
                        } catch (IOException e) {
                            log.error("파일 삭제 실패: {}", path, e);
                        }
                    });
        } catch (Exception e) {
            log.error("파일 정리 중 오류 발생", e);
        }
    }
}