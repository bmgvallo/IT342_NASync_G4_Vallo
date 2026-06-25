package com.citu.nasync_backend.features.duty;

import com.citu.nasync_backend.shared.enums.DutyStatus;

public class DecisionRequest {
    private DutyStatus newStatus;
    private String remarks;

    public DutyStatus getNewStatus() { return newStatus; }
    public void setNewStatus(DutyStatus newStatus) { this.newStatus = newStatus; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
