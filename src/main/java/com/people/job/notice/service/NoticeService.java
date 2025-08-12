package com.people.job.notice.service;

import com.people.job.notice.dto.NoticeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NoticeService {

    // 공지사항 등록
    Long createNotice(NoticeDTO noticeDTO);

    // 공지사항 전체 조회 (활성화된 것만)
    List<NoticeDTO> getAllActiveNotices();

    // 공지사항 페이징 조회
    Page<NoticeDTO> getActiveNotices(Pageable pageable);

    // 공지사항 상세 조회
    NoticeDTO getNoticeDetail(Long noticeNo);

    // 공지사항 수정
    void updateNotice(NoticeDTO noticeDTO);

    // 공지사항 삭제 (논리 삭제 - 비활성화)
    void deleteNotice(Long noticeNo);

    // 공지사항 물리 삭제 (관리자용)
    void permanentDeleteNotice(Long noticeNo);

    // 중요 공지사항 조회
    List<NoticeDTO> getImportantNotices();

    // 공지사항 검색
    Page<NoticeDTO> searchNotices(String keyword, Pageable pageable);

    // 제목으로 검색
    Page<NoticeDTO> searchByTitle(String title, Pageable pageable);

    // 내용으로 검색
    Page<NoticeDTO> searchByContent(String content, Pageable pageable);

    // 조회수 증가
    void increaseViewCount(Long noticeNo);

    // 최근 공지사항 조회 (메인 페이지용)
    List<NoticeDTO> getRecentNotices(int limit);

    // 관리자용: 모든 공지사항 조회 (비활성화 포함)
    Page<NoticeDTO> getAllNoticesForAdmin(Pageable pageable);

    // 작성자별 공지사항 조회
    List<NoticeDTO> getNoticesByWriter(String writer);

    // 공지사항 활성화/비활성화
    void toggleNoticeStatus(Long noticeNo);

    // 중요 공지 설정/해제
    void toggleImportantStatus(Long noticeNo);
}