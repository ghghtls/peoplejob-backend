package com.people.job.scrap.service;

import com.people.job.scrap.dto.ScrapDTO;

import java.util.List;

public interface ScrapService {

    void addScrap(ScrapDTO dto);

    List<ScrapDTO> getScrapsByUser(Long userNo);

    void deleteScrap(Long scrapNo);

    void deleteScrapByUserAndJob(Long userNo, Long jobopeningNo);

    boolean isScraped(Long userNo, Long jobNo);
}
