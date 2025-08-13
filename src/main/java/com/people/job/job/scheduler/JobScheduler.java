package com.people.job.job.scheduler;

import com.people.job.job.service.JobopeningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobScheduler {

    private final JobopeningService jobopeningService;

    /**
     * 매일 자정에 마감일이 지난 채용공고들을 자동으로 마감 처리
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정 실행
    public void expireOverdueJobs() {
        log.info("채용공고 자동 마감 처리 스케줄러 시작");

        try {
            jobopeningService.expireOverdueJobs();
            log.info("채용공고 자동 마감 처리 스케줄러 완료");
        } catch (Exception e) {
            log.error("채용공고 자동 마감 처리 중 오류 발생", e);
        }
    }

    /**
     * 매시간 정각에 마감일이 지난 채용공고 확인 (더 빠른 처리를 위해)
     */
    @Scheduled(cron = "0 0 * * * *") // 매시간 정각 실행
    public void hourlyExpireCheck() {
        log.debug("시간별 채용공고 마감 확인 시작");

        try {
            jobopeningService.expireOverdueJobs();
        } catch (Exception e) {
            log.error("시간별 채용공고 마감 확인 중 오류 발생", e);
        }
    }
}