package com.citu.nasync_backend.features.admin;

public class DashboardStatsResponse {

    private long totalScholars;
    private long pendingApprovals;
    private long totalAbsencesThisSemester;
    private long activeDutiesToday;

    public DashboardStatsResponse(long totalScholars, long pendingApprovals,
                                   long totalAbsencesThisSemester, long activeDutiesToday) {
        this.totalScholars = totalScholars;
        this.pendingApprovals = pendingApprovals;
        this.totalAbsencesThisSemester = totalAbsencesThisSemester;
        this.activeDutiesToday = activeDutiesToday;
    }

    public long getTotalScholars() { return totalScholars; }
    public long getPendingApprovals() { return pendingApprovals; }
    public long getTotalAbsencesThisSemester() { return totalAbsencesThisSemester; }
    public long getActiveDutiesToday() { return activeDutiesToday; }
}
