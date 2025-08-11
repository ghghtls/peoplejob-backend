package com.people.job.file.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface FileService {

    String uploadResumeImage(MultipartFile file, Integer resumeId) throws Exception;

    Map<String, String> uploadResumeFile(MultipartFile file, Integer resumeId) throws Exception;

    Map<String, String> uploadJobFile(MultipartFile file, Integer jobId) throws Exception;

    String uploadBoardImage(MultipartFile file) throws Exception;

    boolean deleteFile(String fileUrl) throws Exception;

    boolean isValidImageFile(MultipartFile file);

    boolean isValidDocumentFile(MultipartFile file);

    String generateFileName(String originalName, String prefix);
}