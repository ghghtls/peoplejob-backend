package com.people.job.admin.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {
    private long totalUsers;
    private long totalJobs;
    private long totalInquiries;
    private long totalPayments;
}