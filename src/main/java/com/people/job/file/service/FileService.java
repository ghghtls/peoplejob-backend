package com.people.job.file.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

public interface FileService {

    String uploadResumeImage(MultipartFile file, Integer resumeId) throws Exception;

    Map<String, String> uploadResumeFile(MultipartFile file, Integer resumeId) throws Exception;

    Map<String, String> uploadJobFile(MultipartFile file, Integer jobId) throws Exception;

    String uploadBoardImage(MultipartFile file) throws Exception;

    Map<String, String> uploadBoardFile(MultipartFile file) throws Exception;

    Map<String, String> uploadJobAttachment(MultipartFile file) throws Exception;

    boolean deleteFile(String fileUrl) throws Exception;

    ResponseEntity<?> downloadFile(String fileUrl) throws Exception;

    byte[] downloadMultipleAsZip(List<String> fileUrls, String zipFileName) throws Exception;

    Map<String, Object> getFileList(String type, int page, int size) throws Exception;

    boolean isValidImageFile(MultipartFile file);

    boolean isValidDocumentFile(MultipartFile file);

    String generateFileName(String originalName, String prefix);
}