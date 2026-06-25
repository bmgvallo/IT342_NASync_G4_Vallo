package com.citu.nasync_backend.features.admin;

public class ScholarDutySummaryResponse {
    private final double totalRequiredHours;
    private final double completedHours;
    private final double missedHours;
    private final long totalLates;
    private final double remainingHours;

    public ScholarDutySummaryResponse(
            double totalRequiredHours,
            double completedHours,
            double missedHours,
            long totalLates,
            double remainingHours) {
        this.totalRequiredHours = totalRequiredHours;
        this.completedHours = completedHours;
        this.missedHours = missedHours;
        this.totalLates = totalLates;
        this.remainingHours = remainingHours;
    }

    public double getTotalRequiredHours() { return totalRequiredHours; }
    public double getCompletedHours()     { return completedHours; }
    public double getMissedHours()        { return missedHours; }
    public long   getTotalLates()         { return totalLates; }
    public double getRemainingHours()     { return remainingHours; }
}
