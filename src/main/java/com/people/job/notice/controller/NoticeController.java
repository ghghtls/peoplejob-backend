package com.people.job.notice.controller;

import com.people.job.notice.dto.NoticeDTO;
import com.people.job.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // 공지사항 등록 (관리자용)
    @PostMapping
    public ResponseEntity<?> createNotice(@RequestBody NoticeDTO noticeDTO) {
        try {
            Long noticeId = noticeService.createNotice(noticeDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "공지사항이 등록되었습니다.");
            response.put("noticeId", noticeId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("공지사항 등록 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "공지사항 등록에 실패했습니다: " + e.getMessage()));
        }
    }

    // 공지사항 전체 목록 조회
    @GetMapping
    public ResponseEntity<List<NoticeDTO>> getAllNotices() {
        try {
            List<NoticeDTO> notices = noticeService.getAllActiveNotices();
            return ResponseEntity.ok(notices);
        } catch (Exception e) {
            log.error("공지사항 목록 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    // 공지사항 페이징 조회
    @GetMapping("/page")
    public ResponseEntity<Page<NoticeDTO>> getNoticesWithPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<NoticeDTO> notices = noticeService.getActiveNotices(pageable);
            return ResponseEntity.ok(notices);
        } catch (Exception e) {
            log.error("공지사항 페이징 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    // 공지사항 상세 조회
    @GetMapping("/{noticeNo}")
    public ResponseEntity<NoticeDTO> getNoticeDetail(@PathVariable Long noticeNo) {
        try {
            NoticeDTO notice = noticeService.getNoticeDetail(noticeNo);
            return ResponseEntity.ok(notice);
        } catch (Exception e) {
            log.error("공지사항 상세 조회 실패: ID = {}, 오류 = {}", noticeNo, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    // 공지사항 수정 (관리자용)
    @PutMapping("/{noticeNo}")
    public ResponseEntity<?> updateNotice(
            @PathVariable Long noticeNo,
            @RequestBody NoticeDTO noticeDTO) {
        try {
            noticeDTO.setNoticeNo(noticeNo);
            noticeService.updateNotice(noticeDTO);
            return ResponseEntity.ok(Map.of("message", "공지사항이 수정되었습니다."));
        } catch (Exception e) {
            log.error("공지사항 수정 실패: ID = {}, 오류 = {}", noticeNo, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "공지사항 수정에 실패했습니다: " + e.getMessage()));
        }
    }

    // 공지사항 삭제 (논리 삭제 - 비활성화)
    @DeleteMapping("/{noticeNo}")
    public ResponseEntity<?> deleteNotice(@PathVariable Long noticeNo) {
        try {
            noticeService.deleteNotice(noticeNo);
            return ResponseEntity.ok(Map.of("message", "공지사항이 삭제되었습니다."));
        } catch (Exception e) {
            log.error("공지사항 삭제 실패: ID = {}, 오류 = {}", noticeNo, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "공지사항 삭제에 실패했습니다: " + e.getMessage()));
        }
    }

    // 중요 공지사항 조회
    @GetMapping("/important")
    public ResponseEntity<List<NoticeDTO>> getImportantNotices() {
        try {
            List<NoticeDTO> notices = noticeService.getImportantNotices();
            return ResponseEntity.ok(notices);
        } catch (Exception e) {
            log.error("중요 공지사항 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    // 공지사항 검색
    @GetMapping("/search")
    public ResponseEntity<Page<NoticeDTO>> searchNotices(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<NoticeDTO> notices = noticeService.searchNotices(keyword, pageable);
            return ResponseEntity.ok(notices);
        } catch (Exception e) {
            log.error("공지사항 검색 실패: 키워드 = {}, 오류 = {}", keyword, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    // 최근 공지사항 조회 (메인 페이지용)
    @GetMapping("/recent")
    public ResponseEntity<List<NoticeDTO>> getRecentNotices(
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<NoticeDTO> notices = noticeService.getRecentNotices(limit);
            return ResponseEntity.ok(notices);
        } catch (Exception e) {
            log.error("최근 공지사항 조회 실패: limit = {}, 오류 = {}", limit, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    // 관리자용: 모든 공지사항 조회 (비활성화 포함)
    @GetMapping("/admin/all")
    public ResponseEntity<Page<NoticeDTO>> getAllNoticesForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<NoticeDTO> notices = noticeService.getAllNoticesForAdmin(pageable);
            return ResponseEntity.ok(notices);
        } catch (Exception e) {
            log.error("관리자용 공지사항 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    // 관리자용: 공지사항 활성화/비활성화 토글
    @PutMapping("/{noticeNo}/toggle-status")
    public ResponseEntity<?> toggleNoticeStatus(@PathVariable Long noticeNo) {
        try {
            noticeService.toggleNoticeStatus(noticeNo);
            return ResponseEntity.ok(Map.of("message", "공지사항 상태가 변경되었습니다."));
        } catch (Exception e) {
            log.error("공지사항 상태 변경 실패: ID = {}, 오류 = {}", noticeNo, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "공지사항 상태 변경에 실패했습니다: " + e.getMessage()));
        }
    }

    // 관리자용: 중요 공지 설정/해제 토글
    @PutMapping("/{noticeNo}/toggle-important")
    public ResponseEntity<?> toggleImportantStatus(@PathVariable Long noticeNo) {
        try {
            noticeService.toggleImportantStatus(noticeNo);
            return ResponseEntity.ok(Map.of("message", "공지사항 중요도가 변경되었습니다."));
        } catch (Exception e) {
            log.error("공지사항 중요도 변경 실패: ID = {}, 오류 = {}", noticeNo, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "공지사항 중요도 변경에 실패했습니다: " + e.getMessage()));
        }
    }

    // 관리자용: 공지사항 물리 삭제
    @DeleteMapping("/admin/{noticeNo}/permanent")
    public ResponseEntity<?> permanentDeleteNotice(@PathVariable Long noticeNo) {
        try {
            noticeService.permanentDeleteNotice(noticeNo);
            return ResponseEntity.ok(Map.of("message", "공지사항이 완전히 삭제되었습니다."));
        } catch (Exception e) {
            log.error("공지사항 물리 삭제 실패: ID = {}, 오류 = {}", noticeNo, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "공지사항 완전 삭제에 실패했습니다: " + e.getMessage()));
        }
    }

    // 작성자별 공지사항 조회
    @GetMapping("/writer/{writer}")
    public ResponseEntity<List<NoticeDTO>> getNoticesByWriter(@PathVariable String writer) {
        try {
            List<NoticeDTO> notices = noticeService.getNoticesByWriter(writer);
            return ResponseEntity.ok(notices);
        } catch (Exception e) {
            log.error("작성자별 공지사항 조회 실패: writer = {}, 오류 = {}", writer, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    // 조회수 증가 (별도 API - 필요시)
    @PostMapping("/{noticeNo}/view")
    public ResponseEntity<?> increaseViewCount(@PathVariable Long noticeNo) {
        try {
            noticeService.increaseViewCount(noticeNo);
            return ResponseEntity.ok(Map.of("message", "조회수가 증가되었습니다."));
        } catch (Exception e) {
            log.error("조회수 증가 실패: ID = {}, 오류 = {}", noticeNo, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "조회수 증가에 실패했습니다: " + e.getMessage()));
        }
    }

    // 공지사항 통계 (관리자용)
    @GetMapping("/admin/statistics")
    public ResponseEntity<Map<String, Object>> getNoticeStatistics() {
        try {
            List<NoticeDTO> allNotices = noticeService.getAllActiveNotices();
            List<NoticeDTO> importantNotices = noticeService.getImportantNotices();

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalNotices", allNotices.size());
            statistics.put("importantNotices", importantNotices.size());
            statistics.put("totalViews", allNotices.stream()
                    .mapToInt(notice -> notice.getViewCount() != null ? notice.getViewCount() : 0)
                    .sum());

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("공지사항 통계 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "공지사항 통계 조회에 실패했습니다: " + e.getMessage()));
        }
    }
}