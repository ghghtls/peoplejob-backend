package com.people.job.notice.service;

import com.people.job.notice.dto.NoticeDTO;
import com.people.job.notice.entity.NoticeEntity;
import com.people.job.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;

    @Override
    public Long createNotice(NoticeDTO noticeDTO) {
        try {
            NoticeEntity noticeEntity = NoticeEntity.builder()
                    .title(noticeDTO.getTitle())
                    .content(noticeDTO.getContent())
                    .writer(noticeDTO.getWriter())
                    .regdate(noticeDTO.getRegdate() != null ? noticeDTO.getRegdate() : LocalDate.now())
                    .isImportant(noticeDTO.getIsImportant() != null ? noticeDTO.getIsImportant() : false)
                    .isActive(noticeDTO.getIsActive() != null ? noticeDTO.getIsActive() : true)
                    .filename(noticeDTO.getFilename())
                    .originalFilename(noticeDTO.getOriginalFilename())
                    .build();

            NoticeEntity savedEntity = noticeRepository.save(noticeEntity);
            log.info("공지사항 생성 완료: ID = {}, 제목 = {}", savedEntity.getNoticeNo(), savedEntity.getTitle());

            return savedEntity.getNoticeNo();
        } catch (Exception e) {
            log.error("공지사항 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("공지사항 생성에 실패했습니다.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoticeDTO> getAllActiveNotices() {
        try {
            List<NoticeEntity> entities = noticeRepository.findByIsActiveTrueOrderByRegdateDesc();
            return entities.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("활성 공지사항 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("공지사항 목록 조회에 실패했습니다.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeDTO> getActiveNotices(Pageable pageable) {
        try {
            Page<NoticeEntity> entities = noticeRepository.findByIsActiveTrueOrderByRegdateDesc(pageable);
            return entities.map(this::convertToDTO);
        } catch (Exception e) {
            log.error("공지사항 페이징 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("공지사항 목록 조회에 실패했습니다.");
        }
    }

    @Override
    @Transactional
    public NoticeDTO getNoticeDetail(Long noticeNo) {
        try {
            NoticeEntity entity = noticeRepository.findById(noticeNo)
                    .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다. ID: " + noticeNo));

            // 조회수 증가
            entity.incrementViewCount();
            noticeRepository.save(entity);

            log.info("공지사항 상세 조회: ID = {}, 조회수 = {}", noticeNo, entity.getViewCount());
            return convertToDTO(entity);
        } catch (Exception e) {
            log.error("공지사항 상세 조회 실패: ID = {}, 오류 = {}", noticeNo, e.getMessage(), e);
            throw new RuntimeException("공지사항 상세 조회에 실패했습니다.");
        }
    }

    @Override
    public void updateNotice(NoticeDTO noticeDTO) {
        try {
            NoticeEntity entity = noticeRepository.findById(noticeDTO.getNoticeNo())
                    .orElseThrow(() -> new RuntimeException("수정할 공지사항을 찾을 수 없습니다."));

            entity.setTitle(noticeDTO.getTitle());
            entity.setContent(noticeDTO.getContent());
            entity.setIsImportant(noticeDTO.getIsImportant());
            entity.setIsActive(noticeDTO.getIsActive());
            entity.setFilename(noticeDTO.getFilename());
            entity.setOriginalFilename(noticeDTO.getOriginalFilename());

            noticeRepository.save(entity);
            log.info("공지사항 수정 완료: ID = {}", noticeDTO.getNoticeNo());
        } catch (Exception e) {
            log.error("공지사항 수정 실패: ID = {}, 오류 = {}", noticeDTO.getNoticeNo(), e.getMessage(), e);
            throw new RuntimeException("공지사항 수정에 실패했습니다.");
        }
    }

    @Override
    public void deleteNotice(Long noticeNo) {
        try {
            NoticeEntity entity = noticeRepository.findById(noticeNo)
                    .orElseThrow(() -> new RuntimeException("삭제할 공지사항을 찾을 수 없습니다."));

            // 논리 삭제 (비활성화)
            entity.setIsActive(false);
            noticeRepository.save(entity);

            log.info("공지사항 논리 삭제 완료: ID = {}", noticeNo);
        } catch (Exception e) {
            log.error("공지사항 삭제 실패: ID = {}, 오류 = {}", noticeNo, e.getMessage(), e);
            throw new RuntimeException("공지사항 삭제에 실패했습니다.");
        }
    }

    @Override
    public void permanentDeleteNotice(Long noticeNo) {
        try {
            noticeRepository.deleteById(noticeNo);
            log.info("공지사항 물리 삭제 완료: ID = {}", noticeNo);
        } catch (Exception e) {
            log.error("공지사항 물리 삭제 실패: ID = {}, 오류 = {}", noticeNo, e.getMessage(), e);
            throw new RuntimeException("공지사항 완전 삭제에 실패했습니다.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoticeDTO> getImportantNotices() {
        try {
            List<NoticeEntity> entities = noticeRepository.findByIsImportantTrueAndIsActiveTrueOrderByRegdateDesc();
            return entities.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("중요 공지사항 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("중요 공지사항 조회에 실패했습니다.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeDTO> searchNotices(String keyword, Pageable pageable) {
        try {
            Page<NoticeEntity> entities = noticeRepository.findByKeywordInTitleOrContent(keyword, pageable);
            return entities.map(this::convertToDTO);
        } catch (Exception e) {
            log.error("공지사항 검색 실패: 키워드 = {}, 오류 = {}", keyword, e.getMessage(), e);
            throw new RuntimeException("공지사항 검색에 실패했습니다.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeDTO> searchByTitle(String title, Pageable pageable) {
        try {
            Page<NoticeEntity> entities = noticeRepository.findByIsActiveTrueAndTitleContainingIgnoreCaseOrderByRegdateDesc(title, pageable);
            return entities.map(this::convertToDTO);
        } catch (Exception e) {
            log.error("제목으로 공지사항 검색 실패: 제목 = {}, 오류 = {}", title, e.getMessage(), e);
            throw new RuntimeException("공지사항 제목 검색에 실패했습니다.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeDTO> searchByContent(String content, Pageable pageable) {
        try {
            Page<NoticeEntity> entities = noticeRepository.findByIsActiveTrueAndContentContainingIgnoreCaseOrderByRegdateDesc(content, pageable);
            return entities.map(this::convertToDTO);
        } catch (Exception e) {
            log.error("내용으로 공지사항 검색 실패: 내용 = {}, 오류 = {}", content, e.getMessage(), e);
            throw new RuntimeException("공지사항 내용 검색에 실패했습니다.");
        }
    }

    @Override
    @Async("taskExecutor")
    @Transactional
    public void increaseViewCount(Long noticeNo) {
        try {
            noticeRepository.incrementViewCount(noticeNo);
            log.debug("조회수 증가: ID = {}", noticeNo);
        } catch (Exception e) {
            log.error("조회수 증가 실패: ID = {}, 오류 = {}", noticeNo, e.getMessage(), e);
            // 조회수 증가 실패는 전체 프로세스를 중단시키지 않음
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoticeDTO> getRecentNotices(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<NoticeEntity> entities = noticeRepository.findRecentNotices(pageable);
            return entities.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("최근 공지사항 조회 실패: limit = {}, 오류 = {}", limit, e.getMessage(), e);
            throw new RuntimeException("최근 공지사항 조회에 실패했습니다.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NoticeDTO> getAllNoticesForAdmin(Pageable pageable) {
        try {
            Page<NoticeEntity> entities = noticeRepository.findAllByOrderByRegdateDesc(pageable);
            return entities.map(this::convertToDTO);
        } catch (Exception e) {
            log.error("관리자용 공지사항 조회 실패: {}", e.getMessage(), e);
            throw new RuntimeException("관리자용 공지사항 조회에 실패했습니다.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<NoticeDTO> getNoticesByWriter(String writer) {
        try {
            List<NoticeEntity> entities = noticeRepository.findByWriterOrderByRegdateDesc(writer);
            return entities.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("작성자별 공지사항 조회 실패: writer = {}, 오류 = {}", writer, e.getMessage(), e);
            throw new RuntimeException("작성자별 공지사항 조회에 실패했습니다.");
        }
    }

    @Override
    public void toggleNoticeStatus(Long noticeNo) {
        try {
            NoticeEntity entity = noticeRepository.findById(noticeNo)
                    .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));

            entity.setIsActive(!entity.getIsActive());
            noticeRepository.save(entity);

            log.info("공지사항 상태 변경: ID = {}, 활성화 = {}", noticeNo, entity.getIsActive());
        } catch (Exception e) {
            log.error("공지사항 상태 변경 실패: ID = {}, 오류 = {}", noticeNo, e.getMessage(), e);
            throw new RuntimeException("공지사항 상태 변경에 실패했습니다.");
        }
    }

    @Override
    public void toggleImportantStatus(Long noticeNo) {
        try {
            NoticeEntity entity = noticeRepository.findById(noticeNo)
                    .orElseThrow(() -> new RuntimeException("공지사항을 찾을 수 없습니다."));

            entity.setIsImportant(!entity.getIsImportant());
            noticeRepository.save(entity);

            log.info("공지사항 중요도 변경: ID = {}, 중요 = {}", noticeNo, entity.getIsImportant());
        } catch (Exception e) {
            log.error("공지사항 중요도 변경 실패: ID = {}, 오류 = {}", noticeNo, e.getMessage(), e);
            throw new RuntimeException("공지사항 중요도 변경에 실패했습니다.");
        }
    }

    // DTO 변환 메서드
    private NoticeDTO convertToDTO(NoticeEntity entity) {
        return NoticeDTO.builder()
                .noticeNo(entity.getNoticeNo())
                .title(entity.getTitle())
                .content(entity.getContent())
                .writer(entity.getWriter())
                .regdate(entity.getRegdate())
                .viewCount(entity.getViewCount())
                .isImportant(entity.getIsImportant())
                .isActive(entity.getIsActive())
                .filename(entity.getFilename())
                .originalFilename(entity.getOriginalFilename())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}